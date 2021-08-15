// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import com.google.common.collect.Lists;
import org.terasology.nui.properties.PropertyProvider;

import java.util.List;

/**
 * Behavior node with a limited number of children (ordered).
 */
public abstract class CompositeNode implements BehaviorNode {
    protected final List<BehaviorNode> children = Lists.newArrayList();

    @Override
    public PropertyProvider getProperties() {
        return null;
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        T childItem = visitor.visit(item, this);
        for (BehaviorNode child : children) {
            child.visit(childItem, visitor);
        }
        return childItem;
    }

    @Override
    public void insertChild(int index, BehaviorNode child) {
        children.add(index, child);
    }

    @Override
    public void replaceChild(int index, BehaviorNode child) {
        if (index < children.size()) {
            children.set(index, child);
        } else {
            children.add(index, child);
        }
    }

    @Override
    public BehaviorNode removeChild(int index) {
        return children.remove(index);
    }

    @Override
    public BehaviorNode getChild(int index) {
        return children.get(index);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    @Override
    public int getMaxChildren() {
        return Integer.MAX_VALUE;
    }


    public List<BehaviorNode> getChildren() {
        return children;
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
