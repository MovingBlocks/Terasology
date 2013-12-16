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
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class RowLayout extends CoreLayout {

    private static final Logger logger = LoggerFactory.getLogger(RowLayout.class);

    private List<Row> rows = Lists.newArrayList();
    private Border padding = Border.ZERO;

    public Row addRow(UIWidget... widgets) {
        Row row = new Row(widgets);
        rows.add(row);
        return row;
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

    @Override
    public Iterator<UIWidget> iterator() {
        List<UIWidget> widgets = Lists.newArrayList();
        for (Row row : rows) {
            widgets.addAll(row.items);
        }
        return widgets.iterator();
    }

    public class Row {
        private List<UIWidget> items;
        private TFloatList columnWidths = new TFloatArrayList();

        public Row(UIWidget... widgets) {
            this.items = Lists.newArrayList(widgets);
            if (widgets.length > 0) {
                float equalWidth = 1.0f / widgets.length;
                for (UIWidget widget : widgets) {
                    columnWidths.add(equalWidth);
                }
            }
        }

        public void draw(Canvas canvas, int yOffset, int height) {
            if (!items.isEmpty()) {
                int xOffset = 0;
                for (int i = 0; i < items.size(); ++i) {
                    int itemWidth = TeraMath.floorToInt(canvas.size().x * columnWidths.get(i));
                    Rect2i region = Rect2i.createFromMinAndSize(xOffset + padding.getLeft(), yOffset + padding.getTop(),
                            itemWidth - padding.getTotalWidth(), height - padding.getTotalHeight());
                    canvas.drawElement(items.get(i), region);
                    xOffset += itemWidth;
                }
            }
        }

        public Row setColumnRatios(float... columns) {
            if (columns.length > items.size()) {
                throw new IllegalArgumentException("Number of column ratios must not exceed number of elements in row.");
            }
            int column = 0;
            float totalWidthUsed = 0;
            TFloatList newColumnRatios = new TFloatArrayList();
            while (column < columns.length && column < items.size() - 1) {
                newColumnRatios.add(columns[column]);
                totalWidthUsed += columns[column];
                column++;
            }
            if (totalWidthUsed >= 1.0f) {
                logger.warn("Attempted to set column widths to a value exceeding 1.0f, ignoring");
            } else {
                float widthForRemaining = (1.0f - totalWidthUsed) / (items.size() - column);
                while (column < items.size()) {
                    newColumnRatios.add(widthForRemaining);
                    column++;
                }
                columnWidths = newColumnRatios;
            }
            return this;
        }

    }

}
