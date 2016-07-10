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
package org.terasology.rendering.nui.widgets;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.models.Tree;
import org.terasology.rendering.nui.widgets.models.TreeModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A Tree View widget. Presents a hierarchical view of items, visualised by indentation.
 *
 * @param <T> Type of objects stored in the underlying tree.
 */
public class UITreeView<T> extends CoreWidget {
    private enum MouseOverItemType {
        TOP,
        CENTER,
        BOTTOM
    }

    // Canvas parts.
    private static final String EXPAND_BUTTON = "expand-button";
    private static final String TREE_ITEM = "tree-item";

    // Canvas modes.
    private static final String CONTRACT_MODE = "contract";
    private static final String CONTRACT_HOVER_MODE = "contract-hover";
    private static final String EXPAND_MODE = "expand";
    private static final String EXPAND_HOVER_MODE = "expand-hover";
    private static final String HOVER_DISABLED_MODE = "hover-disabled";
    private static final String SELECTED_MODE = "selected";

    /**
     * The indentation of one level in the tree.
     */
    private Binding<Integer> itemIndent = new DefaultBinding<>(25);
    /**
     * The underlying tree model - a wrapper around a {@code Tree<T>}.
     */
    private Binding<TreeModel<T>> model = new DefaultBinding<>(new TreeModel<>());
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
    private Binding<MouseOverItemType> mouseOverItemType = new DefaultBinding<>();
    /**
     * The item currently being copied, or null if no item has been copied.
     */
    private Binding<Tree<T>> clipboard = new DefaultBinding<>();
    /**
     * The value to be used when adding nodes to the tree.
     */
    private Binding<T> defaultValue = new DefaultBinding<>();
    /**
     * The item renderer used for drawing the items in the tree.
     */
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();
    /**
     * The individual item listeners.
     */
    private final List<TreeViewListenerSet> treeViewListenerSets = Lists.newArrayList();
    /**
     * The individual expand/contract button listeners.
     */
    private final List<ExpandButtonInteractionListener> expandListeners = Lists.newArrayList();
    /**
     * Tree view item update listeners.
     */
    private List<UpdateListener> updateListeners = Lists.newArrayList();
    /**
     * Tree view item click listeners.
     */
    private List<TreeMouseClickListener> itemListeners = Lists.newArrayList();
    /**
     *
     */
    private Map<Integer, UIWidget> alternativeWidgets = Maps.newHashMap();

    private class ExpandButtonInteractionListener extends BaseInteractionListener {
        private int index;

        ExpandButtonInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                // Expand or contract and item on LMB - works even if the tree is disabled.
                model.get().getItem(index).setExpanded(!model.get().getItem(index).isExpanded());

                Tree<T> selectedItem = selectedIndex.get() != null ? model.get().getItem(selectedIndex.get()) : null;
                model.get().resetItems();

