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

import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

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
     * @return This node's children.
     */
    public List<ExpandableTree<T>> getChildren() {
        return this.children;
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
     * Adds a child to this tree.
     *
     * @param child The child to be added.
     */
    public void addChild(ExpandableTree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        this.children.add(child);
        child.setParent(this);
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

    /**
     * @param enumerateExpandedOnly Whether the children of non-expanded elements are excluded from the enumeration.
     * @return The iterator of this tree in breadth-first order.
     */
    public Iterator getBreadthFirstIterator(boolean enumerateExpandedOnly) {
        return new BreadthFirstIterator(this, enumerateExpandedOnly);
    }

    public Iterator getBreadthFirstIterator() {
        return this.getBreadthFirstIterator(false);
    }

    /**
     * An iterator of an {@code ExpandableTree} in breadth-first order.
     */
    private class BreadthFirstIterator implements Iterator {
        /**
         * If true, the children of non-expanded elements will be excluded from iteration.
         */
        private boolean enumerateExpandedOnly;
        private Queue<ExpandableTree> queue = new LinkedList<>();

        public BreadthFirstIterator(ExpandableTree root, boolean enumerateExpandedOnly) {
            this.queue.add(root);
            this.enumerateExpandedOnly = enumerateExpandedOnly;
        }

        @Override
        public boolean hasNext() {
            return !this.queue.isEmpty();
        }

        @Override
        public Object next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("No elements left in the queue (try validating with hasMoreElements?)");
            }
            ExpandableTree nextElement = this.queue.remove();

            if (!this.enumerateExpandedOnly || nextElement.isExpanded()) {
                Enumeration childEnumeration = Collections.enumeration(nextElement.getChildren());

                while (childEnumeration.hasMoreElements()) {
                    this.queue.add((ExpandableTree) childEnumeration.nextElement());
                }
            }

            return nextElement;
        }
    }
}
