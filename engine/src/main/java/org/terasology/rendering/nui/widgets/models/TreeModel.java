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

import java.util.Iterator;
import java.util.List;

/**
 * @param <T> Type of objects stored in the underlying tree.
 */
public class TreeModel<T> {
    private List<Tree<T>> elements = Lists.newArrayList();
    private boolean enumerateExpandedOnly = true;

    public TreeModel() {

    }

    public TreeModel(Tree<T> root) {
        this.resetElements(root);
    }

    public void resetElements(Tree<T> root) {
        this.elements = Lists.newArrayList();

        Iterator it = root.getDepthFirstIterator(enumerateExpandedOnly);

        while (it.hasNext()) {
            this.elements.add((Tree<T>) it.next());
        }
    }

    public Tree<T> getElement(int index) {
        return this.elements.get(index);
    }

    public void removeElement(int index) {
        Tree<T> element = this.getElement(index);

        // Never remove the root node
        if (element.isRoot()) {
            return;
        }

        Iterator it = this.elements.get(0).getRoot().getDepthFirstIterator(enumerateExpandedOnly);

        while (it.hasNext()) {
            Tree<T> next = (Tree<T>) it.next();
            if (next.containsChild(element)) {
                next.removeChild(element);
                break;
            }
        }
        this.resetElements(this.elements.get(0).getRoot());
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public void setEnumerateExpandedOnly(boolean enumerateExpandedOnly) {
        this.enumerateExpandedOnly = enumerateExpandedOnly;
        this.resetElements(this.elements.get(0).getRoot());
    }
}
