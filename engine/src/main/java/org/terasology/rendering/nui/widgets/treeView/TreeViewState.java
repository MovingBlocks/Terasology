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

import com.google.common.collect.Maps;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.widgets.UITreeView;

import java.util.Map;

public class TreeViewState<T> {
    /**
     * The index of the currently selected item, or null if no item is selected.
     */
    private Binding<Integer> selectedIndex = new DefaultBinding<>();
    /**
     * The index of the item being drag&dropped, or null if no item is dragged.
     */
    private Binding<Integer> draggedItemIndex = new DefaultBinding<>();
    /**
     * The index of the item being moused over if another item is currently dragged.
     * Null if no item is either dragged or moused over.
     */
    private Binding<Integer> mouseOverItemIndex = new DefaultBinding<>();
    /**
     * The index of the item being moused over if another item is currently dragged.
     * Null if no item is either dragged or moused over.
     */
    private Binding<UITreeView.MouseOverItemType> mouseOverItemType = new DefaultBinding<>();
    /**
     * The item currently being copied, or null if no item has been copied.
     */
    private Binding<Tree<T>> clipboard = new DefaultBinding<>();
    /**
     * A map containing an item index as key and the widget to be drawn in place of the
     * specified item as value.
     */
    private Map<Integer, UIWidget> alternativeWidgets = Maps.newHashMap();

    public Integer getSelectedIndex() {
        return selectedIndex.get();
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex.set(selectedIndex);
    }

    public Integer getDraggedItemIndex() {
        return draggedItemIndex.get();
    }

    public void setDraggedItemIndex(Integer draggedItemIndex) {
        this.draggedItemIndex.set(draggedItemIndex);
    }

    public Integer getMouseOverItemIndex() {
        return mouseOverItemIndex.get();
    }

    public void setMouseOverItemIndex(Integer mouseOverItemIndex) {
        this.mouseOverItemIndex.set(mouseOverItemIndex);
    }

    public UITreeView.MouseOverItemType getMouseOverItemType() {
        return mouseOverItemType.get();
    }

    public void setMouseOverItemType(UITreeView.MouseOverItemType mouseOverItemType) {
        this.mouseOverItemType.set(mouseOverItemType);
    }

    public Tree<T> getClipboard() {
        return clipboard.get();
    }

    public void setClipboard(Tree<T> clipboard) {
        this.clipboard.set(clipboard);
    }

    public Map<Integer, UIWidget> getAlternativeWidgets() {
        return alternativeWidgets;
    }

    public void setAlternativeWidgets(Map<Integer, UIWidget> alternativeWidgets) {
        this.alternativeWidgets = alternativeWidgets;
    }
}
