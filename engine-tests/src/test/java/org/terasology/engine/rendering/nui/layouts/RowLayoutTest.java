// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layouts;

import org.joml.Vector2i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.nui.Canvas;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.RowLayout;
import org.terasology.nui.util.RectUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RowLayoutTest {
    private static final int CANVAS_HEIGHT = 200;
    private static final int CANVAS_WIDTH = 200;

    private RowLayout rowLayout;

    private Canvas canvas;

    private UIWidget itemAt1x1;
    private UIWidget itemAt1x2;
    private UIWidget itemAt1x3;

    @BeforeEach
    public void setup() {
        rowLayout = new RowLayout();

        itemAt1x1 = mock(UIWidget.class);
        itemAt1x2 = mock(UIWidget.class);
        itemAt1x3 = mock(UIWidget.class);

        canvas = mock(Canvas.class);

        //    +-----------------------------------+  +---+  +-------+
        //    |                                   |  |1x2|  |       |
        //    |               1x1                 |  +---+  |       |
        //    |                                   |         |  1x3  |
        //    +-----------------------------------+         |       |
        //                                                  |       |
        //                                                  +-------+

        when(canvas.calculateRestrictedSize(eq(itemAt1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt1x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt1x3), any(Vector2i.class))).thenReturn(new Vector2i(10, 15));

        Vector2i availableSize = new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT);
        when(canvas.size()).thenReturn(availableSize);

        rowLayout.addWidget(itemAt1x1, null);
        rowLayout.addWidget(itemAt1x2, null);
        rowLayout.addWidget(itemAt1x3, null);
    }

    @Test
    public void testAllRelativeWidths() throws Exception {

        //Set relative width for all 3 widgets
        rowLayout.setColumnRatios(0.4f, 0.5f, 0.1f);
        rowLayout.setHorizontalSpacing(0);

        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be width of canvas
        assertEquals(CANVAS_WIDTH, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width split according to the relative widths of the widgets
        // Gets 4/10 of the entire area
        final int width1 = CANVAS_WIDTH * 4 / 10;
        // Gets 1/2 of the entire area
        final int width2 = CANVAS_WIDTH / 2;
        // Gets 1/10 of the entire area
        final int width3 = CANVAS_WIDTH / 10;

        verify(canvas).drawWidget(itemAt1x1,
                RectUtility.createFromMinAndSize(0, 0, width1, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x2,
                RectUtility.createFromMinAndSize(width1, 0, width2, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x3,
                RectUtility.createFromMinAndSize(width1 + width2, 0, width3, CANVAS_HEIGHT));
    }

    @Test
    public void testNoRelativeWidths() throws Exception {

        rowLayout.setHorizontalSpacing(0);

        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be width of canvas
        assertEquals(CANVAS_WIDTH, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width split equally among 3 widgets as they have no relative widths
        verify(canvas).drawWidget(itemAt1x1,
                RectUtility.createFromMinAndSize(0, 0, CANVAS_WIDTH / 3, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x2,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 3, 0, CANVAS_WIDTH / 3, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x3,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 3 + CANVAS_WIDTH / 3, 0, CANVAS_WIDTH / 3, CANVAS_HEIGHT));
    }

    @Test
    public void testSomeRelativeWidths() throws Exception {

        //Sets relative width for first widget only
        rowLayout.setColumnRatios(0.5f);
        rowLayout.setHorizontalSpacing(0);

        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be width of canvas
        assertEquals(CANVAS_WIDTH, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width first determined for widget with relative width, then split equally among remaining widgets
        final int width1 = CANVAS_WIDTH / 2;
        final int width2 = (CANVAS_WIDTH - width1) / 2;
        final int width3 = (CANVAS_WIDTH - width1) / 2;

        verify(canvas).drawWidget(itemAt1x1,
                RectUtility.createFromMinAndSize(0, 0, width1, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x2,
                RectUtility.createFromMinAndSize(width1, 0, width2, CANVAS_HEIGHT));
        verify(canvas).drawWidget(itemAt1x3,
                RectUtility.createFromMinAndSize(width1 + width2, 0, width3, CANVAS_HEIGHT));
    }
}
