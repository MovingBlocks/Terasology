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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.module.sandbox.API;
import org.terasology.registry.InjectionHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

/**
 * An interpreter evaluates a behavior tree. This is done by creating tasks for an actor for the nodes of the BT.
 * If a task returns RUNNING, the task is placed to the active list and asked next tick again.
 * Finished nodes may create new tasks, which are placed to the active list.
 * <br><br>
 *
 */
@API
public class Interpreter {

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
        return start(task, parent);
    }

    private Task start(final Task task, Task parent) {
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
        return AccessController.doPrivileged((PrivilegedAction<Task>) () -> {
            InjectionHelper.inject(task);
            return task;
        });
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

    /**
     * Executes one tick on the interpreter (one step)
     * @param deltaSeconds Seconds since last update
     * @return the number of started nodes
     */
    public int tick(float deltaSeconds) {
        if (debugger == null || debugger.beforeTick()) {
            startedNodes.clear();
            while (step(deltaSeconds)) {
                continue;
            }
            if (debugger != null) {
                debugger.afterTick();
            }
        }
        return startedNodes.size();
    }

    /**
     * @param deltaSeconds Seconds since last update
     * @return false if no nodes were updated
     */
    public boolean step(float deltaSeconds) {
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

        current.tick(deltaSeconds);

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
        //try to find the best name for the entity.
        //use display name first
        if (actor.hasComponent(DisplayNameComponent.class)) {
            return actor.getComponent(DisplayNameComponent.class).name;
        }
        //minimal name: id of the entity
        String entityId = "entityId: " + actor.getEntity().getId();
        //if possible, attach the prefab name for better readability
        Prefab parentPrefab = actor.getEntity().getParentPrefab();
        if (parentPrefab != null) {
            return "prefab: " + parentPrefab.getName() + " " + entityId;
        } else {
            return entityId;
        }
    }

    public interface Debugger {
        void nodeFinished(Node node, Status status);

        void nodeUpdated(Node node, Status status);

        void started();

        boolean beforeTick();

        void afterTick();
    }
}
