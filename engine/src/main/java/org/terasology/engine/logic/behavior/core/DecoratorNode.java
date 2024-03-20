// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A decorator node uses a associated Action to control the result state of the child node.
 */
public class DecoratorNode extends ActionNode {
    private static final Logger logger = LoggerFactory.getLogger(DecoratorNode.class);
    private BehaviorNode child;
    private BehaviorState lastState = BehaviorState.UNDEFINED;

    public DecoratorNode() {
    }

    public DecoratorNode(Action action) {
        super(action);
    }

    @Override
    public String getName() {
        return action != null ? action.getName() : "decorator";
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        T childItem = visitor.visit(item, this);
        for (int i = 0; i < getChildrenCount(); i++) {
            getChild(i).visit(childItem, visitor);
        }
        return childItem;
    }

    @Override
    public BehaviorNode deepCopy() {
        DecoratorNode node = new DecoratorNode();
        node.setAction(action);
        node.child = child;
        return node;
    }

    @Override
    public void construct(Actor actor) {
        if (action != null) {
            try {
                action.construct(actor);
            } catch (Exception e) {
                logger.info("Exception while running construct() of action {} from entity {}:", action, actor.getEntity()); //NOPMD
            }
        }
    }

    private void runChild(Actor actor) {
        if (child == null) {
            return;
        }
        if (lastState != BehaviorState.RUNNING) {
            child.construct(actor);
        }
        lastState = child.execute(actor);
        if (lastState != BehaviorState.RUNNING) {
            child.destruct(actor);
        }
    }

    @Override
    public BehaviorState execute(Actor actor) {
        if (action == null) {
            runChild(actor);
            return lastState;
        }
        if (!action.prune(actor)) {
            runChild(actor);
        }

        BehaviorState modifiedState;
        try {
            modifiedState = action.modify(actor, lastState);
        } catch (Exception e) {
            logger.info("Exception while running action {} from entity {}: {}", action, actor.getEntity(), e.getStackTrace()); //NOPMD
            // TODO maybe returning UNDEFINED would be more canonical?
            return BehaviorState.FAILURE;
        }

        if (modifiedState != BehaviorState.RUNNING && lastState == BehaviorState.RUNNING) {
            child.destruct(actor);
        }
        return modifiedState;
    }

    @Override
    public void destruct(Actor actor) {
        if (action != null) {
            action.destruct(actor);
        }
    }

    @Override
    public void insertChild(int index, BehaviorNode aChild) {
        replaceChild(index, aChild);
    }

    @Override
    public void replaceChild(int index, BehaviorNode aChild) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        this.child = aChild;
    }

    @Override
    public BehaviorNode removeChild(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        return child;
    }

    @Override
    public BehaviorNode getChild(int index) {
        if (index != 0) {
            throw new IllegalArgumentException("Decorator accepts only one child!");
        }
        return child;
    }

    @Override
    public int getChildrenCount() {
        return child != null ? 1 : 0;
    }

    @Override
    public int getMaxChildren() {
        return 1;
    }
}
