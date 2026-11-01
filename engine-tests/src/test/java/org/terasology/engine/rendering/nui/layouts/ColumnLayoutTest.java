// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layouts;

import org.joml.Vector2i;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.nui.Canvas;
import org.terasology.nui.UIWidget;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.util.RectUtility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ColumnLayoutTest {
    private static final int CANVAS_HEIGHT = 200;
    private static final int CANVAS_WIDTH = 200;

    private ColumnLayout columnLayout;

    private Canvas canvas;

    private UIWidget itemAt1x1;
    private UIWidget itemAt2x1;
    private UIWidget itemAt3x1;
    private UIWidget itemAt1x2;
    private UIWidget itemAt2x2;
    private UIWidget itemAt3x2;

    @BeforeEach
    public void setup() {
        columnLayout = new ColumnLayout();

        itemAt1x1 = mock(UIWidget.class);
        itemAt2x1 = mock(UIWidget.class);
        itemAt3x1 = mock(UIWidget.class);
        itemAt1x2 = mock(UIWidget.class);
        itemAt2x2 = mock(UIWidget.class);
        itemAt3x2 = mock(UIWidget.class);

        canvas = mock(Canvas.class);

        //    +-----------------------------------+  +---+  +-------+
        //    |                                   |  |2x1|  |       |
        //    |               1x1                 |  +---+  |  3x1  |
        //    |                                   |         |       |
        //    +-----------------------------------+         +-------+

        when(canvas.calculateRestrictedSize(eq(itemAt1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x1), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x1), any(Vector2i.class))).thenReturn(new Vector2i(10, 10));

        //    +--------------+  +---+  +--------------+
        //    |              |  |2x2|  |              |
        //    |     1x2      |  +---+  |      3x2     |
        //    |              |         |              |
        //    +--------------+         +--------------+

        when(canvas.calculateRestrictedSize(eq(itemAt1x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));
        when(canvas.calculateRestrictedSize(eq(itemAt2x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(itemAt3x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));

        Vector2i availableSize = new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT);
        when(canvas.size()).thenReturn(availableSize);

        columnLayout.setColumns(3);
        columnLayout.addWidget(itemAt1x1);
        columnLayout.addWidget(itemAt2x1);
        columnLayout.addWidget(itemAt3x1);
        columnLayout.addWidget(itemAt1x2);
        columnLayout.addWidget(itemAt2x2);
        columnLayout.addWidget(itemAt3x2);
    }

    @Test
    public void testThreeColumnsProportionallySized() throws Exception {

        columnLayout.setAutoSizeColumns(false);
        columnLayout.setFillVerticalSpace(false);
        columnLayout.setColumnWidths(0.5f, 0.2f, 0.3f);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, canvas.size());

        // This is the size of the first column divided by its ratio.
        // In general, the minimum column size / ratio guarantees the ration
        // and insures that every column has at least as much as its preferred size
        assertEquals(100, result.x);
        assertEquals(20, result.y);

        columnLayout.onDraw(canvas);

        // Gets half of entire area
        verify(canvas).drawWidget(itemAt1x1,
                RectUtility.createFromMinAndSize(0, ((CANVAS_HEIGHT - 20) / 2),
                        CANVAS_WIDTH / 2, 10));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x1,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 2, ((CANVAS_HEIGHT - 20) / 2),
                        CANVAS_WIDTH * 2 / 10, 10));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x1,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 2 + CANVAS_WIDTH * 2 / 10, ((CANVAS_HEIGHT - 20) / 2),
                        CANVAS_WIDTH * 3 / 10, 10));

        // Gets half of entire area
        verify(canvas).drawWidget(itemAt1x2,
                RectUtility.createFromMinAndSize(0, ((CANVAS_HEIGHT - 20) / 2) + 10,
                        CANVAS_WIDTH / 2, 10));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x2,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 2, ((CANVAS_HEIGHT - 20) / 2) + 10,
                        CANVAS_WIDTH * 2 / 10, 10));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x2,
                RectUtility.createFromMinAndSize(CANVAS_WIDTH / 2 + CANVAS_WIDTH * 2 / 10, ((CANVAS_HEIGHT - 20) / 2) + 10,
                        CANVAS_WIDTH * 3 / 10, 10));
    }

    @Test
    public void testThreeColumnsAutosizedMinimallySized() throws Exception {

        columnLayout.setAutoSizeColumns(true);
        columnLayout.setFillVerticalSpace(false);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(75, result.x);
        assertEquals(20, result.y);

        columnLayout.onDraw(canvas);

        verify(canvas).drawWidget(itemAt1x1,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2), ((CANVAS_HEIGHT - 20) / 2),
                        50, 10));
        verify(canvas).drawWidget(itemAt2x1,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2) + 50, ((CANVAS_HEIGHT - 20) / 2),
                        5, 10));
        verify(canvas).drawWidget(itemAt3x1,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2) + 50 + 5, ((CANVAS_HEIGHT - 20) / 2),
                        20, 10));

        verify(canvas).drawWidget(itemAt1x2,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2), ((CANVAS_HEIGHT - 20) / 2) + 10,
                        50, 10));
        verify(canvas).drawWidget(itemAt2x2,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2) + 50, ((CANVAS_HEIGHT - 20) / 2) + 10,
                        5, 10));
        verify(canvas).drawWidget(itemAt3x2,
                RectUtility.createFromMinAndSize(((CANVAS_WIDTH - 75) / 2) + 50 + 5, ((CANVAS_HEIGHT - 20) / 2) + 10,
                        20, 10));
    }

}
