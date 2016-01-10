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
import com.google.gson.annotations.SerializedName;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIKeyEvent;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 */
public class MultiRowLayout extends CoreLayout<LayoutHint> {

    @LayoutConfig
    private int rows = 1;
    @LayoutConfig
    private int verticalSpacing;
    @LayoutConfig
    private int horizontalSpacing;
    @LayoutConfig
    private boolean autoSizeRows;

    private List<UIWidget> widgetList = Lists.newArrayList();

    @LayoutConfig
    @SerializedName("row-heights")
    private float[] rowHeights = new float[]{1.0f};

    public MultiRowLayout() {
    }

    public MultiRowLayout(String id) {
        super(id);
    }

    public void addWidget(UIWidget widget) {
        widgetList.add(widget);
    }

    @Override
    public void removeWidget(UIWidget widget) {
        widgetList.remove(widget);
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
        rowHeights = new float[rows];
        float equalHeight = 1.0f / rows;
        for (int i = 0; i < rowHeights.length; ++i) {
            rowHeights[i] = equalHeight;
        }
    }

    public void setRowHeights(float ... heights) {
        if (heights.length > rows) {
            throw new IllegalArgumentException("More heights than rows");
        }

        float total = 0;
        int rowIndex = 0;
        while (rowIndex < heights.length) {
            total += heights[rowIndex];
            rowHeights[rowIndex] = heights[rowIndex];
            rowIndex++;
        }

        if (total > 1.0f) {
            throw new IllegalArgumentException("Total height exceeds 1.0");
        }

        if (rowIndex < rowHeights.length) {
            float remainingHeight = 1.0f - total;
            float heightPerRow = remainingHeight / (rowHeights.length - rowIndex);
            while (rowIndex < rowHeights.length) {
                rowHeights[rowIndex++] = heightPerRow;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!widgetList.isEmpty()) {
            Vector2i availableSize = canvas.size();
            int numColumns = TeraMath.ceilToInt((float) widgetList.size() / rows);
            if (numColumns > 0) {
                availableSize.x -= horizontalSpacing * (numColumns - 1);
            }
            if (rows > 0) {
                availableSize.y -= verticalSpacing * (rows - 1);
            }

            List<List<UIWidget>> columns = Lists.newArrayList(getColumnIterator());
            List<ColumnInfo> columnInfos = Lists.newArrayList();
            columnInfos.addAll(columns.stream().map(column -> calculateColumnSize(column, canvas, availableSize)).collect(Collectors.toList()));

            int[] minHeights = new int[rows];
            int minColumnHeight = 0;
            int columnOffsetY = 0;
            if (autoSizeRows) {
                for (ColumnInfo column : columnInfos) {
                    for (int row = 0; row < column.widgetSizes.size(); row++) {
                        minHeights[row] = Math.max(minHeights[row], column.widgetSizes.get(row).getY());
                    }
                }

                for (int height : minHeights) {
                    minColumnHeight += height;
                }

                minColumnHeight += (rows - 1) * verticalSpacing;

                columnOffsetY = (canvas.size().y - minColumnHeight) / 2;
            } else {
                minColumnHeight = canvas.size().y;
                for (int i = 0; i < rows; ++i) {
                    minHeights[i] = TeraMath.floorToInt((minColumnHeight - (rows - 1) * verticalSpacing) * rowHeights[i]);
                }
            }

            int columnOffsetX = 0;
            int usedWidth = 0;
            for (ColumnInfo column : columnInfos) {
                usedWidth += column.width;
            }
            usedWidth += (columnInfos.size() - 1) * horizontalSpacing;
            columnOffsetX = (canvas.size().x - usedWidth) / 2;
            for (int columnIndex = 0; columnIndex < columns.size(); ++columnIndex) {
                List<UIWidget> column = columns.get(columnIndex);
                ColumnInfo columnInfo = columnInfos.get(columnIndex);
                int cellOffsetY = columnOffsetY;
                for (int i = 0; i < column.size(); ++i) {
                    UIWidget widget = column.get(i);
                    int columnWidth = columnInfo.width;
                    if (widget != null) {
                        Rect2i drawRegion = Rect2i.createFromMinAndSize(columnOffsetX, cellOffsetY, columnWidth, minHeights[i]);
                        canvas.drawWidget(widget, drawRegion);
                    }
                    cellOffsetY += minHeights[i] + verticalSpacing;
                }
                columnOffsetX += columnInfo.width + horizontalSpacing;
            }
        }
    }

    private ColumnInfo calculateColumnSize(List<UIWidget> column, Canvas canvas, Vector2i areaHint) {
        int availableHeight = areaHint.y - verticalSpacing * (rows - 1);

        ColumnInfo columnInfo = new ColumnInfo();

        for (int i = 0; i < rows && i < column.size(); ++i) {
            UIWidget widget = column.get(i);
            Vector2i cellSize = new Vector2i(areaHint.x, availableHeight);
            if (!autoSizeRows) {
                cellSize.y *= rowHeights[i];
            }
            if (widget != null) {
                Vector2i contentSize = canvas.calculateRestrictedSize(widget, cellSize);
                columnInfo.widgetSizes.add(contentSize);
                columnInfo.width = Math.max(columnInfo.width, contentSize.x);
            } else {
                columnInfo.widgetSizes.add(new Vector2i(0, 0));
            }
        }
        return columnInfo;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Vector2i availableSize = new Vector2i(areaHint);
        int numColumns = TeraMath.ceilToInt((float) widgetList.size() / rows);
        if (numColumns > 0) {
            availableSize.x -= horizontalSpacing * (numColumns - 1);
        }
        if (rows > 0) {
            availableSize.y -= verticalSpacing * (rows - 1);
        }

        Iterator<List<UIWidget>> columns = getColumnIterator();
        Vector2i size = new Vector2i();
        int[] rowSizes = new int[rows];
        while (columns.hasNext()) {
            List<UIWidget> column = columns.next();
            ColumnInfo columnInfo = calculateColumnSize(column, canvas, availableSize);
            size.x += columnInfo.width;
            if (columns.hasNext()) {
                size.x += horizontalSpacing;
            }
            for (int i = 0; i < columnInfo.widgetSizes.size(); ++i) {
                rowSizes[i] = Math.max(rowSizes[i], columnInfo.widgetSizes.get(i).getY());
            }
        }
        for (int rowSize : rowSizes) {
            size.y += rowSize;
        }

        if (!autoSizeRows) {
            for (int i = 0; i < rows; ++i) {
                size.y = Math.max(size.y, TeraMath.floorToInt(rowSizes[i] / rowHeights[i]));
            }
        }

        size.y += verticalSpacing * (rows - 1);

        return size;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        Iterator<List<UIWidget>> columns = getColumnIterator();
        Vector2i size = new Vector2i();
        int[] rowSizes = new int[rows];
        while (columns.hasNext()) {
            List<UIWidget> column = columns.next();
            int columnWidth = 0;
            for (int i = 0; i < column.size(); ++i) {
                Vector2i maxSize = canvas.calculateMaximumSize(column.get(i));
                rowSizes[i] = Math.max(rowSizes[i], maxSize.y);
                columnWidth = Math.max(columnWidth, maxSize.x);
            }
            size.x = TeraMath.addClampAtMax(size.x, columnWidth);
            if (columns.hasNext()) {
                size.x = TeraMath.addClampAtMax(size.x, horizontalSpacing);
            }
        }

        long height = 0;
        for (int rowSize : rowSizes) {
            height += rowSize;
        }

        if (!autoSizeRows) {
            for (int i = 0; i < rows; ++i) {
                height = Math.min(height, TeraMath.floorToInt(rowSizes[i] / rowHeights[i]));
            }
        }

        height += verticalSpacing * (rows - 1);

        size.y = (int) Math.min(Integer.MAX_VALUE, height);
        return size;
    }

    @Override
    public void update(float delta) {
        for (UIWidget widget : widgetList) {
            widget.update(delta);
        }
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        return false;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return widgetList.iterator();
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        addWidget(element);
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(int verticalSpacing) {
        this.verticalSpacing = verticalSpacing;
    }

    public boolean isAutoSizeRows() {
        return autoSizeRows;
    }

    public void setAutoSizeRows(boolean autoSizeRows) {
        this.autoSizeRows = autoSizeRows;
    }

    private Iterator<List<UIWidget>> getColumnIterator() {
        return new Iterator<List<UIWidget>>() {

            Iterator<UIWidget> contentIterator = iterator();

            @Override
            public boolean hasNext() {
                return contentIterator.hasNext();
            }

            @Override
            public List<UIWidget> next() {
                List<UIWidget> column = Lists.newArrayList();
                for (int i = 0; i < rows; ++i) {
                    if (contentIterator.hasNext()) {
                        column.add(contentIterator.next());
                    }
                }
                return column;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static class ColumnInfo {
        private int width;
        private List<Vector2i> widgetSizes = Lists.newArrayList();

        @Override
        public String toString() {
            return super.toString() + "{width:" + width + ", widgetSizes:" + widgetSizes + "}";
        }
    }
}
