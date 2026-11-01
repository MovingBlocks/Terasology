// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layouts;

import org.joml.Vector2i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.nui.Canvas;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.CardLayout;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class CardLayoutTest {

    private CardLayout cardLayout;

    private Canvas canvas;

    private UIWidget widget1;
    private UIWidget widget2;
    private UIWidget widget3;

    @BeforeEach
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

        when(widget1.getPreferredContentSize(eq(canvas), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(widget2.getPreferredContentSize(eq(canvas), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(widget3.getPreferredContentSize(eq(canvas), any(Vector2i.class))).thenReturn(new Vector2i(10, 15));

        when(widget1.getId()).thenReturn("widget1");
        when(widget2.getId()).thenReturn("widget2");
        when(widget3.getId()).thenReturn("widget3");

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