                // Update the index of the selected item.
                if (selectedItem != null) {
                    int newIndex = model.get().indexOf(selectedItem);
                    if (newIndex == -1) {
                        selectedIndex.set(null);
                    } else {
                        selectedIndex.set(newIndex);
                    }
                }
                return true;
            }
            return false;
        }
    }

    private class ItemTopListener extends BaseInteractionListener {
        private int index;

        ItemTopListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onItemMouseClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onItemMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex.get() != null
                    && !model.get().getItem(index).isRoot()
                    && model.get().getItem(index).getParent().acceptsChild(model.get().getItem(draggedItemIndex.get()))) {
                onItemMouseOver(index, MouseOverItemType.TOP);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease(index);
        }
    }

    private class ItemCenterListener extends BaseInteractionListener {
        private int index;

        ItemCenterListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onItemMouseClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onItemMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex.get() != null
                    && model.get().getItem(index).acceptsChild(model.get().getItem(draggedItemIndex.get()))) {
                onItemMouseOver(index, MouseOverItemType.CENTER);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease(index);
        }
    }

    private class ItemBottomListener extends BaseInteractionListener {
        private int index;

        ItemBottomListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return onItemMouseClick(index, event);
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            onItemMouseDrag(index);
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex.get() != null
                    && !model.get().getItem(index).isRoot()
                    && model.get().getItem(index).getParent().acceptsChild(model.get().getItem(draggedItemIndex.get()))) {
                onItemMouseOver(index, MouseOverItemType.BOTTOM);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease(index);
        }
    }

    private boolean onItemMouseClick(int index, NUIMouseClickEvent event) {
        for (TreeMouseClickListener listener : itemListeners) {
            listener.onMouseClick(event, model.get().getItem(index));
        }
        if (isEnabled() && event.getMouseButton() == MouseInput.MOUSE_LEFT) {
            // Select the item on LMB - deselect when selected again.
            if (selectedIndex.get() != null && selectedIndex.get().equals(index)) {
                selectedIndex.set(null);
            } else {
                selectedIndex.set(index);
            }
            clearAlternativeWidgets();
            return true;
        }
        return false;
    }

    private void onItemMouseDrag(int index) {
        draggedItemIndex.set(index);
    }

    private void onItemMouseOver(int index, MouseOverItemType type) {
        // Set temporary index variables for the dragged/target items.
        if (draggedItemIndex.get() != null) {
            if (draggedItemIndex.get() != index) {
                mouseOverItemIndex.set(index);
                mouseOverItemType.set(type);
            } else {
                mouseOverItemIndex.set(null);
                mouseOverItemType.set(null);
            }
        }
    }

    private void onItemMouseRelease(int index) {
        if (draggedItemIndex.get() != null && mouseOverItemIndex.get() != null) {
            Tree<T> child = model.get().getItem(draggedItemIndex.get());
            Tree<T> parent = model.get().getItem(mouseOverItemIndex.get());

            // Handle item drag&dropping.
            if (mouseOverItemType.get() == MouseOverItemType.TOP) {
                // Insert the dragged item before the target item (as a child of the same tree).
                child.getParent().removeChild(child);
                parent.getParent().addChild(parent.getParent().indexOf(parent), child);
            } else if (mouseOverItemType.get() == MouseOverItemType.CENTER) {
                // Insert the dragged item as a child of the target item.
                child.getParent().removeChild(child);
                parent.addChild(child);
            } else {
                // Insert the dragged item after the target item (as a child of the same tree).
                child.getParent().removeChild(child);
                parent.getParent().addChild(parent.getParent().indexOf(parent) + 1, child);
            }

            fireUpdateListeners();
        }

        // Reset the temporary index variables.
        if (draggedItemIndex.get() != null) {
            if (draggedItemIndex.get() != index) {
                selectedIndex.set(null);
            }
            draggedItemIndex.set(null);
        }
        if (mouseOverItemIndex.get() != null) {
            mouseOverItemIndex.set(null);
            mouseOverItemType.set(null);
        }
    }

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateListeners();

        canvas.setPart(TREE_ITEM);

        int currentHeight = 0;
        for (int i = 0; i < model.get().getItemCount(); i++) {
            Tree<T> item = model.get().getItem(i);
            TreeViewListenerSet treeViewListenerSet = treeViewListenerSets.get(i);
            ExpandButtonInteractionListener buttonListener = expandListeners.get(i);

            int itemHeight = canvas.getCurrentStyle().getMargin()
                    .grow(itemRenderer.getPreferredSize(item.getValue(), canvas).addX(item.getDepth() * itemIndent.get()))
                    .getY();

            Rect2i itemRegion = Rect2i.createFromMinAndSize((item.getDepth() + 1) * itemIndent.get(),
                    currentHeight,
                    canvas.size().x - (item.getDepth() + 1) * itemIndent.get(),
                    itemHeight);

            // Draw the expand/contract button.
            if (!item.isLeaf()) {
                canvas.setPart(EXPAND_BUTTON);

                setButtonMode(canvas, item, buttonListener);
                Rect2i buttonRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                        currentHeight,
                        itemIndent.get(),
                        itemHeight);
                drawButton(canvas, buttonRegion, buttonListener);

                canvas.setPart(TREE_ITEM);
            }

            if (alternativeWidgets.containsKey(i)) {
                canvas.drawWidget(alternativeWidgets.get(i), itemRegion);
                currentHeight += itemHeight;
            } else {
                // Draw the item itself.
                setItemMode(canvas, item, treeViewListenerSet);

                drawItem(canvas, itemRegion, item, treeViewListenerSet);
                currentHeight += itemHeight;

                // Draw the item dragging hints if the current item is a drag&drop target.
                if (mouseOverItemIndex.get() != null
                        && mouseOverItemIndex.get() == i) {
                    drawDragHint(canvas, itemRegion);
                }
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE_ITEM);

        if (model.get().getItemCount() == 0) {
            return new Vector2i();
        }

        model.get().setEnumerateExpandedOnly(false);
        Vector2i result = new Vector2i();
        for (int i = 0; i < model.get().getItemCount(); i++) {
            Tree<T> item = model.get().getItem(i);
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin()
                    .grow(itemRenderer.getPreferredSize(item.getValue(), canvas).addX(item.getDepth() * itemIndent.get()));
            result.x = Math.max(result.x, preferredSize.x);
            result.y += preferredSize.y;
        }
        model.get().setEnumerateExpandedOnly(true);

        // Account for the expand/contract button.
        result.addX(itemIndent.get());

        return result;
    }


    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();
            KeyboardDevice keyboard = event.getKeyboard();
            boolean ctrlDown = keyboard.isKeyDown(Keyboard.KeyId.RIGHT_CTRL) || keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL);

            if (id == Keyboard.KeyId.UP || id == Keyboard.KeyId.DOWN) {
                // Up/Down: change a node's position within the parent node.
                moveSelected(id);
                return true;
            } else if (id == Keyboard.KeyId.DELETE) {
                // Delete: remove a node (and all its' children).
                removeSelected();
                return true;
            } else if ((ctrlDown && id == Keyboard.KeyId.A) || id == Keyboard.KeyId.INSERT) {
                // Ctrl+A / Insert: add a new child with a placeholder value to the currently selected node.
                addToSelected();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.C) {
                // Ctrl+C: copy a selected node.
                copy(model.get().getItem(selectedIndex.get()));
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.V) {
                // Ctrl+V: paste the copied node as a child of the currently selected node.
                paste(model.get().getItem(selectedIndex.get()));
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public void fireUpdateListeners() {
        clearAlternativeWidgets();
        updateListeners.forEach(UpdateListener::onAction);
    }

    private void moveSelected(int keyId) {
        if (selectedIndex.get() != null) {
            Tree<T> selectedItem = model.get().getItem(selectedIndex.get());
            Tree<T> parent = selectedItem.getParent();

            if (!selectedItem.isRoot()) {
                int itemIndex = parent.getIndex(selectedItem);

                if (keyId == Keyboard.KeyId.UP && itemIndex > 0) {
                    // Move the item up, unless it is the first item.
                    parent.removeChild(selectedItem);
                    parent.addChild(itemIndex - 1, selectedItem);
                    model.get().resetItems();

                    // Re-select the moved item.
                    selectedIndex.set(model.get().indexOf(selectedItem));

                    fireUpdateListeners();
                } else if (keyId == Keyboard.KeyId.DOWN && itemIndex < parent.getChildren().size() - 1) {
                    // Move the item down, unless it is the last item.
                    parent.removeChild(selectedItem);
                    parent.addChild(itemIndex + 1, selectedItem);
                    model.get().resetItems();

                    // Re-select the moved item.
                    selectedIndex.set(model.get().indexOf(selectedItem));

                    fireUpdateListeners();
                }
            }
        }
    }

    private void removeSelected() {
        model.get().removeItem(selectedIndex.get());
        selectedIndex.set(null);

        fireUpdateListeners();
    }

    private void addToSelected() {
        model.get().getItem(selectedIndex.get()).addChild(defaultValue.get());

        fireUpdateListeners();
    }

    public void copy(Tree<T> item) {
        clipboard.set(item.copy());
    }

    public void paste(Tree<T> item) {
        if (clipboard.get() != null) {
            item.addChild(clipboard.get());

            fireUpdateListeners();
        }
    }

    private void setButtonMode(Canvas canvas, Tree<T> item, ExpandButtonInteractionListener listener) {
        if (listener.isMouseOver()) {
            canvas.setMode(item.isExpanded() ? CONTRACT_HOVER_MODE : EXPAND_HOVER_MODE);
        } else {
            canvas.setMode(item.isExpanded() ? CONTRACT_MODE : EXPAND_MODE);
        }
    }

    private void setItemMode(Canvas canvas, Tree<T> item, TreeViewListenerSet listenerSet) {
        if (item.isSelected()) {
            canvas.setMode(SELECTED_MODE);
        } else if (selectedIndex.get() != null && Objects.equals(item, model.get().getItem(selectedIndex.get()))) {
            canvas.setMode(ACTIVE_MODE);
        } else if (listenerSet.isMouseOver()) {
            canvas.setMode(isEnabled() ? HOVER_MODE : HOVER_DISABLED_MODE);
        } else if (!isEnabled()) {
            canvas.setMode(DISABLED_MODE);
        } else {
            canvas.setMode(DEFAULT_MODE);
        }
    }

    private void drawButton(Canvas canvas, Rect2i buttonRegion, ExpandButtonInteractionListener listener) {
        canvas.drawBackground(buttonRegion);
        canvas.addInteractionRegion(listener, buttonRegion);
    }

    private void drawItem(Canvas canvas, Rect2i itemRegion, Tree<T> item, TreeViewListenerSet listenerSet) {
        canvas.drawBackground(itemRegion);
        itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));

        // Add the top listener.
        canvas.addInteractionRegion(listenerSet.getTopListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY(),
                        itemRegion.width(), itemRegion.height() / 3));

        // Add the central listener.
        canvas.addInteractionRegion(listenerSet.getCenterListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY() + itemRegion.height() / 3,
                        itemRegion.width(), itemRegion.height() / 3));

        int heightOffset = itemRegion.height() - 3 * (itemRegion.height() / 3);

        // Add the bottom listener.
        canvas.addInteractionRegion(listenerSet.getBottomListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY() + 2 * itemRegion.height() / 3,
                        itemRegion.width(), heightOffset + itemRegion.height() / 3));
    }

    private void drawDragHint(Canvas canvas, Rect2i itemRegion) {
        if (mouseOverItemType.get() == MouseOverItemType.TOP) {
            // Draw a line at the top of the item.
            canvas.drawLine(itemRegion.minX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.minY(), Color.WHITE);
        } else if (mouseOverItemType.get() == MouseOverItemType.CENTER) {
            // Draw a border around the item.
            canvas.drawLine(itemRegion.minX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.minY(), Color.WHITE);
            canvas.drawLine(itemRegion.maxX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.maxY(), Color.WHITE);
            canvas.drawLine(itemRegion.minX(), itemRegion.minY(), itemRegion.minX(), itemRegion.maxY(), Color.WHITE);
            canvas.drawLine(itemRegion.minX(), itemRegion.maxY(), itemRegion.maxX(), itemRegion.maxY(), Color.WHITE);
        } else if (mouseOverItemType.get() == MouseOverItemType.BOTTOM) {
            // Draw a line at the bottom of the item.
            canvas.drawLine(itemRegion.minX(), itemRegion.maxY(), itemRegion.maxX(), itemRegion.maxY(), Color.WHITE);
        }
    }

    private void updateListeners() {
        boolean mouseOver = false;
        for (TreeViewListenerSet set : treeViewListenerSets) {
            if (set.isMouseOver()) {
                mouseOver = true;
                break;
            }
        }
        if (!mouseOver) {
            // Reset the temporary index variables.
            if (draggedItemIndex.get() != null) {
                draggedItemIndex.set(null);
            }
            if (mouseOverItemIndex.get() != null) {
                mouseOverItemIndex.set(null);
                mouseOverItemType.set(null);
            }
        }

        // Update the listener sets.
        while (treeViewListenerSets.size() > model.get().getItemCount()) {
            treeViewListenerSets.remove(treeViewListenerSets.size() - 1);
            expandListeners.remove(expandListeners.size() - 1);
        }
        while (treeViewListenerSets.size() < model.get().getItemCount()) {
            treeViewListenerSets.add(new TreeViewListenerSet(
                    new ItemTopListener(treeViewListenerSets.size()),
                    new ItemCenterListener(treeViewListenerSets.size()),
                    new ItemBottomListener(treeViewListenerSets.size())));
            expandListeners.add(new ExpandButtonInteractionListener(expandListeners.size()));
        }
    }

    public TreeModel<T> getModel() {
        return model.get();
    }

    public void setModel(Tree<T> root) {
        setModel(new TreeModel<>(root));
    }

    public void setModel(TreeModel<T> newModel) {
        model.set(newModel);
        alternativeWidgets.clear();
        selectedIndex.set(null);
    }

    public void setDefaultValue(T value) {
        defaultValue.set(value);
    }

    public void subscribeTreeViewUpdate(UpdateListener listener) {
        Preconditions.checkNotNull(listener);
        updateListeners.add(listener);
    }

    public void subscribeItemMouseClick(TreeMouseClickListener listener) {
        Preconditions.checkNotNull(listener);
        itemListeners.add(listener);
    }

    public void addAlternativeWidget(int index, UIWidget widget) {
        alternativeWidgets.put(index, widget);
    }

    public void clearAlternativeWidgets() {
        alternativeWidgets.clear();
    }

    /**
     * A set of tree element sub-listeners.
     */
    private class TreeViewListenerSet {
        /**
         * The top listener.
         */
        private ItemTopListener topListener;
        /**
         * The central listener.
         */
        private ItemCenterListener centerListener;
        /**
         * The bottom listener.
         */
        private ItemBottomListener bottomListener;

        private TreeViewListenerSet(ItemTopListener topListener, ItemCenterListener centerListener, ItemBottomListener bottomListener) {
            this.topListener = topListener;
            this.centerListener = centerListener;
            this.bottomListener = bottomListener;
        }

        /**
         * @return The top listener.
         */
        public ItemTopListener getTopListener() {
            return topListener;
        }

        /**
         * @return The central listener.
         */
        public ItemCenterListener getCenterListener() {
            return centerListener;
        }

        /**
         * @return The bottom listener.
         */
        public ItemBottomListener getBottomListener() {
            return bottomListener;
        }

        /**
         * @return Whether any of the listeners are currently moused over.
         */
        public boolean isMouseOver() {
            return topListener.isMouseOver() || centerListener.isMouseOver() || bottomListener.isMouseOver();
        }
    }
}