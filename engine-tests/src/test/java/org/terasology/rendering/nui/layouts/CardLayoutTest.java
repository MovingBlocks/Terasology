/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.rendering.nui.layouts;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CardLayoutTest {

    private CardLayout cardLayout;

    private Canvas canvas;

    private UIWidget widget1;
    private UIWidget widget2;
    private UIWidget widget3;

    @Before
    public void setup() {
        cardLayout = new CardLayout();

        widget1 = mock(UIWidget.class);
        widget2 = mock(UIWidget.class);
        widget3 = mock(UIWidget.class);

        canvas = mock(Canvas.class);

        //    +-----------------------------------+  +---+  +-------+
        //    |                                   |  |1x2|  |       |
        //    |               1x1                 |  +---+  |       |
        //    |                                   |         |  1x3  |
        //    +-----------------------------------+         |       |
        //                                                  |       |
        //                                                  +-------+

        when(widget1.getPreferredContentSize(eq(canvas),any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(widget2.getPreferredContentSize(eq(canvas),any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(widget3.getPreferredContentSize(eq(canvas),any(Vector2i.class))).thenReturn(new Vector2i(10, 15));

        when (widget1.getId()).thenReturn("widget1");
        when (widget2.getId()).thenReturn("widget2");
        when (widget3.getId()).thenReturn("widget3");

        Vector2i availableSize = new Vector2i(200, 200);
        when(canvas.size()).thenReturn(availableSize);

        cardLayout.addWidget(widget1);
        cardLayout.addWidget(widget2);
        cardLayout.addWidget(widget3);
    }

    @Test
    public void testSwitchCard() throws Exception {

        Vector2i result = cardLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be the longest preferred width among widgets
        assertEquals(50, result.x);
        //Preferred height should be the tallest preferred height among widgets
        assertEquals(15, result.y);

        //Switch to widget1
        cardLayout.setDisplayedCard("widget1");
        cardLayout.onDraw(canvas);
        verify(canvas).drawWidget(widget1);

        //Switch to widget2
        cardLayout.setDisplayedCard("widget2");
        cardLayout.onDraw(canvas);
        verify(canvas).drawWidget(widget2);

        //Switch to widget3
        cardLayout.setDisplayedCard("widget3");
        cardLayout.onDraw(canvas);
        verify(canvas).drawWidget(widget3);
    }
}

