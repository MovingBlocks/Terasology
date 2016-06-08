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
    private static final String TREE_ITEM = "tree-item";
    private static final String HOVER_DISABLED_MODE = "hover-disabled";

    /**
     * Whether the widget is enabled and accepts user changes to its' structure.
     */
    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);
    /**
     * The maximum amount of items displayed before a scrollbar is created.
     */
    private Binding<Integer> maxItems = new DefaultBinding<>(5);
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
    private final List<TreeInteractionListener> itemListeners = Lists.newArrayList();

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
                copySelected();
                return true;
            } else if (ctrlDown && id == Keyboard.KeyId.V) {
                // Ctrl+V: paste the copied node as a child of the currently selected node.
                pasteSelected();
                return true;
            } else {
                return false;
            }
        }

        return false;
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
        model.get().getItem(selectedIndex.get()).addChild(new Tree<>(defaultValue.get()));
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
        updateItemListeners();

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
            TreeInteractionListener listener = itemListeners.get(i);

            handleListeners(canvas, item, listener);

            Rect2i itemRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                    i * itemHeight - verticalBar.getValue(),
                    availableWidth - item.getDepth() * itemIndent.get(),
                    itemHeight);

            try (SubRegion ignored = canvas.subRegion(scrollableArea, true)) {
                drawItem(canvas, itemRegion, item, listener);
            }
        }
    }

    private void drawNonScrollItems(Canvas canvas, int itemHeight) {
        for (int i = 0; i < model.get().getItemCount(); i++) {
            Tree<T> item = model.get().getItem(i);
            TreeInteractionListener listener = itemListeners.get(i);

            handleListeners(canvas, item, listener);

            Rect2i itemRegion = Rect2i.createFromMinAndSize(item.getDepth() * itemIndent.get(),
                    i * itemHeight,
                    canvas.size().x - item.getDepth() * itemIndent.get(),
                    itemHeight);

            drawItem(canvas, itemRegion, item, listener);
        }
    }

    private void handleListeners(Canvas canvas, Tree<T> item, TreeInteractionListener listener) {
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

    private void drawItem(Canvas canvas, Rect2i itemRegion, Tree<T> item, TreeInteractionListener listener) {
        canvas.drawBackground(itemRegion);
        itemRenderer.draw(item.getValue(), canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
        canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item.getValue()), itemRegion);
    }


    private void updateItemListeners() {
        while (itemListeners.size() > model.get().getItemCount()) {
            itemListeners.remove(itemListeners.size() - 1);
        }
        while (itemListeners.size() < model.get().getItemCount()) {
            itemListeners.add(new TreeInteractionListener(itemListeners.size()));
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE_ITEM);

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

        return result;
    }

    public void setModel(Tree<T> root) {
        setModel(new TreeModel<>(root));
    }

    public void setModel(TreeModel<T> newModel) {
        model.set(newModel);
    }

    public void setDefaultValue(T value) {
        defaultValue.set(value);
    }

    private class TreeInteractionListener extends BaseInteractionListener {
        private int index;

        TreeInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_RIGHT) {
                // Expand or contract and item on RMB - works even if the tree is disabled.
                model.get().getItem(index).setExpanded(!model.get().getItem(index).isExpanded());

                Tree<T> selectedItem = selectedIndex.get() != null ? model.get().getItem(selectedIndex.get()) : null;
                model.get().resetItems();

                if (selectedItem != null) {
                    int newIndex = model.get().indexOf(selectedItem);
                    if (newIndex == -1) {
                        selectedIndex.set(null);
                    } else {
                        selectedIndex.set(newIndex);
                    }
                }
                return true;
            } else if (isEnabled()) {
                if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                    // Select the item on LMB - deselect when selected again.
                    if (selectedIndex.get() != null && selectedIndex.get().equals(index)) {
                        selectedIndex.set(null);
                    } else {
                        selectedIndex.set(index);
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
