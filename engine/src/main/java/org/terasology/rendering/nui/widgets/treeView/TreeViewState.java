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

import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.widgets.UITreeView;

public class TreeViewState<T> {
    /**
     * The index of the currently selected node.
     * <p>
     * {@code null} if no node is selected.
     */
    private Binding<Integer> selectedIndex = new DefaultBinding<>();
    /**
     * The index of the node being drag&dropped.
     * <p>
     * {@code null} if no node is dragged.
     */
    private Binding<Integer> draggedIndex = new DefaultBinding<>();
    /**
     * The index of the node being moused over if another node is currently dragged.
     * <p>
     * {@code null} if no node is either dragged or moused over.
     */
    private Binding<Integer> mouseOverIndex = new DefaultBinding<>();
    /**
     * The index of the node being moused over if another node is currently dragged.
     * <p>
     * {@code null} if no node is either dragged or moused over.
     */
    private Binding<UITreeView.MouseOverType> mouseOverType = new DefaultBinding<>();
    /**
     * The node currently being copied.
     * <p>
     * {@code null} if no node has been copied.
     */
    private Binding<Tree<T>> clipboard = new DefaultBinding<>();
    /**
     * The widget to be drawn in place of a selected item.
     * <p>
     * {@code null} if no alternative widget is to be drawn.
     */
    private Binding<UIWidget> alternativeWidget = new DefaultBinding<>();

    public Integer getSelectedIndex() {
        return selectedIndex.get();
    }

    public void setSelectedIndex(Integer selectedIndex) {
        this.selectedIndex.set(selectedIndex);
    }

    public Integer getDraggedIndex() {
        return draggedIndex.get();
    }

    public void setDraggedIndex(Integer draggedIndex) {
        this.draggedIndex.set(draggedIndex);
    }

    public Integer getMouseOverIndex() {
        return mouseOverIndex.get();
    }

    public void setMouseOverIndex(Integer mouseOverIndex) {
        this.mouseOverIndex.set(mouseOverIndex);
    }

    public UITreeView.MouseOverType getMouseOverType() {
        return mouseOverType.get();
    }

    public void setMouseOverType(UITreeView.MouseOverType mouseOverType) {
        this.mouseOverType.set(mouseOverType);
    }

    public Tree<T> getClipboard() {
        return clipboard.get();
    }

    public void setClipboard(Tree<T> clipboard) {
        this.clipboard.set(clipboard);
    }

    public UIWidget getAlternativeWidget() {
        return alternativeWidget.get();
    }

    public void setAlternativeWidget(UIWidget alternativeWidget) {
        this.alternativeWidget.set(alternativeWidget);
    }
}
