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
package org.terasology.rendering.nui.widgets.models;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * A general-purpose tree data structure.
 * <p>
 * Stores a single object. May have a single parent and an arbitrary amount of children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public class ExpandableTree<T> {
    private static final String NULL_NODE_ARGUMENT = "node argument is null";
    private static final String NODE_ARGUMENT_INVALID_PARENT = "node argument is not a child of this tree";
    
    private T value;
    private ExpandableTree<T> parent;
    private List<ExpandableTree<T>> children = Lists.newArrayList();
    private boolean expanded;

    public ExpandableTree(T value) {
        this.value = value;
    }

    /**
     * @param childIndex The index of the child to return.
     * @return The element at the specified index in this tree.
     */
    public ExpandableTree<T> getChildAt(int childIndex) {
        return this.children.get(childIndex);
    }

    /**
     * @return The amount of this node's children.
     */
    public int getChildCount() {
        return this.children.size();
    }

    /**
     * @return This node's parent, or null if the node is a root.
     */
    public ExpandableTree<T> getParent() {
        return this.parent;
    }

    /**
     * @param node The node the index of which is to be returned.
     * @return The index of the specified node.
     */
    public int getIndex(ExpandableTree<T> node) {
        if (node == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        return this.children.indexOf(node);
    }

    /**
     * @return Whether the node is a leaf (a node with no children).
     */
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    /**
     * @return Whether the node is a root (a node with no parent).
     */
    public boolean isRoot() {
        return this.parent == null;
    }

    /**
     * Adds a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be added.
     * @param child      The child to be added.
     */
    public void addChild(int childIndex, ExpandableTree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        this.children.add(childIndex, child);
        child.setParent(this);
    }

    /**
     * Removes a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be removed.
     */
    public void removeChild(int childIndex) {
        ExpandableTree<T> child = this.children.remove(childIndex);
        child.setParent(null);
    }


    /**
     * Removes a specified child in this tree.
     *
     * @param child The child to be removed.
     */
    public void removeChild(ExpandableTree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }
        if (child.getParent() != this) {
            throw new IllegalArgumentException(NODE_ARGUMENT_INVALID_PARENT);
        }

        this.children.remove(child);
        child.setParent(null);
    }

    /**
     * Sets the parent of the tree to a specific {@code Tree}.
     *
     * @param parent The {@code Tree} the parent of this tree will be set to.
     */
    public void setParent(ExpandableTree<T> parent) {
        this.parent.removeChild(this);
        this.parent = parent;
    }

    /**
     * @return The object stored in this tree.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * @param value The new value of the object stored in this tree.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * @return Whether this tree is expanded.
     */
    public boolean isExpanded() {
        return this.expanded;
    }

    /**
     * @param expanded The new expanded state of this tree.
     */
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
