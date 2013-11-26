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
package org.terasology.rendering.nui.layout;

import com.google.common.collect.Lists;
import org.terasology.math.Rect2i;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

import java.util.List;

/**
 * @author Immortius
 */
public class RowLayout extends AbstractWidget {

    private List<Row> rows = Lists.newArrayList();
    private Border padding = Border.ZERO;

    public void addRow(UIWidget ... widgets) {
        rows.add(new Row(widgets));
    }

    public Border getPadding() {
        return padding;
    }

    public void setPadding(Border padding) {
        this.padding = padding;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!rows.isEmpty()) {
            int height = canvas.size().y / rows.size();
            int yOffset = 0;
            for (Row row : rows) {

                row.draw(canvas, yOffset, height);
                yOffset += height;
            }
        }
    }

    public int getRowCount() {
        return rows.size();
    }

    private class Row {
        private List<UIWidget> items;

        public Row(UIWidget ... widgets) {
            this.items = Lists.newArrayList(widgets);
        }

        public void draw(Canvas canvas, int yOffset, int height) {
            if (!items.isEmpty()) {
                int itemWidth = canvas.size().x / items.size();
                for (int i = 0; i < items.size(); ++i) {
                    int xOffset = i * canvas.size().x / items.size();
                    Rect2i region = Rect2i.createFromMinAndSize(xOffset + padding.getLeft(), yOffset + padding.getTop(),
                            itemWidth - padding.getTotalWidth(), height - padding.getTotalHeight());
                    canvas.drawElement(items.get(i), region);
                }
            }
        }

    }

}
