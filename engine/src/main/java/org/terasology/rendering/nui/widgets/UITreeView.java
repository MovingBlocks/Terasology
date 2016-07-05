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
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.device.KeyboardDevice;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
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

    ;

    private static final String EXPAND_BUTTON = "expand-button";
    private static final String TREE_ITEM = "tree-item";

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
    private final List<ItemListenerSet> itemListenerSets = Lists.newArrayList();
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
    private Integer draggedItemIndex;
    /**
     *
     */
    private Integer mouseOverItemIndex;
    /**
     *
     */
    private MouseOverItemType mouseOverItemType;

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
            draggedItemIndex = index;
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex != null) {
                if (mouseOverItemIndex != null) {
                    model.get().getItem(mouseOverItemIndex).setSelected(false);
                }
                if (draggedItemIndex != index) {
                    mouseOverItemIndex = index;
                    model.get().getItem(index).setSelected(true);
                    mouseOverItemType = MouseOverItemType.TOP;
                }
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease();
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
            draggedItemIndex = index;
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex != null) {
                if (mouseOverItemIndex != null) {
                    model.get().getItem(mouseOverItemIndex).setSelected(false);
                }
                if (draggedItemIndex != index) {
                    mouseOverItemIndex = index;
                    model.get().getItem(index).setSelected(true);
                    mouseOverItemType = MouseOverItemType.CENTER;
                }
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease();
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
            draggedItemIndex = index;
        }

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            if (draggedItemIndex != null) {
                if (mouseOverItemIndex != null) {
                    model.get().getItem(mouseOverItemIndex).setSelected(false);
                }
                if (draggedItemIndex != index) {
                    mouseOverItemIndex = index;
                    model.get().getItem(index).setSelected(true);
                    mouseOverItemType = MouseOverItemType.BOTTOM;
                }
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            onItemMouseRelease();
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
            return true;
        }
        return false;
    }

    private void onItemMouseRelease() {
        if (draggedItemIndex != null) {
            selectedIndex.set(null);
            draggedItemIndex = null;
        }
        if (mouseOverItemIndex != null) {
            model.get().getItem(mouseOverItemIndex).setSelected(false);
            mouseOverItemIndex = null;
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
            ItemListenerSet itemListenerSet = itemListenerSets.get(i);
            ExpandButtonInteractionListener buttonListener = expandListeners.get(i);

            int itemHeight = canvas.getCurrentStyle().getMargin()
                    .grow(itemRenderer.getPreferredSize(item.getValue(), canvas).addX(item.getDepth() * itemIndent.get()))
                    .getY();

            if (!item.isLeaf()) {
                canvas.setPart(EXPAND_BUTTON);
                setButtonMode(canvas, item, buttonListener);
                Rect2i buttonRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                        currentHeight,
                        itemIndent.get(),
                        itemHeight);
                drawButton(canvas, buttonRegion, buttonListener);
            }

            canvas.setPart(TREE_ITEM);
            setItemMode(canvas, item, itemListenerSet);
            Rect2i itemRegion = Rect2i.createFromMinAndSize((item.getDepth() + 1) * itemIndent.get(),
                    currentHeight,
                    canvas.size().x - (item.getDepth() + 1) * itemIndent.get(),
                    itemHeight);
            drawItem(canvas, itemRegion, item, itemListenerSet);
            currentHeight += itemHeight;

            if (mouseOverItemIndex != null
                    && mouseOverItemIndex == i) {
                drawDragHint(canvas, itemRegion);
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
                fireUpdateListeners();
                return true;
            } else if (id == Keyboard.KeyId.DELETE) {
                // Delete: remove a node (and all its' children).
                removeSelected();
                fireUpdateListeners();
                return true;
            } else if ((ctrlDown && id == Keyboard.KeyId.A) || id == Keyboard.KeyId.INSERT) {
                // Ctrl+A / Insert: add a new child with a placeholder value to the currently selected node.
                addToSelected();
                fireUpdateListeners();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.C) {
                // Ctrl+C: copy a selected node.
                copy(model.get().getItem(selectedIndex.get()));
                fireUpdateListeners();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.V) {
                // Ctrl+V: paste the copied node as a child of the currently selected node.
                paste(model.get().getItem(selectedIndex.get()));
                fireUpdateListeners();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private void fireUpdateListeners() {
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
                } else if (keyId == Keyboard.KeyId.DOWN && itemIndex < parent.getChildren().size() - 1) {
                    // Move the item down, unless it is the last item.
                    parent.removeChild(selectedItem);
                    parent.addChild(itemIndex + 1, selectedItem);
                    model.get().resetItems();

                    // Re-select the moved item.
                    selectedIndex.set(model.get().indexOf(selectedItem));
                }
            }
        }
    }

    private void removeSelected() {
        model.get().removeItem(selectedIndex.get());
        selectedIndex.set(null);
    }

    private void addToSelected() {
        model.get().getItem(selectedIndex.get()).addChild(defaultValue.get());
    }

    public void copy(Tree<T> item) {
        clipboard.set(item.copy());
    }

    public void paste(Tree<T> item) {
        if (clipboard.get() != null) {
            item.addChild(clipboard.get());
        }
    }

    private void setButtonMode(Canvas canvas, Tree<T> item, ExpandButtonInteractionListener listener) {
        if (listener.isMouseOver()) {
            canvas.setMode(item.isExpanded() ? CONTRACT_HOVER_MODE : EXPAND_HOVER_MODE);
        } else {
            canvas.setMode(item.isExpanded() ? CONTRACT_MODE : EXPAND_MODE);
        }
    }

    private void setItemMode(Canvas canvas, Tree<T> item, ItemListenerSet listenerSet) {
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

    private void drawItem(Canvas canvas, Rect2i itemRegion, Tree<T> item, ItemListenerSet listenerSet) {
        canvas.drawBackground(itemRegion);
        itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));

        canvas.addInteractionRegion(listenerSet.getTopListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY(),
                        itemRegion.width(), itemRegion.height() / 3));

        canvas.addInteractionRegion(listenerSet.getCenterListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY() + itemRegion.height() / 3,
                        itemRegion.width(), itemRegion.height() / 3));

        int heightOffset = itemRegion.height() - 3 * (itemRegion.height() / 3);

        canvas.addInteractionRegion(listenerSet.getBottomListener(), itemRenderer.getTooltip(item.getValue()),
                Rect2i.createFromMinAndSize(itemRegion.minX(), itemRegion.minY() + 2 * itemRegion.height() / 3,
                        itemRegion.width(), heightOffset + itemRegion.height() / 3));
    }

    private void drawDragHint(Canvas canvas, Rect2i itemRegion) {
        if (mouseOverItemType == MouseOverItemType.TOP) {
            canvas.drawLine(itemRegion.minX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.minY(), Color.WHITE);
        } else if (mouseOverItemType == MouseOverItemType.CENTER) {
            canvas.drawLine(itemRegion.minX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.minY(), Color.WHITE);
            canvas.drawLine(itemRegion.maxX(), itemRegion.minY(), itemRegion.maxX(), itemRegion.maxY(), Color.WHITE);
            canvas.drawLine(itemRegion.maxX(), itemRegion.maxY(), itemRegion.minX(), itemRegion.maxY(), Color.WHITE);
            canvas.drawLine(itemRegion.minX(), itemRegion.maxY(), itemRegion.minX(), itemRegion.minY(), Color.WHITE);
        } else if (mouseOverItemType == MouseOverItemType.BOTTOM) {
            canvas.drawLine(itemRegion.maxX(), itemRegion.maxY(), itemRegion.minX(), itemRegion.maxY(), Color.WHITE);
        }
    }

    private void updateListeners() {
        boolean mouseOver = false;
        for (ItemListenerSet set : itemListenerSets) {
            if (set.isMouseOver()) {
                mouseOver = true;
                break;
            }
        }
        if (!mouseOver) {
            onItemMouseRelease();
        }

        while (itemListenerSets.size() > model.get().getItemCount()) {
            itemListenerSets.remove(itemListenerSets.size() - 1);
            expandListeners.remove(expandListeners.size() - 1);
        }
        while (itemListenerSets.size() < model.get().getItemCount()) {
            itemListenerSets.add(new ItemListenerSet(
                    new ItemTopListener(itemListenerSets.size()),
                    new ItemCenterListener(itemListenerSets.size()),
                    new ItemBottomListener(itemListenerSets.size())));
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

    /**
     * A set of tree element sublisteners.
     */
    private class ItemListenerSet {
        private ItemTopListener topListener;
        private ItemCenterListener centerListener;
        private ItemBottomListener bottomListener;

        private ItemListenerSet(ItemTopListener topListener, ItemCenterListener centerListener, ItemBottomListener bottomListener) {
            this.topListener = topListener;
            this.centerListener = centerListener;
            this.bottomListener = bottomListener;
        }

        public ItemTopListener getTopListener() {
            return topListener;
        }

        public ItemCenterListener getCenterListener() {
            return centerListener;
        }

        public ItemBottomListener getBottomListener() {
            return bottomListener;
        }

        public boolean isMouseOver() {
            return topListener.isMouseOver() || centerListener.isMouseOver() || bottomListener.isMouseOver();
        }
    }
}