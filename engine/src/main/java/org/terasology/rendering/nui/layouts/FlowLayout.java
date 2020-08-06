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
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;

import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class FlowLayout extends CoreLayout<LayoutHint> {

    private List<UIWidget> contents = Lists.newArrayList();

    /**
     * Whether the directional flow of this layout goes from left-to-right and right-to-left.
     * <p>
     * The children are laid out from left-to-right by default (false), aligned at the left border of the canvas. If
     * this toggle is explicitly enabled (true) the children are laid out right-to-left, aligned at the right border of
     * the canvas.
     * <p>
     * This toggle can be set programmatically or in {@code .ui} files that use the Flow layout.
     */
    @LayoutConfig
    private boolean rightToLeftAlign = false;

    /**
     * The vertical spacing between adjacent widgets, in pixels
     */
    @LayoutConfig
    private int verticalSpacing;

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        contents.add(element);
    }

    /**
     * The horizontal spacing between adjacent widgets, in pixels
     */
    @LayoutConfig
    private int horizontalSpacing;

    @Override
    public void removeWidget(UIWidget element) {
        contents.remove(element);
    }

    @Override
    public void removeAllWidgets() {
        contents.clear();
    }

    @Override
    public void onDraw(Canvas canvas) {
        int filledWidth = getInitializedWidgetWidth(canvas.size().x, 0);
        int filledHeight = 0;
        int heightOffset = 0;
        for (UIWidget widget : contents) {
            Vector2i size = canvas.calculatePreferredSize(widget);
            if (rightToLeftAlign) {
                filledWidth -= size.x;
            }
            if (filledWidth != getInitializedWidgetWidth(canvas.size().x, size.x) &&
                    (filledWidth + size.x > canvas.size().x || filledWidth < 0)) {
                heightOffset += filledHeight + verticalSpacing;
                filledWidth = getInitializedWidgetWidth(canvas.size().x, size.x);
                filledHeight = 0;
            }
            canvas.drawWidget(widget, Rect2i.createFromMinAndSize(filledWidth, heightOffset, size.x, size.y));
            filledWidth += ((rightToLeftAlign) ? -horizontalSpacing : size.x + horizontalSpacing);
            filledHeight = Math.max(filledHeight, size.y);
        }
    }

    private int getInitializedWidgetWidth(int canvasSizeX, int widgetSizeX) {
        return (rightToLeftAlign) ? canvasSizeX - widgetSizeX : 0;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Vector2i result = new Vector2i();
        int filledWidth = 0;
        int filledHeight = 0;
        for (UIWidget widget : contents) {
            Vector2i size = canvas.calculatePreferredSize(widget);
            if (filledWidth != 0 && filledWidth + size.x > sizeHint.x) {
                result.x = Math.max(result.x, filledWidth);
                result.y += filledHeight + verticalSpacing;
                filledWidth = size.x + horizontalSpacing;
                filledHeight = size.y;
            } else {
                filledWidth += size.x + horizontalSpacing;
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

    /**
     * Retrieves the horizontal spacing between adjacent widgets in this {@code FlowLayout}.
     *
     * @return The spacing, in pixels
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Retrieves the vertical spacing between adjacent widgets in this {@code FlowLayout}.
     *
     * @return The spacing, in pixels
     */
    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Sets the horizontal spacing between adjacent widgets in this {@code FlowLayout}.
     *
     * @param spacing The spacing, in pixels
     * @return This {@code FlowLayout}
     */
    public FlowLayout setHorizontalSpacing(int spacing) {
        this.horizontalSpacing = spacing;
        return this;
    }

    /**
     * Sets the vertical spacing between adjacent widgets in this {@code FlowLayout}.
     *
     * @param spacing The spacing, in pixels
     * @return This {@code FlowLayout}
     */
    public FlowLayout setVerticalSpacing(int spacing) {
        this.verticalSpacing = spacing;
        return this;
    }

    /**
     * Whether the directional flow of this layout goes from left-to-right and right-to-left.
     * <p>
     * If false, the children are laid out from left-to-right and aligned at the left border of the canvas. If true, the
     * children are laid out right-to-left and aligned at the right border of the canvas.
     * <p>
     * This toggle can be set programmatically or in {@code .ui} files that use the Flow layout.
     *
     * @return whether the children are laid out right-to-left and aligned to the right
     */
    public boolean isRightToLeftAlign() {
        return rightToLeftAlign;
    }

    /**
     * Set whether the directional flow of this layout goes from left-to-right and right-to-left.
     * <p>
     * The children are laid out from left-to-right by default (false), aligned at the left border of the canvas. If
     * this toggle is explicitly enabled (true) the children are laid out right-to-left, aligned at the right border of
     * the canvas.
     * <p>
     * This toggle can be set programmatically or in {@code .ui} files that use the Flow layout.
     *
     * @param rightToLeftAlign  whether the children are laid out right-to-left and aligned to the right
     */
    public void setRightToLeftAlign(boolean rightToLeftAlign) {
        this.rightToLeftAlign = rightToLeftAlign;
    }
}
