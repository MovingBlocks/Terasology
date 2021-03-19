// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * Runs always.
 */
public class RunningNode extends LeafNode {
    @Override
    public String getName() {
        return "running";
    }

    @Override
    public BehaviorNode deepCopy() {
        return new RunningNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {

    }
    

}
