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
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;

import java.util.List;

public class TreeViewModel<T> {
    private ExpandableTree<T> rootNode;
    private final List<TreeItemInteractionListener> treeItemListeners = Lists.newArrayList();

    public TreeViewModel(ExpandableTree<T> rootNode) {
        this.rootNode = rootNode;
    }

    public ExpandableTree<T> getRootNode() {
        return this.rootNode;
    }

    public void setRootNode(ExpandableTree<T> rootNode) {
        this.rootNode = rootNode;
    }

    public void subscribe(TreeItemInteractionListener eventListener) {
        this.treeItemListeners.add(eventListener);
    }

    public void unsubscribe(TreeItemInteractionListener eventListener) {
        this.treeItemListeners.remove(eventListener);
    }

    private class TreeItemInteractionListener extends BaseInteractionListener {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return false;
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            return false;
        }
    }
}
