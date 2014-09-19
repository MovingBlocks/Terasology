/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;

import java.util.List;
import java.util.Objects;

/**
 *
 */
public class UIList<T> extends CoreWidget {

    private Binding<Boolean> selectable = new DefaultBinding<>(true);
    private Binding<T> selection = new DefaultBinding<>();
    private Binding<List<T>> list = new DefaultBinding<List<T>>(Lists.<T>newArrayList());

    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();
    private List<ItemInteractionListener> itemListeners = Lists.newArrayList();
    private List<ItemActivateEventListener<T>> eventListeners = Lists.newArrayList();


    public UIList() {

    }

    public UIList(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        updateItemListeners();

        canvas.setPart("item");
        int yOffset = 0;
        for (int i = 0; i < list.get().size(); ++i) {
            T item = list.get().get(i);
            ItemInteractionListener listener = itemListeners.get(i);
            if (Objects.equals(item, selection.get())) {
                canvas.setMode(ACTIVE_MODE);
            } else if (listener.isMouseOver()) {
                canvas.setMode(HOVER_MODE);
            } else {
                canvas.setMode(DEFAULT_MODE);
            }

            Vector2i preferredSize = canvas.getCurrentStyle().getMargin().grow(itemRenderer.getPreferredSize(item, canvas));
            Rect2i itemRegion = Rect2i.createFromMinAndSize(0, yOffset, canvas.size().x, preferredSize.y);
            canvas.drawBackground(itemRegion);

            itemRenderer.draw(item, canvas, canvas.getCurrentStyle().getMargin().shrink(itemRegion));
            canvas.addInteractionRegion(listener, itemRenderer.getTooltip(item), itemRegion);

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

    public void setList(List<T> list) {
        this.list.set(list);
    }

    public List<T> getList() {
        return list.get();
    }

    public void bindSelectable(Binding<Boolean> binding) {
        selectable = binding;
    }

    public boolean isSelectable() {
        return selectable.get();
    }

    public void setSelectable(boolean value) {
        selectable.set(value);
    }

    public void bindSelection(Binding<T> binding) {
        selection = binding;
    }

    public T getSelection() {
        if (!isSelectable()) {
            return null;
        }
        return selection.get();
    }

    public void setSelection(T val) {
        if (isSelectable()) {
            selection.set(val);
        }
    }

    public void subscribe(ItemActivateEventListener<T> eventListener) {
        eventListeners.add(eventListener);
    }

    public void unsubscribe(ItemActivateEventListener<T> eventListener) {
        eventListeners.remove(eventListener);
    }

    public void select(int index) {
        if (index >= 0 && index < list.get().size() && isSelectable()) {
            T item = list.get().get(index);
            setSelection(item);
        }
    }

    private void activate(int index) {
        if (index < list.get().size()) {
            T item = list.get().get(index);
            for (ItemActivateEventListener<T> listener : eventListeners) {
                listener.onItemActivated(this, item);
            }
        }
    }

    public ItemRenderer<T> getItemRenderer() {
        return itemRenderer;
    }

    public void setItemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
    }

    private class ItemInteractionListener extends BaseInteractionListener {
        private int index;

        public ItemInteractionListener(int index) {
            this.index = index;
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT && isSelectable()) {
                select(index);
                return true;
            }
            return false;
        }

        @Override
        public boolean onMouseDoubleClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT && isSelectable()) {
                activate(index);
                return true;
            }
            return false;
        }
    }

}
