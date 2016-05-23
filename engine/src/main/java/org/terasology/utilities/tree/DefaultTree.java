/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.utilities.tree;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A simple implementation of the Tree interface.
 * <p>
 * Uses an ArrayList for storing children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public class DefaultTree<T> implements Tree<T> {
    private static final String NULL_NODE_ARGUMENT = "node argument is null";
    private static final String NODE_ARGUMENT_INVALID_PARENT = "node argument is not a child of this tree";

    private T value;
    private Tree<T> parent;
    private List<Tree<T>> children = Lists.newArrayList();

    public DefaultTree() {
        this(null);
    }

    public DefaultTree(T value) {
        this.value = value;
    }

    @Override
    public Tree<T> getChildAt(int childIndex) {
        return this.children.get(childIndex);
    }

    @Override
    public int getChildCount() {
        return this.children.size();
    }

    @Override
    public Tree<T> getParent() {
        return this.parent;
    }

    @Override
    public int getIndex(Tree<T> node) {
        if (node == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        return this.children.indexOf(node);
    }

    @Override
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    @Override
    public boolean isRoot() {
        return this.parent == null;
    }

    @Override
    public void addChild(int childIndex, Tree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        this.children.add(childIndex, child);
        child.setParent(this);
    }

    @Override
    public void removeChild(int childIndex) {
        Tree<T> child = this.children.remove(childIndex);
        child.setParent(null);
    }

    @Override
    public void removeChild(Tree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }
        if (child.getParent() != this) {
            throw new IllegalArgumentException(NODE_ARGUMENT_INVALID_PARENT);
        }

        this.children.remove(child);
        child.setParent(null);
    }

    @Override
    public void setParent(Tree<T> parent) {
        this.parent.removeChild(this);
        this.parent = parent;
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }
}
