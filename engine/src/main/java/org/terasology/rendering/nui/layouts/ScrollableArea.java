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
 */
public class ScrollableArea extends CoreLayout {
    private static final int SCROLL_MULTIPLIER = -42;

    @LayoutConfig
    private UIWidget content;

    @LayoutConfig
    private boolean stickToBottom;
    @LayoutConfig
    private boolean verticalScrollbar = true;
    @LayoutConfig
    private boolean horizontalScrollbar;

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

    public void setContent(UIWidget widget) {
        this.content = widget;
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return canvas.calculatePreferredSize(content);
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

    public boolean isStickToBottom() {
        return stickToBottom;
    }

    public void setStickToBottom(boolean stickToBottom) {
        this.stickToBottom = stickToBottom;
    }

    public void moveToBottom() {
        moveToBottomPending = true;
    }

    public void moveToTop() {
        moveToTopPending = true;
    }

}
