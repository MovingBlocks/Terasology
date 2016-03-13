/*
 * Copyright 2014 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.module.sandbox.API;

/**
 * A task run by an {@link Interpreter} for an {@link Actor}.
 *
 */
@API
public abstract class Task {
    private static final Logger LOG = LoggerFactory.getLogger(Task.class);
    private Interpreter interpreter;
    private Node node;
    private Actor actor;
    private Status status = Status.NOT_INITIALIZED;
    private Task parent;

    protected Task(Node node) {
        this.node = node;
    }

    /**
     * Is called when this tasks should initialize itself (first tick)
     */
    public void onInitialize() {
    }

    /**
     * Is called on each tick if the Task is {@link Status#RUNNING}
     * @return the new Status, this should be one of
     * <ul>
     * <li>{@link Status#RUNNING} (continue execution)</li>
     * <li>{@link Status#SUCCESS} (task finished with success)</li>
     * <li>{@link Status#FAILURE} (task finished not / had an error)</li>
     * </ul>
     */
    public abstract Status update(float dt);

    /**
     * Is called when is task is terminated with a new status.
     */
    public void onTerminate(Status result) {
    }

    /**
     * Is called when a sub tasks finishes
     */
    public abstract void handle(Status result);

    /**
     * Executes on tick on the task, depending on the status:
     * <ul>
     * <li> {@link Status#NOT_INITIALIZED} : initialize ({@link #onInitialize()}) and set status to {@link Status#RUNNING}</li>
     * <li> {@link Status#RUNNING} : {@link #update(float)} the task</li>
     * <li> {@link Status#SUSPENDED} : do nothing</li>
     * <li> {@link Status#SUCCESS} or {@link Status#FAILURE} : status will be terminated, success or failure can be handled in {@link #onTerminate(Status)}
     * <li> {@link Status#TERMINATED} : terminated task</li>
     * </ul>
     * @param deltaSeconds Seconds since last engine update
     * @return The updated status of the task
     */
    public Status tick(float deltaSeconds) {
        switch (status) {
            //not initialized: initialize and set running
            case NOT_INITIALIZED: {
                onInitialize();
                status = Status.RUNNING;
                break;
            }
                //running: update
            case RUNNING: {
                Status newStatus = update(deltaSeconds);
                if (!(newStatus == Status.RUNNING || newStatus == Status.SUCCESS || newStatus == Status.FAILURE)) {
                    LOG.warn("update of Task {} returned invalid state {}", this.getClass(), newStatus);
                }
                status = newStatus;
                break;
            }
                //suspended: do nothing
            case SUSPENDED:
                break;
            //end of running: terminate
            case FAILURE:
            case SUCCESS: {
                onTerminate(status);
                status = Status.TERMINATED;
                break;
            }
                //terminated, do nothing
            case TERMINATED: {
                break;
            }
            default:
                break;

        }
        return status;
    }

    public void start(Node child) {
        interpreter().start(child, this);
    }

    public void stop(Status result) {
        interpreter().stop(this, result);
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

    void setParent(Task parent) {
        this.parent = parent;
    }

    void setActor(Actor actor) {
        this.actor = actor;
    }

    void setInterpreter(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    Task getParent() {
        return parent;
    }

}
