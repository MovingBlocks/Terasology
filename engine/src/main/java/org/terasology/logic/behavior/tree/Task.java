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

import org.terasology.engine.API;

/**
 * A task run by an interpreter for an actor.
 *
 * @author synopia
 */
@API
public abstract class Task {
    private Interpreter interpreter;
    private Node node;
    private Actor actor;
    private Status status = Status.INVALID;
    private Observer observer;

    protected Task(Node node) {
        this.node = node;
    }

    /**
     * Is called when this tasks should initialize itself (first tick)
     */
    public void onInitialize() {
    }

    /**
     * Is called on each tick and return the new status
     */
    public abstract Status update(float dt);

    /**
     * Is called when is task is terminated with a new status.
     */
    public void onTerminate(Status result) {
    }

    public Status tick(float dt) {
        if (status == Status.INVALID) {
            onInitialize();
        }

        status = update(dt);

        if (status != Status.RUNNING) {
            onTerminate(status);
        }
        return status;
    }

    public Status getStatus() {
        return status;
    }

    public Node getNode() {
        return node;
    }

    public Interpreter interpreter() {
        return interpreter;
    }

    public Actor actor() {
        return actor;
    }

    void setStatus(Status status) {
        this.status = status;
    }

    void setObserver(Observer observer) {
        this.observer = observer;
    }

    void setActor(Actor actor) {
        this.actor = actor;
    }

    void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    Observer getObserver() {
        return observer;
    }

    public interface Observer {
        void handle(Status result);
    }

}
