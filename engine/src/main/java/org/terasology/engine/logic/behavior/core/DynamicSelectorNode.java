// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import java.util.BitSet;

/**
 * Works like a normal selector but each update tick, all children are checked for state changes.
 */
public class DynamicSelectorNode extends CompositeNode {
    private BitSet constructed;

    @Override
    public String getName() {
        return "dynamic";
    }

    @Override
    public BehaviorNode deepCopy() {
        DynamicSelectorNode result = new DynamicSelectorNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;
    }

    @Override
    public void construct(Actor actor) {
        constructed = new BitSet(children.size());
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        for (int i = 0; i < children.size(); i++) {
            BehaviorNode child = children.get(i);
            if (!constructed.get(i)) {
                child.construct(actor);
                constructed.set(i);
            }
            result = child.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            child.destruct(actor);
            constructed.clear(i);
            if (result == BehaviorState.SUCCESS) {
                return BehaviorState.SUCCESS;
            }
        }
        return BehaviorState.FAILURE;
    }

    @Override
    public void destruct(Actor actor) {
    }

}
