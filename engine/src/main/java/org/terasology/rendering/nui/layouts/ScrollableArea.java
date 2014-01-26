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

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.widgets.UIScrollbar;
import org.terasology.utilities.collection.NullIterator;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Immortius
 */
public class ScrollableArea extends CoreLayout {
    private static final int SCROLL_MULTIPLIER = -42;

    private UIWidget content;
    private UIScrollbar scrollbar = new UIScrollbar();
    private boolean stickToBottom;

    private boolean moveToBottomPending;
    private boolean moveToTopPending;

    private InteractionListener scrollListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(int wheelTurns, Vector2i pos) {
            scrollbar.setValue(scrollbar.getValue() + wheelTurns * SCROLL_MULTIPLIER);
            return true;
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        int contentHeight = canvas.calculateRestrictedSize(content, new Vector2i(canvas.size().x, Integer.MAX_VALUE)).y;
        if (canvas.size().y < contentHeight) {
            int scrollbarWidth = canvas.calculateRestrictedSize(scrollbar, canvas.size()).x;
            contentHeight = canvas.calculateRestrictedSize(content, new Vector2i(canvas.size().x - scrollbarWidth, Integer.MAX_VALUE)).y;

            boolean atBottom = scrollbar.getRange() == scrollbar.getValue();

            Rect2i contentRegion = Rect2i.createFromMinAndSize(0, 0, canvas.size().x - scrollbarWidth, canvas.size().y);
            scrollbar.setRange(contentHeight - contentRegion.height());
            if ((stickToBottom && atBottom) || moveToBottomPending) {
                scrollbar.setValue(scrollbar.getRange());
                moveToBottomPending = false;
            }
            if (moveToTopPending) {
                scrollbar.setValue(0);
                moveToTopPending = false;
            }

            canvas.addInteractionRegion(scrollListener);
            canvas.drawWidget(scrollbar, Rect2i.createFromMinAndSize(canvas.size().x - scrollbarWidth, 0,
                    scrollbarWidth, canvas.size().y));

            // Draw content
            try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
                canvas.drawWidget(content, Rect2i.createFromMinAndSize(0, -scrollbar.getValue(), canvas.size().x, contentHeight));
            }
        } else {
            canvas.drawWidget(content);
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
