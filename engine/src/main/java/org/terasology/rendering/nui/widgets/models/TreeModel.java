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
import org.terasology.input.MouseInput;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;

import java.util.Iterator;
import java.util.List;

/**
 * @param <T> Type of objects stored in the underlying tree.
 */
public class TreeModel<T> {
    private List<Tree<T>> elements = Lists.newArrayList();
    private List<TreeInteractionListener> listeners = Lists.newArrayList();
    private boolean enumerateExpandedOnly = true;

    public TreeModel() {

    }

    public TreeModel(Tree<T> root) {
        this.resetElements(root);
    }

    private void resetElements(Tree<T> root) {
        this.elements = Lists.newArrayList();
        this.listeners = Lists.newArrayList();

        Iterator it = root.getDepthFirstIterator(enumerateExpandedOnly);

        int i = 0;
        while (it.hasNext()) {
            this.elements.add((Tree<T>) it.next());
            this.listeners.add(new TreeInteractionListener(i));
            i++;
        }
    }

    public Tree<T> getElement(int index) {
        return this.elements.get(index);
    }

    public TreeInteractionListener getListener(int index) {
        return this.listeners.get(index);
    }

    public int getElementCount() {
        return this.elements.size();
    }

    public void setEnumerateExpandedOnly(boolean enumerateExpandedOnly) {
        this.enumerateExpandedOnly = enumerateExpandedOnly;
        this.resetElements(this.elements.get(0).getRoot());
    }

    public class TreeInteractionListener extends BaseInteractionListener {
        private int index;

        public TreeInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                elements.get(index).setExpanded(!elements.get(index).isExpanded());
                resetElements(elements.get(index).getRoot());
                return true;
            }
            return false;
        }
    }
}
