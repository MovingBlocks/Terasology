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
package org.terasology.rendering.nui.widgets.treeView;

import com.google.common.collect.Lists;
import org.terasology.module.sandbox.API;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @param <T> Type of objects stored in the underlying tree.
 */
@SuppressWarnings("unchecked")
@API
public class TreeModel<T> {
    /**
     * A list of nodes, fetched from a {@code Tree} iterator.
     */
    private List<Tree<T>> nodes = Lists.newArrayList();
    /**
     * Whether the children of non-expanded nodes are excluded from the enumeration.
     */
    private boolean enumerateExpandedOnly = true;

    public TreeModel() {

    }

    public TreeModel(Tree<T> root) {
        this.resetNodes(root);
    }

    /**
     * Reset the nodes in the tree.
     */
    public void resetNodes() {
        this.resetNodes(this.nodes.get(0).getRoot());
    }

    /**
     * @param root The tree the list of nodes is to be fetched from.
     */
    private void resetNodes(Tree<T> root) {
        this.nodes = Lists.newArrayList();

        Iterator it = root.getDepthFirstIterator(this.enumerateExpandedOnly);

        while (it.hasNext()) {
            this.nodes.add((Tree<T>) it.next());
        }
    }

    /**
     * @param index The index.
     * @return The node located at a given index.
     */
    public Tree<T> getNode(int index) {
        return this.nodes.get(index);
    }

    /**
     * @param value A node value.
     * @return The node with the given value.
     */
    public Tree<T> getNodeByValue(T value) {
        Optional<Tree<T>> node = this.nodes.stream().filter(n -> n.getValue() == value).findFirst();
        return node.isPresent() ? node.get() : null;
    }

    /**
     * @param node The node.
     * @return The index of the given node.
     */
    public int indexOf(Tree<T> node) {
        return nodes.indexOf(node);
    }

    /**
     * Removes the node located at a given index.
     *
     * @param index The index.
     */
    public void removeNode(int index) {
        Tree<T> item = this.getNode(index);

        // Never remove the root node.
        if (item.isRoot()) {
            return;
        }

        Iterator it = this.nodes.get(0).getRoot().getDepthFirstIterator(this.enumerateExpandedOnly);

        while (it.hasNext()) {
            Tree<T> next = (Tree<T>) it.next();
            if (next.containsChild(item)) {
                next.removeChild(item);
                break;
            }
        }
        this.resetNodes(this.nodes.get(0).getRoot());
    }

    /**
     * @return The amount of nodes in the tree.
     */
    public int getNodeCount() {
        return this.nodes.size();
    }

    /**
     * @param enumerateExpandedOnly Whether the children of non-expanded nodes are excluded from the enumeration.
     */
    public void setEnumerateExpandedOnly(boolean enumerateExpandedOnly) {
        this.enumerateExpandedOnly = enumerateExpandedOnly;
        this.resetNodes();
    }
}
