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
package org.terasology.rendering.nui.layouts;

import com.google.common.collect.Lists;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.List;

/**
 */
public class FlowLayout extends CoreLayout<LayoutHint> {

    private List<UIWidget> contents = Lists.newArrayList();

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        contents.add(element);
    }

    @Override
    public void removeWidget(UIWidget element) {
        contents.remove(element);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int filledWidth = 0;
        int filledHeight = 0;
        int heightOffset = 0;
        for (UIWidget widget : contents) {
            Vector2i size = canvas.calculatePreferredSize(widget);
            if (filledWidth != 0 && filledWidth + size.x  > canvas.size().x) {
                heightOffset += filledHeight;
                filledWidth = 0;
                filledHeight = 0;
            }
            canvas.drawWidget(widget, Rect2i.createFromMinAndSize(filledWidth, heightOffset, size.x, size.y));
            filledWidth += size.x;
            filledHeight = Math.max(filledHeight, size.y);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Vector2i result = new Vector2i();
        int filledWidth = 0;
        int filledHeight = 0;
        for (UIWidget widget : contents) {
            Vector2i size = canvas.calculatePreferredSize(widget);
            if (filledWidth != 0 && filledWidth + size.x  > sizeHint.x) {
                result.x = Math.max(result.x, filledWidth);
                result.y += filledHeight;
                filledWidth = size.x;
                filledHeight = size.y;
            } else {
                filledWidth += size.x;
                filledHeight = Math.max(filledHeight, size.y);
            }
        }
        result.x = Math.max(result.x, filledWidth);
        result.y += filledHeight;

        return result;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return contents.iterator();
    }
}
