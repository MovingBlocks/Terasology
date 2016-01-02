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
public class ColumnLayout extends CoreLayout<LayoutHint> {

    @LayoutConfig
    private int columns = 1;
    @LayoutConfig
    private int horizontalSpacing;
    @LayoutConfig
    private int verticalSpacing;
    @LayoutConfig
    private boolean autoSizeColumns;
    @LayoutConfig
    private boolean fillVerticalSpace = true;
    @LayoutConfig
    private boolean extendLast;

    private List<UIWidget> widgetList = Lists.newArrayList();

    @LayoutConfig
    @SerializedName("column-widths")
    private float[] columnWidths = new float[]{1.0f};

    public ColumnLayout() {
    }

    public ColumnLayout(String id) {
        super(id);
    }

    public void addWidget(UIWidget widget) {
        widgetList.add(widget);
    }

    @Override
    public void removeWidget(UIWidget widget) {
        widgetList.remove(widget);
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
        columnWidths = new float[columns];
        float equalWidth = 1.0f / columns;
        for (int i = 0; i < columnWidths.length; ++i) {
            columnWidths[i] = equalWidth;
        }
    }

    public void setColumnWidths(float... widths) {
        if (widths.length > columns) {
            throw new IllegalArgumentException("More widths than columns");
        }

        float total = 0;
        int columnIndex = 0;
        while (columnIndex < widths.length) {
            total += widths[columnIndex];
            columnWidths[columnIndex] = widths[columnIndex];
            columnIndex++;
        }

        if (total > 1.0f) {
            throw new IllegalArgumentException("Total width exceeds 1.0");
        }

        if (columnIndex < columnWidths.length) {
            float remainingWidth = 1.0f - total;
            float widthPerColumn = remainingWidth / (columnWidths.length - columnIndex);
            while (columnIndex < columnWidths.length) {
                columnWidths[columnIndex++] = widthPerColumn;
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!widgetList.isEmpty()) {
            Vector2i availableSize = canvas.size();
            int numRows = TeraMath.ceilToInt((float) widgetList.size() / columns);
            if (numRows > 0) {
                availableSize.y -= verticalSpacing * (numRows - 1);
            }
            if (columns > 0) {
                availableSize.x -= horizontalSpacing * (columns - 1);
            }

            List<List<UIWidget>> rows = Lists.newArrayList(getRowIterator());
            List<RowInfo> rowInfos = Lists.newArrayList();
            rowInfos.addAll(rows.stream().map(row -> calculateRowSize(row, canvas, availableSize)).collect(Collectors.toList()));

            int[] minWidths = new int[columns];
            int minRowWidth = 0;
            int rowOffsetX = 0;
            if (autoSizeColumns) {
                for (RowInfo row : rowInfos) {
                    for (int column = 0; column < row.widgetSizes.size(); column++) {
                        minWidths[column] = Math.max(minWidths[column], row.widgetSizes.get(column).getX());
                    }
                }

                for (int width : minWidths) {
                    minRowWidth += width;
                }

                minRowWidth += (columns - 1) * horizontalSpacing;

                rowOffsetX = (canvas.size().x - minRowWidth) / 2;
            } else {
                minRowWidth = canvas.size().x;
                for (int i = 0; i < columns; ++i) {
                    minWidths[i] = TeraMath.floorToInt((minRowWidth - (columns - 1) * horizontalSpacing) * columnWidths[i]);
                }
            }

            int rowOffsetY = 0;
            int usedHeight = 0;
            for (RowInfo row : rowInfos) {
                usedHeight += row.height;
            }
            usedHeight += (rowInfos.size() - 1) * verticalSpacing;

            int excessHeight = canvas.size().y - usedHeight;
            if (fillVerticalSpace) {
                if (extendLast && numRows > 0) {
                    // give all the extra space to the last entry
                    rowInfos.get(numRows - 1).height += excessHeight;
                } else {
                    // distribute extra height equally
                    int extraSpacePerRow = excessHeight / rowInfos.size();

                    for (RowInfo row : rowInfos) {
                        row.height += extraSpacePerRow;
                    }
                }
            } else {
                rowOffsetY = excessHeight / 2;
            }
            for (int rowIndex = 0; rowIndex < rows.size(); ++rowIndex) {
                List<UIWidget> row = rows.get(rowIndex);
                RowInfo rowInfo = rowInfos.get(rowIndex);
                int cellOffsetX = rowOffsetX;
                for (int i = 0; i < row.size(); ++i) {
                    UIWidget widget = row.get(i);
                    int rowHeight = rowInfo.height;
                    if (widget != null) {
                        Rect2i drawRegion = Rect2i.createFromMinAndSize(cellOffsetX, rowOffsetY, minWidths[i], rowHeight);
                        canvas.drawWidget(widget, drawRegion);
                    }
                    cellOffsetX += minWidths[i] + horizontalSpacing;
                }
                rowOffsetY += rowInfo.height + verticalSpacing;
            }
        }
    }

    private RowInfo calculateRowSize(List<UIWidget> row, Canvas canvas, Vector2i areaHint) {
        int availableWidth = areaHint.x - horizontalSpacing * (columns - 1);

        RowInfo rowInfo = new RowInfo();

        for (int i = 0; i < columns && i < row.size(); ++i) {
            UIWidget widget = row.get(i);
            Vector2i cellSize = new Vector2i(availableWidth, areaHint.y);
            if (!autoSizeColumns) {
                cellSize.x *= columnWidths[i];
            }
            if (widget != null) {
                Vector2i contentSize = canvas.calculateRestrictedSize(widget, cellSize);
                rowInfo.widgetSizes.add(contentSize);
                rowInfo.height = Math.max(rowInfo.height, contentSize.y);
            } else {
                rowInfo.widgetSizes.add(new Vector2i(0, 0));
            }
        }
        return rowInfo;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Vector2i availableSize = new Vector2i(areaHint);
        int numRows = TeraMath.ceilToInt((float) widgetList.size() / columns);
        if (numRows > 0) {
            availableSize.y -= verticalSpacing * (numRows - 1);
        }
        if (columns > 0) {
            availableSize.x -= horizontalSpacing * (columns - 1);
        }

        Iterator<List<UIWidget>> rows = getRowIterator();
        Vector2i size = new Vector2i();
        int[] columnSizes = new int[columns];
        while (rows.hasNext()) {
            List<UIWidget> row = rows.next();
            RowInfo rowInfo = calculateRowSize(row, canvas, availableSize);
            size.y += rowInfo.height;
            if (rows.hasNext()) {
                size.y += verticalSpacing;
            }
            for (int i = 0; i < rowInfo.widgetSizes.size(); ++i) {
                columnSizes[i] = Math.max(columnSizes[i], rowInfo.widgetSizes.get(i).getX());
            }
        }
        for (int columnSize : columnSizes) {
            size.x += columnSize;
        }

        if (!autoSizeColumns) {
            for (int i = 0; i < columns; ++i) {
                size.x = Math.max(size.x, TeraMath.floorToInt(columnSizes[i] / columnWidths[i]));
            }
        }

        size.x += horizontalSpacing * (columns - 1);

        return size;
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        Iterator<List<UIWidget>> rows = getRowIterator();
        Vector2i size = new Vector2i();
        int[] columnSizes = new int[columns];
        while (rows.hasNext()) {
            List<UIWidget> row = rows.next();
            int rowHeight = 0;
            for (int i = 0; i < row.size(); ++i) {
                Vector2i maxSize = canvas.calculateMaximumSize(row.get(i));
                columnSizes[i] = Math.max(columnSizes[i], maxSize.x);
                rowHeight = Math.max(rowHeight, maxSize.y);
            }
            size.y = TeraMath.addClampAtMax(size.y, rowHeight);
            if (rows.hasNext()) {
                size.y = TeraMath.addClampAtMax(size.y, verticalSpacing);
            }
        }

        long width = 0;
        for (int columnSize : columnSizes) {
            width += columnSize;
        }

        if (!autoSizeColumns) {
            for (int i = 0; i < columns; ++i) {
                width = Math.min(width, TeraMath.floorToInt(columnSizes[i] / columnWidths[i]));
            }
        }

        width += horizontalSpacing * (columns - 1);

        size.x = (int) Math.min(Integer.MAX_VALUE, width);
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

    public boolean isAutoSizeColumns() {
        return autoSizeColumns;
    }

    public boolean isFillVerticalSpace() {
        return fillVerticalSpace;
    }

    /**
     * @param fillVerticalSpace true if the vertical space of the canvas should be filled.
     *        The elements are centered vertically otherwise.
     */
    public void setFillVerticalSpace(boolean fillVerticalSpace) {
        this.fillVerticalSpace = fillVerticalSpace;
    }

    public void setAutoSizeColumns(boolean autoSizeColumns) {
        this.autoSizeColumns = autoSizeColumns;
    }

    private Iterator<List<UIWidget>> getRowIterator() {
        return new Iterator<List<UIWidget>>() {

            Iterator<UIWidget> contentIterator = iterator();

            @Override
            public boolean hasNext() {
                return contentIterator.hasNext();
            }

            @Override
            public List<UIWidget> next() {
                List<UIWidget> row = Lists.newArrayList();
                for (int i = 0; i < columns; ++i) {
                    if (contentIterator.hasNext()) {
                        row.add(contentIterator.next());
                    }
                }
                return row;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static class RowInfo {
        private int height;
        private List<Vector2i> widgetSizes = Lists.newArrayList();

        @Override
        public String toString() {
            return super.toString() + "{height:" + height + ", widgetSizes:" + widgetSizes + "}";
        }
    }
}
