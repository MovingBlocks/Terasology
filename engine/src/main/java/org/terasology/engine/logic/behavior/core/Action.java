// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.terasology.context.annotation.API;
import org.terasology.context.annotation.IndexInherited;

/**
 * The action that is used by an action or decorator node. Every action node of a behavior tree has its own action
 * instance. There is only one action instance for all actors, that run a behavior tree - so all state information
 * needs to be stored at the actor.
 * <p>
 * Action instances are shown in the property panel of the behavior editor.
 */
@API
@IndexInherited
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
