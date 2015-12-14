/*
 * Copyright 2014 MovingBlocks
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

public class ColumnLayoutTest {

    private ColumnLayout columnLayout;

    private Canvas canvas;

    private UIWidget itemAt1x1;
    private UIWidget itemAt2x1;
    private UIWidget itemAt3x1;
    private UIWidget itemAt1x2;
    private UIWidget itemAt2x2;
    private UIWidget itemAt3x2;

    @Before
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

        Vector2i availableSize = new Vector2i(200, 200);
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
        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(0, ((200 - 20) / 2), 100, 10));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x1, Rect2i.createFromMinAndSize(100, ((200 - 20) / 2), 40, 10));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x1, Rect2i.createFromMinAndSize(100 + 40, ((200 - 20) / 2), 60, 10));

        // Gets half of entire area
        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(0, ((200 - 20) / 2) + 10, 100, 10));
        // Gets one-fifth of entire area
        verify(canvas).drawWidget(itemAt2x2, Rect2i.createFromMinAndSize(100, ((200 - 20) / 2) + 10, 40, 10));
        // Gets three-tens of entire area
        verify(canvas).drawWidget(itemAt3x2, Rect2i.createFromMinAndSize(100 + 40, ((200 - 20) / 2) + 10, 60, 10));
    }

    @Test
    public void testThreeColumnsAutosizedMinimallySized() throws Exception {

        columnLayout.setAutoSizeColumns(true);
        columnLayout.setFillVerticalSpace(false);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(75, result.x);
        assertEquals(20, result.y);

        columnLayout.onDraw(canvas);

        verify(canvas).drawWidget(itemAt1x1, Rect2i.createFromMinAndSize(((200 - 75) / 2), ((200 - 20) / 2), 50, 10));
        verify(canvas).drawWidget(itemAt2x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, ((200 - 20) / 2), 5, 10));
        verify(canvas).drawWidget(itemAt3x1, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, ((200 - 20) / 2), 20, 10));

        verify(canvas).drawWidget(itemAt1x2, Rect2i.createFromMinAndSize(((200 - 75) / 2), ((200 - 20) / 2) + 10, 50, 10));
        verify(canvas).drawWidget(itemAt2x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50, ((200 - 20) / 2) + 10, 5, 10));
        verify(canvas).drawWidget(itemAt3x2, Rect2i.createFromMinAndSize(((200 - 75) / 2) + 50 + 5, ((200 - 20) / 2) + 10, 20, 10));
    }

}
