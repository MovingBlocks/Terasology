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

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A general-purpose tree data structure.
 * <p>
 * Stores a single object. May have a single parent and an arbitrary amount of children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public class Tree<T> {
    private static final String NULL_NODE_ARGUMENT = "node argument is null";
    private static final String NODE_ARGUMENT_INVALID_PARENT = "node argument is not a child of this tree";
    private static final String ITERATOR_NO_ELEMENTS = "no elements left (try validating with hasNext?)";

    private T value;
    private Tree<T> parent;
    private List<Tree<T>> children = Lists.newArrayList();
    private boolean expanded;

    public Tree() {
        this(null);
    }

    public Tree(T value) {
        this.value = value;
    }

    /**
     * @return A shallow copy of this node.
     */
    public Tree<T> copy() {
        Tree<T> copy = new Tree<T>(this.value);
        copy.setExpanded(this.expanded);

        for (Tree<T> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }

    /**
     * @return This node's children.
     */
    public List<Tree<T>> getChildren() {
        return this.children;
    }

    /**
     * @return This node's parent, or null if the node is a root.
     */
    public Tree<T> getParent() {
        return this.parent;
    }

    /**
     * @param node The node the index of which is to be returned.
     * @return The index of the specified node.
     */
    public int getIndex(Tree<T> node) {
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
     * @return The root of the tree this tree is a member of.
     */
    public Tree<T> getRoot() {
        if (this.isRoot()) {
            return this;
        }

        return this.parent.getRoot();
    }

    /**
     * @return The depth of this node relative to the root node of its' tree.
     */
    public int getDepth() {
        return this.getRecursiveDepth(0);
    }

    private int getRecursiveDepth(int currentDepth) {
        if (this.isRoot()) {
            return currentDepth;
        }

        return this.parent.getRecursiveDepth(currentDepth + 1);
    }

    /**
     * Adds a child to this tree.
     *
     * @param child The child to be added.
     */
    public void addChild(Tree<T> child) {
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
    public void addChild(int childIndex, Tree<T> child) {
        if (child == null) {
            throw new IllegalArgumentException(NULL_NODE_ARGUMENT);
        }

        this.children.add(childIndex, child);
        child.setParent(this);
    }

    /**
     * @param child A specified node.
     * @return Whether the specified node is a child of this tree.
     */
    public boolean containsChild(Tree<T> child) {
        return this.children.contains(child);
    }

    /**
     * Removes a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be removed.
     */
    public void removeChild(int childIndex) {
        Tree<T> child = this.children.remove(childIndex);
        child.setParent(null);
    }


    /**
     * Removes a specified child in this tree.
     *
     * @param child The child to be removed.
     */
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

    /**
     * Sets the parent of the tree to a specific {@code Tree}.
     *
     * @param parent The {@code Tree} the parent of this tree will be set to.
     */
    public void setParent(Tree<T> parent) {
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
     * @return The iterator of this tree in depth-first, pre-ordered order.
     */
    public Iterator getDepthFirstIterator(boolean enumerateExpandedOnly) {
        return new DepthFirstIterator(this, enumerateExpandedOnly);
    }

    /**
     * An iterator of an {@code ExpandableTree} in depth-first, pre-ordered order.
     */
    private class DepthFirstIterator implements Iterator {
        /**
         * If true, the children of non-expanded elements will be excluded from iteration.
         */
        private boolean enumerateExpandedOnly;
        private Tree<T> next;
        private Deque<Enumeration> stack = new ArrayDeque<>();

        public DepthFirstIterator(Tree root, boolean enumerateExpandedOnly) {
            this.enumerateExpandedOnly = enumerateExpandedOnly;
            this.next = root;

            if (!enumerateExpandedOnly || root.isExpanded()) {
                this.stack.push(Collections.enumeration(root.getChildren()));
            }
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Object next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException(ITERATOR_NO_ELEMENTS);
            }

            Tree<T> current = next;

            Enumeration childEnumeration = stack.peek();

            // Retrieve the next element.
            next = traverse(childEnumeration);

            return current;
        }

        private Tree<T> traverse(Enumeration childEnumeration) {
            // Handle the root object being non-expanded
            if (childEnumeration == null) {
                return null;
            }

            if (childEnumeration.hasMoreElements()) {
                Tree<T> child = (Tree<T>) childEnumeration.nextElement();

                // If the child is expanded, iterate through its' children as well
                if (!enumerateExpandedOnly || child.isExpanded()) {
                    stack.push(Collections.enumeration(child.getChildren()));
                }

                return child;
            }

            // If a higher level is available, return to it
            stack.pop();
            if (stack.isEmpty()) {
                return null;
            } else {
                return traverse(stack.peek());
            }
        }
    }
}
