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
import org.terasology.input.events.KeyEvent;
import org.terasology.input.events.MouseButtonEvent;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class ColumnLayout extends AbstractWidget {

    private int columns = 1;
    private Border padding = new Border(0, 0, 0, 0);

    private List<UIWidget> widgetList = Lists.newArrayList();

    public void addWidget(UIWidget widget) {
        widgetList.add(widget);
    }

    public int getColumns() {
        return columns;
    }

    public Border getPadding() {
        return padding;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setPadding(Border padding) {
        this.padding = padding;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!widgetList.isEmpty()) {
            Vector2i cellSize = canvas.size();
            cellSize.x /= columns;
            int numRows = TeraMath.ceilToInt((float) widgetList.size() / columns);
            cellSize.y /= numRows;

            Vector2i drawSize = new Vector2i(cellSize);
            drawSize.x -= padding.getLeft() + padding.getRight();
            drawSize.y -= padding.getTop() + padding.getBottom();

            Vector2i currentOffset = new Vector2i();
            int currentColumn = 0;
            for (UIWidget widget : widgetList) {
                if (widget != null) {
                    Rect2i drawRegion = Rect2i.createFromMinAndSize(currentOffset.x + padding.getLeft(), currentOffset.y + padding.getTop(), drawSize.x, drawSize.y);
                    canvas.drawElement(widget, drawRegion);
                }

                if (++currentColumn == columns) {
                    currentColumn = 0;
                    currentOffset.x = 0;
                    currentOffset.y += cellSize.y;
                } else {
                    currentOffset.x += cellSize.x;
                }
            }
        }
    }

    @Override
    public void update(float delta) {
        for (UIWidget widget : widgetList) {
            widget.update(delta);
        }
    }

    @Override
    public void onMouseButtonEvent(MouseButtonEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void onKeyEvent(KeyEvent event) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T extends UIWidget> T find(String targetId, Class<T> type) {
        T result = super.find(targetId, type);
        if (result == null) {
            for (UIWidget widget : widgetList) {
                result = widget.find(targetId, type);
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return widgetList.iterator();
    }
}
