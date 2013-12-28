/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.baseWidgets;

import com.google.common.collect.Lists;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.itemRendering.ToStringTextRenderer;

import java.util.List;

/**
 *
 */
public class UIList<T> extends CoreWidget {

    private Binding<List<T>> list = new DefaultBinding<List<T>>(Lists.<T>newArrayList());
    private ItemRenderer<T> itemRenderer = new ToStringTextRenderer<>();

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart("item");
        int yOffset = 0;
        for (T item : list.get()) {
            Vector2i preferredSize = itemRenderer.getPreferredSize(item, canvas);
            itemRenderer.draw(item, canvas, Rect2i.createFromMinAndSize(0, yOffset,  canvas.size().x, preferredSize.y));
            yOffset += preferredSize.getY();
        }
    }

    @Override
    public Vector2i calcContentSize(Canvas canvas, Vector2i areaHint) {
        canvas.setPart("item");
        Vector2i result = new Vector2i();
        for (T item : list.get()) {
            Vector2i preferredSize = itemRenderer.getPreferredSize(item, canvas);
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

    public ItemRenderer<T> getItemRenderer() {
        return itemRenderer;
    }

    public void setItemRenderer(ItemRenderer<T> itemRenderer) {
        this.itemRenderer = itemRenderer;
    }
}
