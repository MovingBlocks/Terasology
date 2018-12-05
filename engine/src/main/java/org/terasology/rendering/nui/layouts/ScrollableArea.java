/*
 * Copyright 2017 MovingBlocks
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

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;
import org.terasology.rendering.nui.widgets.UIList;
import org.terasology.rendering.nui.widgets.UIScrollbar;

import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;

/**
 * A layout that allows for a larger widget to be displayed in a smaller area with scrollbars.
 * <p>
 * Eg, If the widget is 100px tall and 20px wide, and the scrollable area is 20px wide and 20px tall
 * then a vertical scrollbar will be shown. This will allow the user to scroll up and down through the large widget,
 * as if they were moving a window over the widget.
 */
public class ScrollableArea extends CoreLayout {
    private static final int SCROLL_MULTIPLIER = -42;

    /**
     * The UIWidget this layout contains.
     */
    @LayoutConfig
    private UIWidget content;

    /**
     * Controls if the scrollable 'window' should stick to the bottom of the content
     * False by default
     */
    @LayoutConfig
    private boolean stickToBottom;

    /**
     * Controls if the widget can have a vertical scrollbar.
     * True by default.
     */
    @LayoutConfig
    private boolean verticalScrollbar = true;

    /**
     * Controls if the widget can have a horizontal scrollbar.
     * False by default.
     */
    @LayoutConfig
    private boolean horizontalScrollbar;

    /**
     * The preferred width of the scrollable area.
     * Set to null (blank) to use the width of the content
     * <p>
     * Null by default
     */
    @LayoutConfig
    private Integer preferredWidth;

    /**
     * The preferred height of the scrollable area.
     * Set to null (blank) to use the height of the content
     * <p>
     * Null by default
     */
    @LayoutConfig
    private Integer preferredHeight;

    private UIScrollbar verticalBar = new UIScrollbar(true);
    private UIScrollbar horizontalBar = new UIScrollbar(false);
    private boolean moveToBottomPending;
    private boolean moveToTopPending;

