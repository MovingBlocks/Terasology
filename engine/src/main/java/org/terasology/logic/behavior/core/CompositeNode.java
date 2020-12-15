/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.core;

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
