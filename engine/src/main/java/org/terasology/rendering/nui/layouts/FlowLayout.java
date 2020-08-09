// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
 * The Flow layout arranges its children in a directional flow that wraps at the layout's boundary, very much like words
 * wrap at the end of a line when writing a text. The children are laid out in row in the flow direction, each widget
 * sized by its preferred size. The individual elements are top-aligned, and wrapped at the first element that does not
 * fit in the row.
 * <p>
 * Flow lays out each managed child regardless of the child's visible property value - invisible children won't be
 * rendered but will take up space.
 * <p>
 * The layout may be styled as other widgets with {@link org.terasology.rendering.nui.skin.UISkin}.
 * <p>
 * The Flow layout can be configured in UI assets ({@code .ui} files):
 * <pre>
 * {@code
 * {
 *   "type": "FlowLayout",
 *   "verticalSpacing": 8,
 *   "horizontalSpacing": 24,
 *   "contents": [...]
 *   // all properties of AbstractWidget
 * }
 * }
 * </pre>
 */
public class FlowLayout extends CoreLayout<LayoutHint> {

    /**
     * The ordered list of widgets to be arranged in the flow direction.
     */
    private List<UIWidget> contents = Lists.newArrayList();

    /**
     * The vertical spacing between adjacent widgets, in pixels
     */
    @LayoutConfig
    private int verticalSpacing;

    /**
     * The horizontal spacing between adjacent widgets, in pixels
     */
    @LayoutConfig
    private int horizontalSpacing;

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        contents.add(element);
    }

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
        layout(canvas, canvas.size(), true);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return layout(canvas, sizeHint, false);
    }

    /**
     * Applies the flow layout to its children, arranging them in a directional flow.
     * <p>
     * The layout algorithm will wrap elements to a new line if adding them in the same line would exceed the bounding
     * size. The first element of a row is placed regardless of its size, and thus may exceed the bounding size. The
     * Flow layout attempts to stay within the preferred width, but will add new lines until all widgets have been laid
     * out.
     * <p>
     * If the preferred bounding size is wider than the preferred size of the widest child the Flow layout guarantees to
     * fit into the preferred width. There is no guarantee for the actual height.
     *
     * @param canvas the canvas to draw to and/or calculate sizes for
     * @param boundingSize the boundary for this layout, may be a canvas size or a size hint
     * @param draw whether to actually draw the widgets to the canvas
     * @return the computed bounding box size for when arranging the widget
     */
    private Vector2i layout(Canvas canvas, Vector2i boundingSize, boolean draw) {
        // current bounding box for the widgets already laid out
        Vector2i result = new Vector2i();
        // current "cursor" position, always where the next widget is to be placed?
        int widthOffset = 0;
        // current "cursor" position, always where the next widget is to be placed
        int heightOffset = 0;
        // local maximum for row height
        int rowHeight = 0;

        for (UIWidget widget : contents) {
            Vector2i size = canvas.calculatePreferredSize(widget);

            if (widthOffset != 0 && widthOffset + horizontalSpacing + size.x <= boundingSize.x) {
                // place widget in the current row
                widthOffset += horizontalSpacing;
            } else if (widthOffset != 0) {
                // we need to wrap the row
                result.x = Math.max(result.x, widthOffset);
                result.y += rowHeight + verticalSpacing;
                heightOffset = result.y;
                widthOffset = 0;
                rowHeight = 0;
            }

            if (draw) {
                canvas.drawWidget(widget, Rect2i.createFromMinAndSize(widthOffset, heightOffset, size.x, size.y));
            }
            widthOffset += size.x;
            rowHeight = Math.max(rowHeight, size.y);
        }

        result.x = Math.max(result.x, widthOffset);
        result.y += rowHeight;

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
}
