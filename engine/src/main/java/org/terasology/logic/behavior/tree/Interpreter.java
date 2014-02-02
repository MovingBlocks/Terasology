/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.API;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.registry.InjectionHelper;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * An interpreter evaluates a behavior tree. This is done by creating tasks for an actor for the nodes of the BT.
 * If a task returns RUNNING, the task is placed to the active list and asked next tick again.
 * Finished nodes may create new tasks, which are placed to the active list.
 * <p/>
 *
 * @author synopia
 */
@API
public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    private static final Task TERMINAL = new Task(null) {
        @Override
        public Status update(float dt) {
            return null;
        }

        @Override
        public void handle(Status result) {

        }
    };

    private Debugger debugger;
    private Actor actor;
    private Deque<Task> tasks = Queues.newLinkedBlockingDeque();
    private Node root;
    private Set<Node> startedNodes = Sets.newHashSet();
    private Map<Task, List<Task>> startedTasks = Maps.newHashMap();

    public Interpreter(Actor actor) {
        this.actor = actor;
        tasks.addLast(TERMINAL);
    }

    public Actor actor() {
        return actor;
    }

    public void reset() {
        tasks.clear();
        startedTasks.clear();

        start(root);
        tasks.addLast(TERMINAL);
    }

    public Task start(Node start) {
        root = start;
        Task task = start(start, null);
        if (debugger != null) {
            debugger.started();
        }
        return task;
    }

    public Task start(Node node, Task parent) {
        if (node == null) {
            return null;
        }
        Task task = node.createTask();
        start(task, parent);
        return task;
    }

    private void start(Task task, Task parent) {
        InjectionHelper.inject(task);
        task.setActor(actor);
        task.setInterpreter(this);
        task.setParent(parent);
        if (parent != null) {
            List<Task> subTasks = startedTasks.get(parent);
            if (subTasks == null) {
                subTasks = Lists.newArrayList();
                startedTasks.put(parent, subTasks);
            }
            subTasks.add(task);
        }
        tasks.addFirst(task);
    }

    public void stop(Task task, Status result) {
        task.setStatus(result);
        Task parent = task.getParent();
        if (parent != null) {
            parent.handle(result);
        }
        stopStartedTasks(task);
        if (debugger != null) {
            debugger.nodeFinished(task.getNode(), result);
        }
    }

    private void stopStartedTasks(Task parent) {
        Queue<Task> open = Queues.newArrayDeque();
        open.offer(parent);
        while (!open.isEmpty()) {
            Task current = open.poll();
            if (current.getStatus() == Status.RUNNING) {
                current.onTerminate(Status.FAILURE);
            }
            tasks.remove(current);
            List<Task> subTasks = startedTasks.remove(current);
            if (subTasks != null) {
                open.addAll(subTasks);
            }
        }
    }

    public int tick(float dt) {
        if (debugger == null || debugger.beforeTick()) {
            startedNodes.clear();
            while (step(dt)) {
                continue;
            }
            if (debugger != null) {
                debugger.afterTick();
            }
        }
        return startedNodes.size();
    }

    public boolean step(float dt) {
        Task current = tasks.pollFirst();
        if (current == TERMINAL) {
            tasks.addLast(TERMINAL);
            return false;
        }

        if (startedNodes.contains(current.getNode())) {
            tasks.addLast(current);
            return true;
        }
        startedNodes.add(current.getNode());

        current.tick(dt);

        if (current.getStatus() != Status.RUNNING) {
            if (debugger != null) {
                debugger.nodeFinished(current.getNode(), current.getStatus());
            }
            if (current.getParent() != null) {
                stop(current, current.getStatus());
            }
        } else {
            tasks.addLast(current);
            if (debugger != null) {
                debugger.nodeUpdated(current.getNode(), current.getStatus());
            }
        }
        return true;
    }

    public void setDebugger(Debugger debugger) {
        this.debugger = debugger;
    }

    @Override
    public String toString() {
        return actor.component(DisplayNameComponent.class).name;
    }

    public interface Debugger {
        void nodeFinished(Node node, Status status);

        void nodeUpdated(Node node, Status status);

        void started();

        boolean beforeTick();

        void afterTick();
    }
}
