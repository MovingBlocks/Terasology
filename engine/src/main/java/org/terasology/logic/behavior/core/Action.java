/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.logic.behavior.core;

import org.terasology.gestalt.module.sandbox.API;

/**
 * The action that is used by an action or decorator node. Every action node of a behavior tree has its own action
 * instance. There is only one action instance for all actors, that run a behavior tree - so all state information
 * needs to be stored at the actor.
 * <p/>
 * Action instances are shown in the property panel of the behavior editor.
 */
@API
public interface Action {

    /**
     * @return the name of this action used by serialization.
     */
    String getName();

    /**
     * @return the id of the action node, which is unique in a behavior tree
     */
    int getId();

    void setId(int id);

    /**
     * Is called right after all fields are injected.
     */
    void setup();

    /**
     * Is called when the action node is constructed on first update tick
     */
    void construct(Actor actor);

    /**
     * Is called before the update. If the action node is a decorator, the result will decide if the child node should
     * be run.
     *
     * @return true, child node is not run; false, child is run
     */
    boolean prune(Actor actor);

    /**
     * Is called after the update with the result of the child node, if it is not pruned.
     *
     * @param result BehaviorState.UNDEFINED if this node is a action node or pruned
     * @return the final state of the node
     */
    BehaviorState modify(Actor actor, BehaviorState result);

    /**
     * Is called when the action node is deconstructed on last update tick (when state switches from RUNNING to anything else)
     */
    void destruct(Actor actor);


}
