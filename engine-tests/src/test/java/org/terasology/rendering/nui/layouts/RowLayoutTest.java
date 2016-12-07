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

public class RowLayoutTest {

    private RowLayout rowLayout;

    private Canvas canvas;

    private UIWidget itemAt1x1;
    private UIWidget itemAt1x2;
    private UIWidget itemAt1x3;

    @Before
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

        Vector2i availableSize = new Vector2i(200, 200);
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
        assertEquals(200, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width split according to the relative widths of the widgets
        // Gets 4/10 of the entire area
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(0, 0, 80, 200));
        // Gets 1/2 of the entire area
        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(80, 0, 100, 200));
        // Gets 1/10 of the entire area
        verify(canvas).drawWidget(itemAt1x3, Rect2i.createFromMinAndSize(80 + 100, 0, 20, 200));
    }

    @Test
    public void testNoRelativeWidths() throws Exception {

        rowLayout.setHorizontalSpacing(0);

        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be width of canvas
        assertEquals(200, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width split equally among 3 widgets as they have no relative widths
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(0, 0, 200 / 3, 200));
        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(200 / 3, 0, 200 / 3, 200));
        verify(canvas).drawWidget(itemAt1x3, Rect2i.createFromMinAndSize(200 / 3 + 200 / 3, 0, 200 / 3, 200));
    }

    @Test
    public void testSomeRelativeWidths() throws Exception {

        //Sets relative width for first widget only
        rowLayout.setColumnRatios(0.5f);
        rowLayout.setHorizontalSpacing(0);

        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());

        //Preferred width should be width of canvas
        assertEquals(200, result.x);
        //Preferred height should be the height of the tallest widget
        assertEquals(15, result.y);

        rowLayout.onDraw(canvas);

        //Width first determined for widget with relative width, then split equally among remaining widgets
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(0, 0, 200 / 2, 200));
        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(200 / 2, 0, (200 - 100) / 2, 200));
        verify(canvas).drawWidget(itemAt1x3, Rect2i.createFromMinAndSize(200 / 2 + (200 - 100) / 2, 0, (200 - 100) / 2, 200));
    }
}
