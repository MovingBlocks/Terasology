/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.API;

import java.util.Deque;
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
    };

    private Debugger debugger;
    private Actor actor;
    private Deque<Task> tasks = Queues.newLinkedBlockingDeque();
    private Node root;
    private Set<Node> startedNodes = Sets.newHashSet();

    public Interpreter(Actor actor) {
        this.actor = actor;
        tasks.addLast(TERMINAL);
    }

    public Actor actor() {
        return actor;
    }

    public void reset() {
        tasks.clear();
        start();
        tasks.addLast(TERMINAL);
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public void start() {
        start(root, null);
        if (debugger != null) {
            debugger.started();
        }
    }

    public void start(Task task) {
        start(task, null);
    }

    public void start(Node node, Task.Observer observer) {
        start(node.createTask(), observer);
    }

    public void start(Task task, Task.Observer observer) {
        task.setActor(actor);
        task.setInterpreter(this);
        task.setObserver(observer);

        tasks.addFirst(task);
    }

    public void stop(Task task, Status result) {
        task.setStatus(result);
        Task.Observer observer = task.getObserver();
        if (observer != null) {
            observer.handle(result);
        }
        tasks.remove(task);
        if (debugger != null) {
            debugger.nodeFinished(task.getNode(), result);
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
            if (current.getObserver() != null) {
                current.getObserver().handle(current.getStatus());
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
        return actor.minion().toString();
    }

    public interface Debugger {
        void nodeFinished(Node node, Status status);

        void nodeUpdated(Node node, Status status);

        void started();

        boolean beforeTick();

        void afterTick();
    }
}
