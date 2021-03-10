// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * Succeed always.
 */
public class SuccessNode extends LeafNode {
    @Override
    public String getName() {
        return "success";
    }

    @Override
    public BehaviorNode deepCopy() {
        return new SuccessNode();
    }

    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return BehaviorState.SUCCESS;
    }

    @Override
    public void destruct(Actor actor) {

    }


}
