// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

/**
 * Runs all children parallel.
 */
public class ParallelNode extends CompositeNode {

    private enum Policy {
        REQUIRE_ONE, REQUIRE_ALL
    }

    private Policy policy = Policy.REQUIRE_ONE;

    @Override
    public String getName() {
        return "parallel";
    }

    @Override
    public BehaviorNode deepCopy() {
        ParallelNode result = new ParallelNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        for (BehaviorNode child : children) {
            child.construct(actor);
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        int successCounter = 0;
        for (BehaviorNode child : children) {
            BehaviorState result = child.execute(actor);
            if (result == BehaviorState.FAILURE) {
                return BehaviorState.FAILURE;
            }
            if (result == BehaviorState.SUCCESS) {
                successCounter++;
            }
        }
        return checkSuccess(successCounter);
    }

    public BehaviorState checkSuccess(int successCounter) {
        if (policy == Policy.REQUIRE_ALL && successCounter == children.size()) {
            return BehaviorState.SUCCESS;
        }
        if (policy == Policy.REQUIRE_ONE && successCounter > 0) {
            return BehaviorState.SUCCESS;
        }
        return BehaviorState.RUNNING;
    }

    @Override
    public void destruct(Actor actor) {
        for (BehaviorNode child : children) {
            child.destruct(actor);
        }
    }

}
