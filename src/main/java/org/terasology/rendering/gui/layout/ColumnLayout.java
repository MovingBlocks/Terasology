/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering.gui.layout;

import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.style.Style;

import javax.vecmath.Vector2f;
import java.util.ArrayList;
import java.util.List;

/**
 * Layout to place all elements in a column (below each other).
 * You can supply a spacing between each item and a border.
 *
 * @author synopia
 */
public class ColumnLayout implements Layout {

    //options
    private float spacingVertical = 0f;
    private boolean equalHeight = false;
    private float border;

    @Override
    public void layout(UIDisplayContainer container, boolean fitSize) {
        List<UIDisplayElement> allElements = container.getDisplayElements();
        List<UIDisplayElement> elements = new ArrayList<UIDisplayElement>();

        for (UIDisplayElement element : allElements) {
            if (element.isVisible() && !(element instanceof Style)) {
                elements.add(element);
            }
        }

        Vector2f[] cellSize = calcCellSize(elements);
        float y = border;
        for (int i = 0; i < cellSize.length; i++) {
            elements.get(i).setPosition(new Vector2f(border, y));
            elements.get(i).setSize(new Vector2f(cellSize[i].x, cellSize[i].y));

            y += cellSize[i].y + spacingVertical;
        }

        if (fitSize) {
            Vector2f size = new Vector2f(0f, 0f);
            for (int i = 0; i < cellSize.length; i++) {
                size.x = Math.max(size.x, cellSize[i].x);
                size.y += cellSize[i].y;
            }

            size.y -= spacingVertical;

            container.setSize(size);
        }
    }

    @Override
    public void render() {

    }

    private Vector2f[] calcCellSize(List<UIDisplayElement> elements) {
        Vector2f[] cellSize = new Vector2f[elements.size()];

        for (int i = 0; i < cellSize.length; i++) {
            cellSize[i] = new Vector2f(0f, 0f);
        }

        //get width and height of each cell
        float maxX = 0;
        float maxY = 0;
        for (int i = 0; i < cellSize.length; i++) {
            cellSize[i].x = elements.get(i).getSize().x;
            cellSize[i].y = elements.get(i).getSize().y;
            if (cellSize[i].x > maxX) {
                maxX = cellSize[i].x;
            }
            if (cellSize[i].y > maxY) {
                maxY = cellSize[i].y;
            }
        }
        for (int i = 0; i < cellSize.length; i++) {
            cellSize[i].x = maxX;
        }
        //if equal height is on
        if (equalHeight) {
            //set all rows to the max height
            for (int i = 0; i < cellSize.length; i++) {
                cellSize[i].y = maxY;
            }
        }

        return cellSize;
    }

    public boolean isEqualHeight() {
        return equalHeight;
    }

    public void setEqualHeight(boolean equalHeight) {
        this.equalHeight = equalHeight;
    }

    public float getSpacingVertical() {
        return spacingVertical;
    }

    public void setSpacingVertical(float spacingVertical) {
        this.spacingVertical = spacingVertical;
    }

    public float getBorder() {
        return border;
    }

    public void setBorder(float border) {
        this.border = border;
    }

}
