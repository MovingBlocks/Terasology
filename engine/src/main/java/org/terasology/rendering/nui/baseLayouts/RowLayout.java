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
package org.terasology.rendering.nui.baseLayouts;

import com.google.common.collect.Lists;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Border;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.UIWidget;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Immortius
 */
public class RowLayout extends CoreLayout<RowLayoutHint> {

    private static final Logger logger = LoggerFactory.getLogger(RowLayout.class);

    private List<UIWidget> contents = Lists.newArrayList();
    private TFloatList normalizedWidths;
    private TFloatList relativeWidths = new TFloatArrayList();

    public RowLayout() {

    }

    public RowLayout(String id) {
        super(id);
    }

    public RowLayout(UIWidget ... widgets) {
        contents.addAll(Arrays.asList(widgets));
    }

    @Override
    public void addWidget(UIWidget element, RowLayoutHint hint) {
        contents.add(element);
        if (hint != null) {
            relativeWidths.add(hint.getRelativeWidth());
        } else {
            relativeWidths.add(0);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (normalizedWidths == null || normalizedWidths.size() != contents.size()) {
            calcWidths();
        }

        Border padding = canvas.getCurrentStyle().getMargin();

        if (!contents.isEmpty()) {
            int xOffset = 0;
            for (int i = 0; i < contents.size(); ++i) {
                int itemWidth = TeraMath.floorToInt(canvas.size().x * normalizedWidths.get(i));
                Rect2i region = Rect2i.createFromMinAndSize(xOffset + padding.getLeft(), padding.getTop(),
                        itemWidth - padding.getTotalWidth(), canvas.size().y - padding.getTotalHeight());
                canvas.drawElement(contents.get(i), region);
                xOffset += itemWidth;
            }
        }
    }

    private void calcWidths() {
        int column = 0;
        float totalWidthUsed = 0;
        normalizedWidths = new TFloatArrayList();

        int skippedColumns = 0;
        while (column < relativeWidths.size() && column < contents.size() - 1) {
            if (relativeWidths.get(column) <= 0) {
                skippedColumns++;
            }
            normalizedWidths.add(relativeWidths.get(column));
            totalWidthUsed += relativeWidths.get(column);
            column++;
        }
        if (totalWidthUsed >= 1.0f) {
            logger.warn("Attempted to set column widths to a value exceeding 1.0f, ignoring");
            column = 0;
            totalWidthUsed = 0;
            skippedColumns = 0;
            normalizedWidths.clear();
        }
        float widthForRemaining = (1.0f - totalWidthUsed) / (contents.size() - column + skippedColumns);
        for (int i = 0; i < column; ++i) {
            if (normalizedWidths.get(i) <= 0) {
                normalizedWidths.set(i, widthForRemaining);
            }
        }
        while (column < contents.size()) {
            normalizedWidths.add(widthForRemaining);
            column++;
        }

    }

    @Override
    public Vector2i calcContentSize(Canvas canvas, Vector2i areaHint) {
        if (normalizedWidths == null || normalizedWidths.size() != contents.size()) {
            calcWidths();
        }

        Vector2i result = new Vector2i(areaHint.x, 0);
        for (int i = 0; i < contents.size(); ++i) {
            Vector2i widgetSize = canvas.calculateSize(contents.get(i), new Vector2i(TeraMath.floorToInt(areaHint.x * normalizedWidths.get(i)), areaHint.y));
            result.y = Math.max(result.y, widgetSize.y);
        }
        return result;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return contents.iterator();
    }

    public UIWidget setColumnRatios(float ... ratios) {
        relativeWidths.clear();
        relativeWidths.addAll(ratios);
        calcWidths();
        return this;
    }
}
