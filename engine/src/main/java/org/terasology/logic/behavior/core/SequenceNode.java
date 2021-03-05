// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Runs all children until one finishes with FAILURE
 */
public class SequenceNode extends CompositeNode {
    private static final Logger logger = LoggerFactory.getLogger(SequenceNode.class);
    private Iterator<BehaviorNode> iterator;
    private BehaviorNode current;
    private String reentry;

    @Override
    public String getName() {
        return "sequence";
    }

    @Override
    public BehaviorNode deepCopy() {
        SequenceNode result = new SequenceNode();
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
            if (result == BehaviorState.FAILURE) {
                return BehaviorState.FAILURE;
            } else {
                nextChild(actor);
            }
        }
        return BehaviorState.SUCCESS;
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
