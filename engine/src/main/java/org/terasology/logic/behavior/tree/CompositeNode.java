/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.logic.behavior.tree;

import com.google.common.collect.Lists;
import org.terasology.module.sandbox.API;

import java.util.List;

/**
 * A composite node aka node with children. Provides methods to manipulate the children list.
 *
 */
@API
public abstract class CompositeNode extends Node {
    private final List<Node> children = Lists.newArrayList();

    public List<Node> children() {
        return children;
    }

    @Override
    public <T> T visit(T item, Visitor<T> visitor) {
        T childItem = super.visit(item, visitor);
        for (Node child : children()) {
            child.visit(childItem, visitor);
        }
        return childItem;
    }

    @Override
    public int getMaxChildren() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void insertChild(int index, Node child) {
        if (index == -1) {
            children.add(child);
        } else {
            children.add(index, child);
        }
    }

    @Override
    public void setChild(int index, Node child) {
        if (index == children.size()) {
            children.add(null);
        }
        children.set(index, child);
    }

    @Override
    public Node removeChild(int index) {
        return children.remove(index);
    }

    @Override
    public Node getChild(int index) {
        return children.get(index);
    }

    @Override
    public int getChildrenCount() {
        return children.size();
    }

    public abstract static class CompositeTask extends Task {
        protected CompositeTask(Node node) {
            super(node);
        }

        @Override
        public CompositeNode getNode() {
            return (CompositeNode) super.getNode();
        }
    }
}
