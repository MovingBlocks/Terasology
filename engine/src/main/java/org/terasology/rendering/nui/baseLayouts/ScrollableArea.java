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

import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.math.Border;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseWidgets.UIScrollbar;

import java.util.Arrays;
import java.util.Iterator;

/**
 * @author Immortius
 */
public class ScrollableArea extends CoreLayout {
    private static final int SCROLL_MULTIPLIER = -42;

    private UIWidget content;
    private UIScrollbar scrollbar = new UIScrollbar();

    private InteractionListener scrollListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseWheel(int wheelTurns, Vector2i pos) {
            scrollbar.setValue(scrollbar.getValue() + wheelTurns * SCROLL_MULTIPLIER);
            return true;
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        int contentHeight = canvas.calculateSize(content, canvas.getCurrentStyle().getMargin().shrink(canvas.size())).y;
        if (canvas.size().y < contentHeight) {
            int scrollbarWidth = canvas.calculateSize(scrollbar, canvas.size()).x;
            Border margin = canvas.getCurrentStyle().getMargin();
            contentHeight = canvas.calculateSize(content, canvas.getCurrentStyle().getMargin().shrink(new Vector2i(canvas.size().x - scrollbarWidth, canvas.size().y))).y;

            canvas.addInteractionRegion(scrollListener);
            canvas.drawElement(scrollbar, Rect2i.createFromMinAndSize(canvas.size().x - scrollbarWidth - margin.getRight(), margin.getTop(),
                    scrollbarWidth, canvas.size().y - margin.getTotalHeight()));

            // Draw content
            Rect2i contentRegion = margin.shrink(Rect2i.createFromMinAndSize(0, 0, canvas.size().x - scrollbarWidth, canvas.size().y));
            scrollbar.setRange(contentHeight - contentRegion.height());
            try (SubRegion ignored = canvas.subRegion(contentRegion, true)) {
                canvas.drawElement(content, Rect2i.createFromMinAndSize(0, -scrollbar.getValue(), canvas.size().x, contentHeight));
            }
        } else {
            canvas.drawElement(content);
        }
    }

    public void setContent(UIWidget widget) {
        this.content = widget;
    }

    @Override
    public Vector2i calcContentSize(Canvas canvas, Vector2i sizeHint) {
        return sizeHint;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return Arrays.asList(content).iterator();
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {

    }
}
