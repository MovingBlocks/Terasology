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

import com.google.api.client.util.Lists;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.List;

/**
 * A general purpose, data agnostic implementation of {@link Tree}.
 * <p>
 * Stores a single object. May have a single parent and an arbitrary amount of children.
 *
 * @param <T> Type of objects stored in the tree.
 */
public class GenericTree<T> extends Tree<T> {
    private static final String NULL_NODE_ARGUMENT = "node argument is null";
    private static final String NODE_ARGUMENT_INVALID_PARENT = "node argument is not a child of this tree";

    /**
     * The object stored in this tree.
     */
    private T value;
    /**
     * Whether the tree is expanded.
     */
    private boolean expanded;
    /**
     * The parent of this tree.
     */
    private Tree<T> parent;
    /**
     * The children of this tree.
     */
    private List<Tree<T>> children = Lists.newArrayList();

    public GenericTree(T value) {
        this.setValue(value);
    }

    @Override
    public T getValue() {
        return this.value;
    }

    @Override
    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean isExpanded() {
        return this.expanded;
    }

    @Override
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public boolean isRoot() {
        return this.parent == null;
    }

    @Override
    public Tree<T> getParent() {
        return this.parent;
    }

    @Override
    public void setParent(Tree<T> tree) {
        this.parent = tree;
    }

    @Override
    public Collection<Tree<T>> getChildren() {
        return this.children;
    }

    @Override
    public int getIndex(Tree<T> tree) {
        Preconditions.checkNotNull(tree, NULL_NODE_ARGUMENT);
        return this.children.indexOf(tree);
    }

    @Override
    public boolean containsChild(Tree<T> child) {
        return this.children.contains(child);
    }

    @Override
    public void addChild(T childValue) {
        this.addChild(new GenericTree<T>(childValue));
    }

    @Override
    public void addChild(Tree<T> child) {
        Preconditions.checkNotNull(child, NULL_NODE_ARGUMENT);

        this.children.add(child);
        child.setParent(this);
    }

    @Override
    public void addChild(int index, Tree<T> child) {
        Preconditions.checkNotNull(child, NULL_NODE_ARGUMENT);

        this.children.add(index, child);
        child.setParent(this);
    }

    @Override
    public void removeChild(int childIndex) {
        Tree<T> child = this.children.remove(childIndex);
        child.setParent(null);
    }

    @Override
    public void removeChild(Tree<T> child) {
        Preconditions.checkNotNull(child, NULL_NODE_ARGUMENT);
        Preconditions.checkState(child.getParent() == this, NODE_ARGUMENT_INVALID_PARENT);

        this.children.remove(child);
        child.setParent(null);
    }

    @Override
    public Tree<T> copy() {
        GenericTree<T> copy = new GenericTree<>(this.value);
        copy.setExpanded(this.expanded);

        for (Tree<T> child : this.children) {
            copy.addChild(child.copy());
        }
        return copy;
    }
}