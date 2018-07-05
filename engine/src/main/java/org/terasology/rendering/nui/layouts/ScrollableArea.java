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
import org.terasology.rendering.nui.widgets.UIScrollbar;

import java.util.Arrays;
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

    @Override
    public void onDraw(Canvas canvas) {
        int availableWidth = canvas.size().x;
        int availableHeight = canvas.size().y;

        // First, try to layout it without any scroll bars
        Vector2i contentSize = canvas.calculateRestrictedSize(content, new Vector2i(availableWidth, availableHeight));
        if (contentSize.x <= availableWidth && contentSize.y <= availableHeight) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(new Vector2i(0, 0), new Vector2i(availableWidth, availableHeight)));
            return;
        }

        // Second, try to layout it just with vertical bar (if supported)
        if (verticalScrollbar) {
            int scrollbarWidth = canvas.calculateRestrictedSize(verticalBar, new Vector2i(availableWidth, availableHeight)).x;
            int scrollbarHeight = canvas.calculateRestrictedSize(verticalBar, new Vector2i(availableWidth, availableHeight)).y;

            contentSize = canvas.calculateRestrictedSize(content, new Vector2i(availableWidth - scrollbarWidth, Integer.MAX_VALUE));
            if (horizontalScrollbar && contentSize.x > availableWidth - scrollbarWidth) {
                if (contentSize.y > availableHeight - scrollbarHeight) {
                    layoutWithBothScrollbars(canvas, contentSize, availableWidth, availableHeight, scrollbarWidth, scrollbarHeight);
                } else {
                    contentSize = canvas.calculateRestrictedSize(content, new Vector2i(availableWidth, availableHeight - scrollbarHeight));
                    layoutWithJustHorizontal(canvas, contentSize, availableWidth, availableHeight, scrollbarHeight);
                }
            } else {
                layoutWithJustVertical(canvas, contentSize, availableWidth, availableHeight, scrollbarWidth);
            }
        } else if (horizontalScrollbar) {
            // Well we know that just horizontal is allowed
            int scrollbarHeight = canvas.calculateRestrictedSize(verticalBar, new Vector2i(availableWidth, availableHeight)).y;
            availableHeight -= scrollbarHeight;

            contentSize = canvas.calculateRestrictedSize(content, new Vector2i(Integer.MAX_VALUE, availableHeight - scrollbarHeight));
            layoutWithJustHorizontal(canvas, contentSize, availableWidth, availableHeight, scrollbarHeight);
        } else {
            throw new IllegalStateException("ScrollableArea without any scrollbar allowed, what's the point of that?!");
        }
    }

    private void layoutWithBothScrollbars(Canvas canvas, Vector2i contentSize, int fullWidth, int fullHeight, int scrollbarWidth, int scrollbarHeight) {
        int availableWidth = fullWidth - scrollbarWidth;
        int availableHeight = fullHeight - scrollbarHeight;

        boolean atBottom = verticalBar.getRange() == verticalBar.getValue();

        Rect2i contentRegion = Rect2i.createFromMinAndSize(0, 0, availableWidth, availableHeight);
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
        canvas.drawWidget(verticalBar, Rect2i.createFromMinAndSize(availableWidth, 0, scrollbarWidth, availableHeight));
        canvas.drawWidget(horizontalBar, Rect2i.createFromMinAndSize(0, availableHeight, availableWidth, scrollbarHeight));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(-horizontalBar.getValue(), -verticalBar.getValue(), contentSize.x, contentSize.y));
        }
    }

    private void layoutWithJustVertical(Canvas canvas, Vector2i contentSize, int fullWidth, int availableHeight, int scrollbarWidth) {
        int availableWidth = fullWidth - scrollbarWidth;

        boolean atBottom = verticalBar.getRange() == verticalBar.getValue();

        Rect2i contentRegion = Rect2i.createFromMinAndSize(0, 0, availableWidth, availableHeight);
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
        canvas.drawWidget(verticalBar, Rect2i.createFromMinAndSize(availableWidth, 0, scrollbarWidth, availableHeight));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(0, -verticalBar.getValue(), availableWidth, contentSize.y));
        }
    }

    private void layoutWithJustHorizontal(Canvas canvas, Vector2i contentSize, int availableWidth, int fullHeight, int scrollbarHeight) {
        int availableHeight = fullHeight - scrollbarHeight;

        Rect2i contentRegion = Rect2i.createFromMinAndSize(0, 0, availableWidth, availableHeight);
        horizontalBar.setRange(contentSize.x - contentRegion.width());

        canvas.addInteractionRegion(scrollListener);
        canvas.drawWidget(horizontalBar, Rect2i.createFromMinAndSize(0, availableHeight, availableWidth, scrollbarHeight));

        try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
            canvas.drawWidget(content, Rect2i.createFromMinAndSize(-horizontalBar.getValue(), 0, contentSize.x, availableHeight));
        }
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
            return Arrays.asList(content).iterator();
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
