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

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Deque;
import java.util.List;

/**
 * An interpreter evaluates a behavior tree. This is done by creating tasks for an actor for the nodes of the BT.
 * If a task returns RUNNING, the task is placed to the active list and asked next tick again.
 * Finished nodes may create new tasks, which are placed to the active list.
 * <p/>
 * TODO add a (synchronized) "debugger" interface, to allow access from gui or other services (instead of the blocking queue).
 *
 * @author synopia
 */
public class Interpreter {
    private static final Logger logger = LoggerFactory.getLogger(Interpreter.class);

    private static final Task TERMINAL = new Task(null) {
        @Override
        public Status update(float dt) {
            return null;
        }
    };

    private Actor actor;
    private Deque<Task> tasks = Queues.newLinkedBlockingDeque();
    private boolean pause;
    private Node root;
    private List<PauseListener> listeners = Lists.newArrayList();

    public Interpreter(Actor actor) {
        this.actor = actor;
        tasks.addLast(TERMINAL);
    }

    public void addListener(PauseListener listener) {
        this.listeners.add(listener);
    }

    public synchronized Deque<Task> tasks() {
        return Queues.newArrayDeque(tasks);
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
    }

    public void start(Task task) {
        start(task, null);
    }

    public void start(Node node, Task.Observer observer) {
        start(node.create(), observer);
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
    }

    public void tick(float dt) {
        if (pause) {
            return;
        }
        while (!pause && step(dt)) {
            continue;
        }
    }

    public boolean step(float dt) {
        Task current = tasks.pollFirst();
        if (current == TERMINAL) {
            tasks.addLast(TERMINAL);
            return false;
        }

        current.tick(dt);


        if (current.getStatus() != Status.RUNNING && current.getObserver() != null) {
            logger.info("Finished " + current + " with status " + current.getStatus());
            current.getObserver().handle(current.getStatus());
        } else {
            tasks.addLast(current);
        }
        return true;
    }

    @Override
    public String toString() {
        return actor.skeletalMesh().toString();
    }

    public void setPause(boolean pause) {
        this.pause = pause;
        for (PauseListener listener : listeners) {
            listener.pauseChanged(pause);
        }
    }

    public boolean isPause() {
        return pause;
    }

    public boolean isRunning() {
        return !isPause();
    }

    public interface PauseListener {
        void pauseChanged(boolean pause);
    }
}
