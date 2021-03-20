// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import java.util.Iterator;

/**
 * Runs all children, until one finishes with SUCCESS
 */
public class SelectorNode extends CompositeNode {
    private Iterator<BehaviorNode> iterator;
    private BehaviorNode current;

    @Override
    public String getName() {
        return "selector";
    }

    @Override
    public BehaviorNode deepCopy() {
        SelectorNode result = new SelectorNode();
        for (BehaviorNode child : children) {
            result.children.add(child.deepCopy());
        }
        return result;

    }

    @Override
    public void construct(Actor actor) {
        iterator = children.iterator();
        nextChild(actor);
    }

    @Override
    public BehaviorState execute(Actor actor) {
        BehaviorState result;
        while (current != null) {
            result = current.execute(actor);
            if (result == BehaviorState.RUNNING) {
                return BehaviorState.RUNNING;
            }
            current.destruct(actor);
            if (result == BehaviorState.SUCCESS) {
                return BehaviorState.SUCCESS;
            } else {
                nextChild(actor);
            }
        }
        return BehaviorState.FAILURE;
    }

    private void nextChild(Actor actor) {
        if (iterator.hasNext()) {
            current = iterator.next();
            current.construct(actor);
        } else {
            current = null;
        }
    }

    @Override
    public void destruct(Actor actor) {
    }

}
