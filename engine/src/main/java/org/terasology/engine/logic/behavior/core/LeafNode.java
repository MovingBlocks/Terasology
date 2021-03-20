// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import org.terasology.nui.properties.PropertyProvider;

/**
 * Node without children.
 */
public abstract class LeafNode implements BehaviorNode {

    @Override
    public PropertyProvider getProperties() {
        return null;
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        return visitor.visit(item, this);
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public BehaviorNode removeChild(int index) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public BehaviorNode getChild(int index) {
        throw new IllegalArgumentException("Leaf nodes does not accept children");
    }

    @Override
    public int getChildrenCount() {
        return 0;
    }

    @Override
    public int getMaxChildren() {
        return 0;
    }


    @Override
    public void construct(Actor actor) {

    }

    @Override
    public BehaviorState execute(Actor actor) {
        return null;
    }

    @Override
    public void destruct(Actor actor) {

    }
}
