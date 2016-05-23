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

/**
 * A general-purpose tree data structure.
 * <p>
 * Stores a single object. May have a single parent and an arbitrary amount of children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public interface Tree<T> {
    /**
     * @param childIndex The index of the child to return.
     * @return The element at the specified index in this tree.
     */
    Tree<T> getChildAt(int childIndex);

    /**
     * @return The amount of this node's children.
     */
    int getChildCount();

    /**
     * @return This node's parent, or null if the node is a root.
     */
    Tree<T> getParent();

    /**
     * @param node The node the index of which is to be returned.
     * @return The index of the specified node.
     */
    int getIndex(Tree<T> node);

    /**
     * @return Whether the node is a leaf (a node with no children).
     */
    boolean isLeaf();

    /**
     * @return Whether the node is a root (a node with no parent).
     */
    boolean isRoot();

    /**
     * Adds a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be added.
     * @param child      The child to be added.
     */
    void addChild(int childIndex, Tree<T> child);

    /**
     * Removes a child at the specified index in this tree.
     *
     * @param childIndex The index of the child to be removed.
     */
    void removeChild(int childIndex);

    /**
     * Removes a specified child in this tree.
     *
     * @param child The child to be removed.
     */
    void removeChild(Tree<T> child);

    /**
     * Sets the parent of the tree to a specific {@code Tree}.
     *
     * @param parent The {@code Tree} the parent of this tree will be set to.
     */
    void setParent(Tree<T> parent);

    /**
     * @return The object stored in this tree.
     */
    T getValue();

    /**
     * @param value The new value of the object stored in this tree.
     */
    void setValue(T value);
}
