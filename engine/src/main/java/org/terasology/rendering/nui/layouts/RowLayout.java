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
import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A layout that places widgets in a row, with support for relative widths
 */
public class RowLayout extends CoreLayout<RowLayoutHint> {

    private static final Logger logger = LoggerFactory.getLogger(RowLayout.class);

    /**
     * A list of the widgets in this {@link RowLayout}
     */
    private List<UIWidget> contents = Lists.newArrayList();
    /**
     * Maps widgets to their layout hints
     */
    private Map<UIWidget, RowLayoutHint> hints = Maps.newHashMap();

    /**
     * The spacing between adjacent widgets, in pixels
     */
    @LayoutConfig
    private int horizontalSpacing;

    /**
     * Creates an empty {@code RowLayout}.
     */
    public RowLayout() {

    }

    /**
     * Creates an empty {@code RowLayout} with the given id.
     *
     * @param id The id assigned to this {@code RowLayout}
     */
    public RowLayout(String id) {
        super(id);
    }

    /**
     * Creates a {@code RowLayout} containing the given widgets.
     *
     * @param widgets A variable number of {@link UIWidget}s to be added to this layout's widget list
     */
    public RowLayout(UIWidget... widgets) {
        for (UIWidget widget : widgets) {
            addWidget(widget, null);
        }
    }

    /**
     * Adds the widget to this layout's widget list.
     *
     * @param widget The {@code UIWidget} to be added
     * @param hint   An optional {@link RowLayoutHint} specifying how the widget should be drawn in this layout =
     *               such as whether it has a relative width or whether it uses the content width
     */
    @Override
    public void addWidget(UIWidget widget, RowLayoutHint hint) {
        contents.add(widget);
        if (hint != null) {
            hints.put(widget, hint);
        }
    }

    /**
     * Removes the widget from this layout's widget list.
     *
     * @param widget The {@code UIWidget} to be removed
     */
    @Override
    public void removeWidget(UIWidget widget) {
        contents.remove(widget);
        hints.remove(widget);
    }

    /**
     * Draws the widgets contained in this layout's widget list,
     * according to the widths calculated in {@link #calcWidths(Canvas)}.
     * This is called every frame.
     *
     * @param canvas The {@link Canvas} on which this {@code RowLayout} is drawn
     */
    @Override
    public void onDraw(Canvas canvas) {
        TIntList widths = calcWidths(canvas);

        if (!contents.isEmpty()) {
            int xOffset = 0;
            for (int i = 0; i < contents.size(); ++i) {
                int itemWidth = widths.get(i);
                Rect2i region = Rect2i.createFromMinAndSize(xOffset, 0, itemWidth, canvas.size().y);
                canvas.drawWidget(contents.get(i), region);
                xOffset += itemWidth;
                xOffset += horizontalSpacing;
            }
        }
    }

    /**
     * Calculates the widths of each of the widgets in this layout's widget list.
     * Widths are first calculated for widgets with a relative width specified,
     * followed by widgets which follow their content width.
     * The remaining width is then split equally among the remaining widgets.
     *
     * @param canvas The {@link Canvas} on which this {@code RowLayout} is drawn
     * @return A list of the widths of the widgets in this layout's widget list, in pixels
     */
    private TIntList calcWidths(Canvas canvas) {
        TIntList results = new TIntArrayList(contents.size());
        if (contents.size() > 0) {
            int width = canvas.size().x - horizontalSpacing * (contents.size() - 1);

            int totalWidthUsed = 0;
            int unprocessedWidgets = 0;
            for (UIWidget widget : contents) {
                RowLayoutHint hint = hints.get(widget);
                if (hint != null) {
                    if (!hint.isUseContentWidth() && hint.getRelativeWidth() != 0) {
                        int elementWidth = TeraMath.floorToInt(hint.getRelativeWidth() * width);
                        results.add(elementWidth);
                        totalWidthUsed += elementWidth;
                    } else {
                        results.add(0);
                        unprocessedWidgets++;
                    }
                } else {
                    results.add(0);
                    unprocessedWidgets++;
                }
            }

            if (unprocessedWidgets > 0) {
                int remainingWidthPerElement = (width - totalWidthUsed) / unprocessedWidgets;
                for (int i = 0; i < results.size(); ++i) {
                    if (results.get(i) == 0) {
                        RowLayoutHint hint = hints.get(contents.get(i));
                        if (hint != null) {
                            if (hint.isUseContentWidth()) {
                                Vector2i contentSize = contents.get(i).getPreferredContentSize(canvas, new Vector2i(remainingWidthPerElement, canvas.size().y));
                                results.set(i, contentSize.x);
                                totalWidthUsed += contentSize.x;
                                unprocessedWidgets--;
                            }
                        }
                    }
                }
            }

            if (unprocessedWidgets > 0) {
                int remainingWidthPerElement = (width - totalWidthUsed) / unprocessedWidgets;
                for (int i = 0; i < results.size(); ++i) {
                    if (results.get(i) == 0) {
                        results.set(i, remainingWidthPerElement);
                    }
                }
            }
        }
        return results;
    }

    /**
     * Retrieves the preferred content size of this {@code RowLayout}.
     * This is the minimum size this layout will take, given no space restrictions.
     *
     * @param canvas The {@code Canvas} on which this {@code RowLayout} is drawn
     * @param areaHint A {@link Vector2i} representing the space available for widgets to be drawn in this layout
     * @return A {@link Vector2i} representing the preferred content size of this {@code RowLayout}
     */
    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        TIntList widths = calcWidths(canvas);

        Vector2i result = new Vector2i(areaHint.x, 0);
        for (int i = 0; i < contents.size(); ++i) {
            Vector2i widgetSize = canvas.calculateRestrictedSize(contents.get(i), new Vector2i(TeraMath.floorToInt(widths.get(i)), areaHint.y));
            result.y = Math.max(result.y, widgetSize.y);
        }
        return result;
    }

    /**
     * Retrieves the maximum content size of this {@code RowLayout}.
     *
     * @param canvas The {@code Canvas} on which this {@code RowLayout} is drawn
     * @return A {@code Vector2i} representing the maximum content size of this {@code RowLayout}
     */
    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Retrieves an {@link Iterator} containing this layout's widget list.
     *
     * @return An {@code Iterator} containing the list of {@code UIWidgets}
     */
    @Override
    public Iterator<UIWidget> iterator() {
        return contents.iterator();
    }

    /**
     * Sets the ratios of the widths of the widgets in this {@code RowLayout}.
     *
     * @param ratios The ratios of the widths, each corresponding to a separate widget, with a maximum total of 1
     * @return This {@code RowLayout}
     */
    public RowLayout setColumnRatios(float... ratios) {
        hints.clear();
        for (int i = 0; i < ratios.length; ++i) {
            hints.put(contents.get(i), new RowLayoutHint(ratios[i]));
        }
        return this;
    }

    /**
     * Retrieves the spacing between adjacent widgets in this {@code RowLayout}.
     *
     * @return The spacing, in pixels
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Sets the spacing betweeen adjacent widgets in this {@code RowLayout}.
     *
     * @param spacing The spacing, in pixels
     * @return This {@code RowLayout}
     */
    public RowLayout setHorizontalSpacing(int spacing) {
        this.horizontalSpacing = spacing;
        return this;
    }
}