    /**
     * Interaction listener for the scrollbar. Handles scrollwheel scrolling.
     */
    private InteractionListener scrollListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            // If there are two scrollbars, we assume vertical has priority
            if (verticalScrollbar) {
                verticalBar.setValue(verticalBar.getValue() + event.getWheelTurns() * SCROLL_MULTIPLIER);
            } else if (horizontalScrollbar) {
                horizontalBar.setValue(horizontalBar.getValue() + event.getWheelTurns() * SCROLL_MULTIPLIER);
            }
            return true;
        }
    };

    /**
     * Scrolls the equivalent of 1 mouse wheel.
     * @param up If to scroll up or down.
     */
    public void scroll(boolean up) {
        int moveAmount = -1;
        if (up) {
            moveAmount = 1;
        }
        if (verticalScrollbar) {
            verticalBar.setValue(verticalBar.getValue() + moveAmount * SCROLL_MULTIPLIER);
        } else if (horizontalScrollbar) {
            horizontalBar.setValue(horizontalBar.getValue() + moveAmount * SCROLL_MULTIPLIER);
        }
    }

    /**
     * Sets the amount of the scrollbar.
     * @param moveAmount The position of the scrollbar as a percent.
     */
    public void setPosition(double moveAmount) {
        if (verticalScrollbar) {
            moveAmount *= verticalBar.getRange();
            verticalBar.setValue((int) Math.round(moveAmount));
        } else if (horizontalScrollbar) {
            moveAmount *= horizontalBar.getRange();
            horizontalBar.setValue((int) Math.round(moveAmount));
        }
    }

    /* Default constructor for internal systems */
    public ScrollableArea() {
    }

    @Override
    public void onDraw(Canvas canvas) {
        Vector2i canvasSize = canvas.size();
        Vector2i contentSize = canvas.calculateRestrictedSize(content, canvasSize);

        int horizontalScrollbarHeight = canvas.calculateRestrictedSize(horizontalBar, canvasSize).y;
        int verticalScrollbarWidth = canvas.calculateRestrictedSize(verticalBar, canvasSize).x;

        boolean verticalNeeded = shouldDrawWithAxisScrollbar(verticalScrollbar,
                horizontalScrollbar,
                canvasSize.y,
                canvasSize.x,
                contentSize.y,
                contentSize.x,
                horizontalScrollbarHeight);

        boolean horizontalNeeded = shouldDrawWithAxisScrollbar(horizontalScrollbar,
                verticalScrollbar,
                canvasSize.x,
                canvasSize.y,
                contentSize.x,
                contentSize.y,
                verticalScrollbarWidth);

        if (verticalNeeded) {
            if (horizontalNeeded) {
                drawWithBoth(canvas, canvasSize.sub(verticalScrollbarWidth, horizontalScrollbarHeight), contentSize);
            } else {
                drawWithJustVertical(canvas, canvasSize.sub(verticalScrollbarWidth, 0), contentSize);
            }
        } else if (horizontalNeeded) {
            drawWithJustHorizontal(canvas, canvasSize.sub(0, horizontalScrollbarHeight), contentSize);
        } else {
            drawWithNeither(canvas, canvasSize);
        }

    }

    /**
     * Checks if the area will need a scrollbar with the given size.
     * Can be used for either vertical o r horizontal checks
     *
     * @param areaSize      The size of the scrollable area
     * @param contentSize   The size of the content
     * @param scrollbarSize The size of the scrollbar
     * @return True if a scrollbar will be needed
     */
    private boolean willNeedScrollbar(int areaSize, int contentSize, int scrollbarSize) {
        return areaSize < contentSize + scrollbarSize;
    }

    /**
     * Checks if the area should display a scrollbar in a given axis.
     * <p>
     * This method has been written to work for both horizontal and vertical checks.
     *
     * @param axisScrollbarEnabled     Is the scrollbar in this axis enabled
     * @param oppositeScrollbarEnabled Is the other scrollbar enabled
     * @param axisAvailableSize        The maximum available size in this axis
     * @param oppositeAvailableSize    The maximum size in the other axis.
     * @param axisContentSize          The size of the content in this axis
     * @param oppositeContentSize      The size of the content in the other axis
     * @param oppositeScrollbarSize    The size of the scrollbar in the other axis
     * @return True if this scrollbar should be drawn
     */
    private boolean shouldDrawWithAxisScrollbar(boolean axisScrollbarEnabled,
                                                boolean oppositeScrollbarEnabled,
                                                int axisAvailableSize,
                                                int oppositeAvailableSize,
                                                int axisContentSize,
                                                int oppositeContentSize,
                                                int oppositeScrollbarSize) {
        if (!axisScrollbarEnabled) {
            /* The scrollbar for this axis is disabled */
            return false;
        } else {
            /* Is the content definitely too big in this axis */
            if (willNeedScrollbar(axisAvailableSize, axisContentSize, 0)) {
                return true;
            } else {
                /* Is the content definitely small enough in this axis */
                if (willNeedScrollbar(axisAvailableSize, axisContentSize, oppositeScrollbarSize)) {
                    return true;
                } else {
                    /* We only need this scrollbar if we need the other.
                     * This is as the content will be covered by the other scrollbar if it exists
                     */
                    return oppositeScrollbarEnabled && willNeedScrollbar(oppositeAvailableSize, oppositeContentSize, 0);
                }
            }
        }
    }


    /**
     * Draws this widget with just a vertical scrollbar.
     * If the content doesn't fit in the horizontal axis, it will be cropped.
     *
     * @param canvas        The canvas to draw on
     * @param availableSize The available size for the layout. Does not include scrollbar size
     * @param contentSize   The size of the content
     */
    private void drawWithJustVertical(Canvas canvas, Vector2i availableSize, Vector2i contentSize) {
        boolean atBottom = verticalBar.getRange() == verticalBar.getValue();

        Rect2i contentRegion = Rect2i.createFromMinAndSize(Vector2i.zero(), availableSize);
        verticalBar.setRange(contentSize.y - contentRegion.height());
        if ((stickToBottom && atBottom) || moveToBottomPending) {
            verticalBar.setValue(verticalBar.getRange());
            moveToBottomPending = false;
        }
        if (moveToTopPending) {
            verticalBar.setValue(0);
            moveToTopPending = false;
        }

        canvas.addInteractionRegion(scrollListener);
        int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, availableSize).x;
        canvas.drawWidget(verticalBar,
                Rect2i.createFromMinAndSize(
                        new Vector2i(availableSize.x, 0),
                        new Vector2i(scrollbarWidth, availableSize.y)));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(0, -verticalBar.getValue(), availableSize.x, contentSize.y));
        }
    }

    /**
     * Draw the widget with just a horizontal scrollbar.
     * <p>
     * If the content doesn't fit in the vertical axis, it will be cropped
     *
     * @param canvas        The canvas to draw on
     * @param availableSize The available size for the layout. Does not include scrollbar size
     * @param contentSize   The size of the widget to draw
     */
    private void drawWithJustHorizontal(Canvas canvas, Vector2i availableSize, Vector2i contentSize) {
        Rect2i contentRegion = Rect2i.createFromMinAndSize(Vector2i.zero(), availableSize);

        canvas.addInteractionRegion(scrollListener);
        horizontalBar.setRange(contentSize.x - contentRegion.width());
        int scrollbarHeight = canvas.calculateRestrictedSize(verticalBar, availableSize).y;
        canvas.drawWidget(horizontalBar,
                Rect2i.createFromMinAndSize(
                        new Vector2i(0, availableSize.y),
                        new Vector2i(availableSize.x, scrollbarHeight)));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(-horizontalBar.getValue(), 0, contentSize.x, availableSize.y));
        }
    }

    /**
     * Draw the widget with both the horizontal and vertical scrollbars
     *
     * @param canvas        The canvas to draw on
     * @param availableSize The available size for the layout. This does not include the scrollbar sizes
     * @param contentSize   The size of the widget to draw
     */
    private void drawWithBoth(Canvas canvas,
                              Vector2i availableSize,
                              Vector2i contentSize) {
        boolean atBottom = verticalBar.getRange() == verticalBar.getValue();

        Rect2i contentRegion = Rect2i.createFromMinAndSize(Vector2i.zero(), availableSize);

        verticalBar.setRange(contentSize.y - contentRegion.height());
        horizontalBar.setRange(contentSize.x - contentRegion.width());
        if ((stickToBottom && atBottom) || moveToBottomPending) {
            verticalBar.setValue(verticalBar.getRange());
            moveToBottomPending = false;
        }
        if (moveToTopPending) {
            verticalBar.setValue(0);
            moveToTopPending = false;
        }

        canvas.addInteractionRegion(scrollListener);
        canvas.drawWidget(verticalBar,
                Rect2i.createFromMinAndSize(
                        new Vector2i(availableSize.x, 0),
                        canvas.calculateRestrictedSize(verticalBar, availableSize)));
        canvas.drawWidget(horizontalBar,
                Rect2i.createFromMinAndSize(
                        new Vector2i(0, availableSize.y),
                        canvas.calculateRestrictedSize(horizontalBar, availableSize)));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(-horizontalBar.getValue(), -verticalBar.getValue(), contentSize.x, contentSize.y));
        }
    }

    /**
     * Draws the widget with neither a horizontal or vertical scrollbar.
     * <p>
     * If the widget is too large in either axis, it will be cropped
     *
     * @param canvas        The canvas to use
     * @param availableSize The available size for the layout
     */
    private void drawWithNeither(Canvas canvas, Vector2i availableSize) {
        canvas.drawWidget(content, Rect2i.createFromMinAndSize(Vector2i.zero(), availableSize));
    }


    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Vector2i pf = canvas.calculatePreferredSize(content);
        return new Vector2i(preferredWidth == null ? pf.x : preferredWidth, preferredHeight == null ? pf.y : preferredHeight);
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public Iterator<UIWidget> iterator() {
        if (content != null) {
            return Collections.singletonList(content).iterator();
        }
        return Collections.emptyIterator();
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        content = element;
    }

    @Override
    public void removeWidget(UIWidget element) {
        if (Objects.equals(element, content)) {
            content = null;
        }
    }

    @Override
    public void removeAllWidgets() {
        content = null;
    }

    /**
     * Moves the layout to display the bottom of the widget.
     * Equivalent to scrolling all the way down
     */
    public void moveToBottom() {
        moveToBottomPending = true;
    }

    /**
     * Moves the layout to display the bottom of the widget.
     * Equivalent to scrolling all the way up
     */
    public void moveToTop() {
        moveToTopPending = true;
    }

    /**
     * @param widget The widget to display
     */
    public void setContent(UIWidget widget) {
        this.content = widget;
    }

    /**
     * @param width  The preferred width of the area
     * @param height The preferred height of the area
     */
    public void setPreferredSize(Integer width, Integer height) {
        preferredWidth = width;
        preferredHeight = height;
    }

    /**
     * @return True if the 'window' of this layout will stick to the bottom
     */
    public boolean isStickToBottom() {
        return stickToBottom;
    }

    /**
     * @param stickToBottom Controls if the 'window' of this layout will stick to the bottom
     */
    public void setStickToBottom(boolean stickToBottom) {
        this.stickToBottom = stickToBottom;
    }

    /**
     * @param horizontalScrollbar Controls if the widget can have a horizontal scrollbar
     */
    public void setHorizontalScrollbar(boolean horizontalScrollbar) {
        this.horizontalScrollbar = horizontalScrollbar;
    }

    /**
     * @param verticalScrollbar Controls if the widget can have a vertical scrollbar
     */
    public void setVerticalScrollbar(boolean verticalScrollbar) {
        this.verticalScrollbar = verticalScrollbar;
    }
}
