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
import org.terasology.math.Border;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
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
    private static final String EXPAND_BUTTON = "expand-button";
    private static final String TREE_ITEM = "tree-item";

    private static final String CONTRACT_MODE = "contract";
    private static final String CONTRACT_HOVER_MODE = "contract-hover";
    private static final String EXPAND_MODE = "expand";
    private static final String EXPAND_HOVER_MODE = "expand-hover";
    private static final String HOVER_DISABLED_MODE = "hover-disabled";

    /**
     * Whether the widget is enabled and accepts user changes to its' structure.
     */
    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);
    /**
     * The maximum amount of items displayed before a scrollbar is created.
     */
    private Binding<Integer> maxItems = new DefaultBinding<>(10);
    /**
     * The indentation of one level in the tree.
     */
    private Binding<Integer> itemIndent = new DefaultBinding<>(25);
    /**
     * A {@code UIScrollbar}; used when the amount of items exceeds {@code maxItems}.
     */
    private UIScrollbar verticalBar = new UIScrollbar(true);
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

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            // Mouse wheel: scroll up/down.
            if (enabled.get()) {
                int scrollMultiplier = 0 - verticalBar.getRange() / model.get().getItemCount();
                verticalBar.setValue(verticalBar.getValue() + event.getWheelTurns() * scrollMultiplier);
                return true;
            }
            return false;
        }
    };

    /**
     * The individual item listeners.
     */
    private final List<ItemInteractionListener> itemListeners = Lists.newArrayList();
    /**
     * The individual expand/contract button listeners.
     */
    private final List<ExpandButtonInteractionListener> expandListeners = Lists.newArrayList();
    /**
     * Tree item update listeners.
     */
    private List<TreeViewUpdateListener> updateListeners = Lists.newArrayList();

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
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
                copySelected();
                fireUpdateListeners();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.V) {
                // Ctrl+V: paste the copied node as a child of the currently selected node.
                pasteSelected();
                fireUpdateListeners();
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    private void fireUpdateListeners() {
        updateListeners.forEach(TreeViewUpdateListener::onChange);
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

    private void copySelected() {
        clipboard.set(model.get().getItem(selectedIndex.get()).copy());
    }

    private void pasteSelected() {
        if (clipboard.get() != null) {
            model.get().getItem(selectedIndex.get()).addChild(clipboard.get());
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateListeners();

        canvas.setPart(TREE_ITEM);

        int itemHeight = canvas.getCurrentStyle().getMargin().getTotalHeight() + canvas.getCurrentStyle().getFont().getLineHeight();
        canvas.addInteractionRegion(mainListener, Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y));

        if (model.get().getItemCount() > maxItems.get()) {
            drawScrollbarItems(canvas, itemHeight);
        } else {
            drawNonScrollItems(canvas, itemHeight);
        }
    }

    private void drawScrollbarItems(Canvas canvas, int itemHeight) {
        Border itemMargin = canvas.getCurrentStyle().getMargin();

        // Calculate scrollbar dimensions.
        int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x;
        int scrollbarHeight = canvas.size().y - itemMargin.getTotalHeight();
        int availableWidth = canvas.size().x - scrollbarWidth;
        int scrollbarXPosition = canvas.size().x - scrollbarWidth - itemMargin.getRight();
        int scrollbarYPosition = itemMargin.getTop();

        Rect2i scrollableArea = Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y);

        // Draw the scrollbar.
        Rect2i scrollbarRegion = Rect2i.createFromMinAndSize(scrollbarXPosition, scrollbarYPosition, scrollbarWidth, scrollbarHeight);
        canvas.drawWidget(verticalBar, scrollbarRegion);

        // Set the scrollbar range.
        int maxScrollbarRange = itemHeight * (model.get().getItemCount() - maxItems.get()) + itemMargin.getBottom();
        verticalBar.setRange(maxScrollbarRange);

        for (int i = 0; i < model.get().getItemCount(); i++) {
            Tree<T> item = model.get().getItem(i);
            ItemInteractionListener itemListener = itemListeners.get(i);
            ExpandButtonInteractionListener buttonListener = expandListeners.get(i);

            if (!item.isLeaf()) {
                canvas.setPart(EXPAND_BUTTON);
                setButtonMode(canvas, item, buttonListener);
                Rect2i buttonRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                        i * itemHeight - verticalBar.getValue(),
                        itemIndent.get(),
                        itemHeight);
                try (SubRegion ignored = canvas.subRegion(scrollableArea, true)) {
                    drawButton(canvas, buttonRegion, buttonListener);
                }
            }

            canvas.setPart(TREE_ITEM);
            setItemMode(canvas, item, itemListener);
            Rect2i itemRegion = Rect2i.createFromMinAndSize((item.getDepth() + 1) * itemIndent.get(),
                    i * itemHeight - verticalBar.getValue(),
                    availableWidth - (item.getDepth() + 1) * itemIndent.get(),
                    itemHeight);
            try (SubRegion ignored = canvas.subRegion(scrollableArea, true)) {
                drawItem(canvas, itemRegion, item, itemListener);
            }
        }
    }

    private void drawNonScrollItems(Canvas canvas, int itemHeight) {
        for (int i = 0; i < model.get().getItemCount(); i++) {
            Tree<T> item = model.get().getItem(i);
            ItemInteractionListener itemListener = itemListeners.get(i);
            ExpandButtonInteractionListener buttonListener = expandListeners.get(i);

            if (!item.isLeaf()) {
                canvas.setPart(EXPAND_BUTTON);
                setButtonMode(canvas, item, buttonListener);
                Rect2i buttonRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                        i * itemHeight,
                        itemIndent.get(),
                        itemHeight);
                drawButton(canvas, buttonRegion, buttonListener);
            }

            canvas.setPart(TREE_ITEM);
            setItemMode(canvas, item, itemListener);
            Rect2i itemRegion = Rect2i.createFromMinAndSize((item.getDepth() + 1) * itemIndent.get(),
                    i * itemHeight,
                    canvas.size().x - (item.getDepth() + 1) * itemIndent.get(),
                    itemHeight);
            drawItem(canvas, itemRegion, item, itemListener);
        }
    }

    private void setButtonMode(Canvas canvas, Tree<T> item, ExpandButtonInteractionListener listener) {
        if (listener.isMouseOver()) {
            canvas.setMode(item.isExpanded() ? CONTRACT_HOVER_MODE : EXPAND_HOVER_MODE);
        } else {
            canvas.setMode(item.isExpanded() ? CONTRACT_MODE : EXPAND_MODE);
        }
    }

    private void setItemMode(Canvas canvas, Tree<T> item, ItemInteractionListener listener) {
        if (selectedIndex.get() != null && Objects.equals(item, model.get().getItem(selectedIndex.get()))) {
            canvas.setMode(ACTIVE_MODE);
        } else if (listener.isMouseOver()) {
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

    private void drawItem(Canvas canvas, Rect2i itemRegion, Tree<T> item, ItemInteractionListener listener) {
        canvas.drawBackground(itemRegion);
        itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
        canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item.getValue()), itemRegion);
    }

    private void updateListeners() {
        while (itemListeners.size() > model.get().getItemCount()) {
            itemListeners.remove(itemListeners.size() - 1);
            expandListeners.remove(expandListeners.size() - 1);
        }
        while (itemListeners.size() < model.get().getItemCount()) {
            itemListeners.add(new ItemInteractionListener(itemListeners.size()));
            expandListeners.add(new ExpandButtonInteractionListener(expandListeners.size()));
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

            if (i < maxItems.get()) {
                result.y += preferredSize.y;
            }
        }
        model.get().setEnumerateExpandedOnly(true);

        // Account for the width of the vertical scrollbar.
        result.addX(canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x);

        // Account for the expand/contract button.
        result.addX(itemIndent.get());

        return result;
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

    public void subscribe(TreeViewUpdateListener listener) {
        Preconditions.checkNotNull(listener);
        updateListeners.add(listener);
    }

    public void unsubscribe(TreeViewUpdateListener listener) {
        Preconditions.checkNotNull(listener);
        updateListeners.remove(listener);
    }

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

    private class ItemInteractionListener extends BaseInteractionListener {
        private int index;

        ItemInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
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
    }
}