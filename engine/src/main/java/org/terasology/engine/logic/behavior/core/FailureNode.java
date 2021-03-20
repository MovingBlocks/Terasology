// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * Fails always.
 */
public class FailureNode extends LeafNode {
    @Override
    public String getName() {
        return "failure";
    }

    @Override
    public BehaviorNode deepCopy() {
        return new FailureNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.FAILURE;
    }

    @Override
    public void destruct(Actor actor) {

    }

}
