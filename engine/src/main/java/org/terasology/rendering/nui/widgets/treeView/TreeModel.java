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

import com.google.api.client.util.Lists;

import java.util.Iterator;
import java.util.List;

/**
 * @param <T> Type of objects stored in the underlying tree.
 */
public class TreeModel<T> {
    /**
     * A list of items, fetched from a {@code Tree} iterator.
     */
    private List<Tree<T>> items = Lists.newArrayList();
    /**
     * Whether the children of non-expanded items are excluded from the enumeration.
     */
    private boolean enumerateExpandedOnly = true;

    public TreeModel() {

    }

    public TreeModel(Tree<T> root) {
        this.resetItems(root);
    }

    /**
     * Reset the items in the tree.
     */
    public void resetItems() {
        this.resetItems(this.items.get(0).getRoot());
    }

    /**
     * @param root The tree the list of items is to be fetched from.
     */
    private void resetItems(Tree<T> root) {
        this.items = Lists.newArrayList();

        Iterator it = root.getDepthFirstIterator(enumerateExpandedOnly);

        while (it.hasNext()) {
            this.items.add((Tree<T>) it.next());
        }
    }

    /**
     * @param index The index.
     * @return The item located at a given index.
     */
    public Tree<T> getItem(int index) {
        return this.items.get(index);
    }

    /**
     * @param item The item.
     * @return The index of the given item.
     */
    public int indexOf(Tree<T> item) {
        return items.indexOf(item);
    }

    /**
     * Removes the item located at a given index.
     *
     * @param index The index.
     */
    public void removeItem(int index) {
        Tree<T> item = this.getItem(index);

        // Never remove the root node.
        if (item.isRoot()) {
            return;
        }

        Iterator it = this.items.get(0).getRoot().getDepthFirstIterator(enumerateExpandedOnly);

        while (it.hasNext()) {
            Tree<T> next = (Tree<T>) it.next();
            if (next.containsChild(item)) {
                next.removeChild(item);
                break;
            }
        }
        this.resetItems(this.items.get(0).getRoot());
    }

    /**
     * @return The amount of items in the tree.
     */
    public int getItemCount() {
        return this.items.size();
    }

    /**
     * @param enumerateExpandedOnly Whether the children of non-expanded items are excluded from the enumeration.
     */
    public void setEnumerateExpandedOnly(boolean enumerateExpandedOnly) {
        this.enumerateExpandedOnly = enumerateExpandedOnly;
        this.resetItems();
    }
}
