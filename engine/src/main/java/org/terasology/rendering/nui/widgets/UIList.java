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
import org.terasology.input.MouseInput;
import org.terasology.math.Border;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDoubleClickEvent;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A list widget.
 *
 * @param <T> the list element type
 */
public class UIList<T> extends CoreWidget {

    private final List<ItemInteractionListener> itemListeners = Lists.newArrayList();
    private final List<ItemActivateEventListener<T>> activateListeners = Lists.newArrayList();
    private final List<ItemSelectEventListener<T>> selectionListeners = Lists.newArrayList();
    private Binding<Boolean> interactive = new DefaultBinding<>(true);
    private Binding<Boolean> selectable = new DefaultBinding<>(true);
    private Binding<T> selection = new DefaultBinding<>();
    private Binding<List<T>> list = new DefaultBinding<>(new ArrayList<>());
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();
    private Binding<Boolean> canBeFocus = new DefaultBinding<>(true);

    public UIList() {

    }

    public UIList(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateItemListeners();
        canvas.setPart("item");

        boolean enabled = isEnabled();
        Border margin = canvas.getCurrentStyle().getMargin();

        int yOffset = 0;
        for (int i = 0; i < list.get().size(); ++i) {
            T item = list.get().get(i);
            Vector2i preferredSize = margin.grow(itemRenderer.getPreferredSize(item, canvas));
            Rect2i itemRegion = Rect2i.createFromMinAndSize(0, yOffset, canvas.size().x, preferredSize.y);
            ItemInteractionListener listener = itemListeners.get(i);
            if (enabled) {
                if (Objects.equals(item, selection.get())) {
                    canvas.setMode(ACTIVE_MODE);
                } else if (listener.isMouseOver()) {
                    canvas.setMode(HOVER_MODE);
                } else {
                    canvas.setMode(DEFAULT_MODE);
                }
                if (isInteractive()) {
                    canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item), itemRegion);
                }
            } else {
                canvas.setMode(DISABLED_MODE);
            }

            canvas.drawBackground(itemRegion);
            itemRenderer.draw(item, canvas, margin.shrink(itemRegion));

            yOffset += preferredSize.getY();
        }
    }

    private void updateItemListeners() {
        while (itemListeners.size() > list.get().size()) {
            itemListeners.remove(itemListeners.size() - 1);
        }
        while (itemListeners.size() < list.get().size()) {
            itemListeners.add(new ItemInteractionListener(itemListeners.size()));
        }
    }

    @Override
    public boolean canBeFocus() {
        return canBeFocus.get();
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        canvas.setPart("item");
        Vector2i result = new Vector2i();
        for (T item : list.get()) {
            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item, canvas));
            result.x = Math.max(result.x, preferredSize.x);
            result.y += preferredSize.y;
        }
        return result;
    }

    public void bindList(Binding<List<T>> binding) {
        this.list = binding;
    }

    /**
     * @return The list of options.
     */
    public List<T> getList() {
        return list.get();
    }

    /**
     * @param list The list to display on the buttons.
     */
    public void setList(List<T> list) {
        this.list.set(list);
    }

    public void bindSelectable(Binding<Boolean> binding) {
        selectable = binding;
    }

    /**
     * @return True if the list is interactive.
     */
    public boolean isInteractive() {
        return interactive.get();
    }

    /**
     * @return True if the list is selectable.
     */
    public boolean isSelectable() {
        return selectable.get();
    }

    /**
     * @param value A Boolean indicating the interactivity to set.
     */
    public void setInteractive(boolean value) {
        interactive.set(value);
    }


    /**
     * @param value A Boolean indicating how selectable the list should be.
     */
    public void setSelectable(boolean value) {
        selectable.set(value);
    }


    /**
     * @param value A Boolean indicating if it should be focusable.
     */
    public void setCanBeFocus(boolean value) {
        canBeFocus.set(value);
    }

    public void bindSelection(Binding<T> binding) {
        selection = binding;
    }

    /**
     * @return The value of the selected button.
     */
    public T getSelection() {
        if (!isSelectable()) {
            return null;
        }
        return selection.get();
    }

    /**
     * @param item The item to be selected
     */
    public void setSelection(T item) {
        if (isSelectable()) {
            selection.set(item);
            for (ItemSelectEventListener<T> listener : selectionListeners) {
                listener.onItemSelected(this, item);
            }
        }
    }

    /**
     * Subscribe an event listener to be called upon the list being activated.
     *
     * @param eventListener The event listener to call.
     */
    public void subscribe(ItemActivateEventListener<T> eventListener) {
        activateListeners.add(eventListener);
    }

    /**
     * Remove an event listener from being called when the list being activated.
     *
     * @param eventListener The event listener to remove.
     */
    public void unsubscribe(ItemActivateEventListener<T> eventListener) {
        activateListeners.remove(eventListener);
    }

    /**
     * Subscribe an event listener to be called then an item is selected.
     *
     * @param eventListener The event listener to add.
     */
    public void subscribeSelection(ItemSelectEventListener<T> eventListener) {
        selectionListeners.add(eventListener);
    }

    /**
     * Remove an event listener from being called when a selection is made.
     *
     * @param eventListener The event listener to remove.
     */
    public void unsubscribeSelection(ItemSelectEventListener<T> eventListener) {
        selectionListeners.remove(eventListener);
    }

    /**
     * Select an item from the list via index.
     *
     * @param index The index of the item to select.
     */
    public void select(int index) {
        if (index >= 0 && index < list.get().size() && isSelectable()) {
            T item = list.get().get(index);
            setSelection(item);
        }
    }

    /**
     * Activate an item from the list via index.
     *
     * @param index The index of the item to select.
     */
    private void activate(int index) {
        if (index < list.get().size()) {
            T item = list.get().get(index);
            for (ItemActivateEventListener<T> listener : activateListeners) {
                listener.onItemActivated(this, item);
            }
        }
    }

    /**
     * @return The item renderer used in the list.
     */
    public ItemRenderer<T> getItemRenderer() {
        return itemRenderer;
    }

    /**
     * @param itemRenderer The renderer to use.
     */
    public void setItemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    private class ItemInteractionListener extends BaseInteractionListener {
        private int index;

        ItemInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT && isSelectable()) {
                select(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseDoubleClick(NUIMouseDoubleClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT && isSelectable()) {
                activate(index);
                return true;
            }
            return false;
        }
    }

}
