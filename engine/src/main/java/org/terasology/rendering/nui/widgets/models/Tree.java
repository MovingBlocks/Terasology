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

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A general-purpose tree data structure.
 *
 * @param <T> Type of objects stored in the tree.
 */
public abstract class Tree<T> {
    /**
     * @return The object stored in this tree.
     */
    public abstract T getValue();

    /**
     * @param value The new value of the object stored in this tree.
     */
    public abstract void setValue(T value);

    /**
     * @return Whether this tree is expanded.
     */
    public abstract boolean isExpanded();

    /**
     * @param expanded The new expanded state of this tree.
     */
    public abstract void setExpanded(boolean expanded);

    /**
     * @return Whether the tree is a root (has no parent node).
     */
    public abstract boolean isRoot();

    /**
     * @return Whether the tree is a leaf (has no child nodes).
     */
    public boolean isLeaf() {
        return this.getChildren().isEmpty();
    }

    /**
     * @return This tree's parent.
     */
    public abstract Tree<T> getParent();

    /**
     * @param tree The tree that the parent of this tree is to be set to.
     */
    public abstract void setParent(Tree<T> tree);

    /**
     * @return The list of children for this tree.
     */
    public abstract Collection<Tree<T>> getChildren();

    /**
     * @param tree The tree the index of which is to be returned.
     * @return The index of the specified tree.
     */
    public abstract int getIndex(Tree<T> tree);

    /**
     * @return The root of the tree this subtree is a member of.
     */
    public Tree<T> getRoot() {
        if (this.isRoot()) {
            return this;
        }
        return this.getParent().getRoot();
    }

    /**
     * @return The depth of the tree this tree is a subtree of.
     */
    public int getDepth() {
        return this.getRecursiveDepth(0);
    }

    private int getRecursiveDepth(int currentDepth) {
        if (this.isRoot()) {
            return currentDepth;
        }

        return this.getParent().getRecursiveDepth(currentDepth + 1);
    }

    /**
     * @param child A specified tree.
     * @return Whether the specified tree is a (direct) child of this tree.
     */
    public abstract boolean containsChild(Tree<T> child);

    /**
     * Instantiates and adds a child with a specified value to this tree.
     *
     * @param childValue The value of the child to be added.
     */
    public abstract void addChild(T childValue);

    /**
     * Adds a child to this tree.
     *
     * @param child The child to be added.
     */
    public abstract void addChild(Tree<T> child);

    /**
     * Adds a child to this tree at a specified index.
     *
     * @param index The index of the child to be added.
     * @param child The child to be added.
     */
    public abstract void addChild(int index, Tree<T> child);

    /**
     * Removes a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be removed.
     */
    public abstract void removeChild(int childIndex);

    /**
     * Removes a specified child in this tree.
     *
     * @param child The child to be removed.
     */
    public abstract void removeChild(Tree<T> child);

    /**
     * @return A shallow copy of this tree.
     */
    public abstract Tree<T> copy();


    /**
     * @param enumerateExpandedOnly Whether the children of non-expanded items are excluded from the enumeration.
     * @return The iterator of this tree in depth-first, pre-ordered order.
     */
    public Iterator getDepthFirstIterator(boolean enumerateExpandedOnly) {
        return new DepthFirstIterator(this, enumerateExpandedOnly);
    }

    /**
     * An iterator in depth-first, pre-ordered order.
     */
    private class DepthFirstIterator implements Iterator {
        private static final String ITERATOR_NO_ITEMS = "no elements left (try validating with hasNext?)";

        /**
         * If true, the children of non-expanded items will be excluded from iteration.
         */
        private boolean enumerateExpandedOnly;
        private Tree<T> next;
        private Deque<Enumeration> stack = new ArrayDeque<>();

        DepthFirstIterator(Tree<T> root, boolean enumerateExpandedOnly) {
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
                throw new NoSuchElementException(ITERATOR_NO_ITEMS);
            }

            Tree<T> current = next;
            Enumeration childEnumeration = stack.peek();

            // Retrieve the next item.
            next = traverse(childEnumeration);

            return current;
        }

        private Tree<T> traverse(Enumeration childEnumeration) {
            // Handle the root object being non-expanded.
            if (childEnumeration == null) {
                return null;
            }

            if (childEnumeration.hasMoreElements()) {
                Tree<T> child = (Tree<T>) childEnumeration.nextElement();

                // If the child is expanded, iterate through its' children as well.
                if (!enumerateExpandedOnly || child.isExpanded()) {
                    stack.push(Collections.enumeration(child.getChildren()));
                }

                return child;
            }

            // If a higher level is available, return to it.
            stack.pop();
            if (stack.isEmpty()) {
                return null;
            } else {
                return traverse(stack.peek());
            }
        }
    }
}
