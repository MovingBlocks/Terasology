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
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;
import org.terasology.rendering.nui.widgets.models.Tree;
import org.terasology.rendering.nui.widgets.models.TreeModel;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class UITreeView<T> extends CoreWidget {
    private static final String TREE_ITEM = "tree-item";
    private static final String HOVER_DISABLED_MODE = "hover-disabled";

    @LayoutConfig
    private Binding<Boolean> enabled = new DefaultBinding<>(Boolean.TRUE);

    private Binding<Integer> maxDisplayedElements = new DefaultBinding<>(5);
    private Binding<Integer> itemIndent = new DefaultBinding<>(25);

    private UIScrollbar verticalBar = new UIScrollbar(true);
    private Binding<TreeModel<T>> model = new DefaultBinding<>(new TreeModel<>());
    private Binding<Integer> selectedIndex = new DefaultBinding<>();
    private Binding<Integer> mouseOverIndex = new DefaultBinding<>();
    private Binding<Tree<T>> clipboard = new DefaultBinding<>();
    private Binding<T> defaultValue = new DefaultBinding<>();

    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();

    private InteractionListener mainListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            // Change the UIScrollbar value on mousewheel
            if (enabled.get()) {
                int scrollMultiplier = 0 - verticalBar.getRange() / model.get().getElementCount();
                verticalBar.setValue(verticalBar.getValue() + event.getWheelTurns() * scrollMultiplier);
                return true;
            }
            return false;
        }
    };

    private final List<TreeInteractionListener> itemListeners = Lists.newArrayList();

    public UITreeView() {
    }

    public UITreeView(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateItemListeners();

        canvas.setPart(TREE_ITEM);

        int itemHeight = canvas.getCurrentStyle().getMargin().getTotalHeight() + canvas.getCurrentStyle().getFont().getLineHeight();
        canvas.addInteractionRegion(mainListener, Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y));

        if (model.get().getElementCount() > maxDisplayedElements.get()) {
            drawScrollbarItems(canvas, itemHeight);
        } else {
            drawNonScrollItems(canvas, itemHeight);
        }
    }

    private void drawScrollbarItems(Canvas canvas, int itemHeight) {
        Border itemMargin = canvas.getCurrentStyle().getMargin();

        // Calculate scrollbar dimensions
        int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x;
        int scrollbarHeight = canvas.size().y - itemMargin.getTotalHeight();
        int availableWidth = canvas.size().x - scrollbarWidth;
        int scrollbarXPosition = canvas.size().x - scrollbarWidth - itemMargin.getRight();
        int scrollbarYPosition = itemMargin.getTop();

        Rect2i scrollableArea = Rect2i.createFromMinAndSize(0, 0, canvas.size().x, canvas.size().y);

        // Draw the scrollbar
        Rect2i scrollbarRegion = Rect2i.createFromMinAndSize(scrollbarXPosition, scrollbarYPosition, scrollbarWidth, scrollbarHeight);
        canvas.drawWidget(verticalBar, scrollbarRegion);

        // Set the scrollbar range
        int maxScrollbarRange = itemHeight * (model.get().getElementCount() - maxDisplayedElements.get()) + itemMargin.getBottom();
        verticalBar.setRange(maxScrollbarRange);

        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
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
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
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
        if (selectedIndex.get() != null && Objects.equals(item, model.get().getElement(selectedIndex.get()))) {
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
        while (itemListeners.size() > model.get().getElementCount()) {
            itemListeners.remove(itemListeners.size() - 1);
        }
        while (itemListeners.size() < model.get().getElementCount()) {
            itemListeners.add(new TreeInteractionListener(itemListeners.size()));
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart(TREE_ITEM);

        model.get().setEnumerateExpandedOnly(false);
        Vector2i result = new Vector2i();
        for (int i = 0; i < model.get().getElementCount(); i++) {
            Tree<T> item = model.get().getElement(i);
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin()
                    .grow(itemRenderer.getPreferredSize(item.getValue(), canvas).addX(item.getDepth() * itemIndent.get()));
            result.x = Math.max(result.x, preferredSize.x);

            if (i < maxDisplayedElements.get()) {
                result.y += preferredSize.y;
            }
        }
        model.get().setEnumerateExpandedOnly(true);

        // Account for the width of the vertical scrollbar
        result.addX(canvas.calculateRestrictedSize(verticalBar, new Vector2i(canvas.size().x, canvas.size().y)).x);

        return result;
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown()) {
            int id = event.getKey().getId();

            if (id == Keyboard.KeyId.UP || id == Keyboard.KeyId.DOWN) {
                // Move the element up/down the list of its' parent's children on UP/DOWN.
                moveSelected(id);
                return true;
            } else if (id == Keyboard.KeyId.DELETE) {
                // Remove the element on DELETE.
                removeSelected();
                return true;
            } else if (id == Keyboard.KeyId.A) {
                // Add a new child on A. TODO: Should be Shift+A.
                addToSelected();
                return true;
            } else if (id == Keyboard.KeyId.C) {
                // Copy the selected element on C. TODO: Should be Ctrl+C.
                copySelected();
                return true;
            } else if (id == Keyboard.KeyId.V) {
                // Paste the element in the clipboard as a child of a selected element on V. TODO: Should be Ctrl+V.
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
            Tree<T> selectedElement = model.get().getElement(selectedIndex.get());
            Tree<T> parent = selectedElement.getParent();
            if (!selectedElement.isRoot()) {
                int indexOfElement = parent.getIndex(selectedElement);

                if (keyId == Keyboard.KeyId.UP && indexOfElement > 0) {
                    // Move the element up, unless it is the first element.
                    parent.removeChild(selectedElement);
                    parent.addChild(indexOfElement - 1, selectedElement);
                    model.get().resetElements(selectedElement.getRoot());

                    // Re-select the moved element.
                    selectedIndex.set(model.get().indexOf(selectedElement));
                } else if (keyId == Keyboard.KeyId.DOWN && indexOfElement < parent.getChildren().size() - 1) {
                    // Move the element down, unless it is the last element.
                    parent.removeChild(selectedElement);
                    parent.addChild(indexOfElement + 1, selectedElement);
                    model.get().resetElements(selectedElement.getRoot());

                    // Re-select the moved element.
                    selectedIndex.set(model.get().indexOf(selectedElement));
                }
            }
        }
    }

    private void removeSelected() {
        model.get().removeElement(selectedIndex.get());
        selectedIndex.set(null);
    }

    private void addToSelected() {
        model.get().getElement(selectedIndex.get()).addChild(new Tree<T>(defaultValue.get()));
    }

    private void copySelected() {
        clipboard.set(model.get().getElement(selectedIndex.get()).copy());
    }

    private void pasteSelected() {
        if (clipboard.get() != null) {
            model.get().getElement(selectedIndex.get()).addChild(clipboard.get());
            clipboard.set(null);
        }
    }

    public void setModel(Tree<T> root) {
        setModel(new TreeModel<T>(root));
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
                // Expand or contract and item on RMB - works even if the tree is disabled
                model.get().getElement(index).setExpanded(!model.get().getElement(index).isExpanded());
                model.get().resetElements(model.get().getElement(index).getRoot());
                return true;
            } else if (isEnabled()) {
                if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                    // Select the item on LMB - deselect when selected again
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

        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            super.onMouseOver(event);
            mouseOverIndex.set(index);
        }

        @Override
        public void onMouseLeave() {
            super.onMouseLeave();
            mouseOverIndex.set(null);
        }
    }
}
