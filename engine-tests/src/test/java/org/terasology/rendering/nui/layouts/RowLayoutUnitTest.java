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

public class RowLayoutUnitTest {
    private static final int C_H = 200;
    private static final int C_W = 200;
    private RowLayout rowLayout;
    private Canvas canvas;
    private UIWidget item1x1;
    private UIWidget item1x2;
    private UIWidget item1x3;
    @Before
    public void setup() {
        rowLayout = new RowLayout();
        item1x1 = mock(UIWidget.class);
        item1x2 = mock(UIWidget.class);
        item1x3 = mock(UIWidget.class);
        canvas = mock(Canvas.class);
        when(canvas.calculateRestrictedSize(eq(item1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(item1x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(item1x3), any(Vector2i.class))).thenReturn(new Vector2i(10, 15));
        Vector2i availableSize = new Vector2i(C_W, C_H);
        when(canvas.size()).thenReturn(availableSize);
        rowLayout.addWidget(item1x1, null);
        rowLayout.addWidget(item1x2, null);
        rowLayout.addWidget(item1x3, null);
    }
    @Test
    public void testallwidths() throws Exception {
        rowLayout.setColumnRatios(0.4f, 0.5f, 0.1f);
        rowLayout.setHorizontalSpacing(0);
        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(C_W, result.x);
        assertEquals(15, result.y);
        rowLayout.onDraw(canvas);
        final int W_1 = C_W * 4 / 10;
        final int W_2 = C_W / 2;
        final int W_3 = C_W / 10;
        verify(canvas).drawWidget(item1x1, Rect2i.createFromMinAndSize(0, 0, W_1, C_H));
        verify(canvas).drawWidget(item1x2, Rect2i.createFromMinAndSize(W_1, 0, W_2, C_H));
        verify(canvas).drawWidget(item1x3, Rect2i.createFromMinAndSize(W_1 + W_2, 0, W_3, C_H));
    }
    @Test
    public void testnonewidthsrel() throws Exception {
        rowLayout.setHorizontalSpacing(0);
        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(C_W, result.x);
        assertEquals(15, result.y);
        rowLayout.onDraw(canvas);
        verify(canvas).drawWidget(item1x1, Rect2i.createFromMinAndSize(0, 0, C_W / 3, C_H));
        verify(canvas).drawWidget(item1x2, Rect2i.createFromMinAndSize(C_W / 3, 0, C_W / 3, C_H));
        verify(canvas).drawWidget(item1x3, Rect2i.createFromMinAndSize(C_W / 3 + C_W / 3, 0, C_W / 3, C_H));
    }
    @Test
    public void testfewwidths() throws Exception {
        rowLayout.setColumnRatios(0.5f);
        rowLayout.setHorizontalSpacing(0);
        Vector2i result = rowLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(C_W, result.x);
        assertEquals(15, result.y);
        rowLayout.onDraw(canvas);
        final int W_1 = C_W / 2;
        final int W_2 = (C_W - W_1) / 2;
        final int W_3 = (C_W - W_1) / 2;
        verify(canvas).drawWidget(item1x1, Rect2i.createFromMinAndSize(0, 0, W_1, C_H));
        verify(canvas).drawWidget(item1x2, Rect2i.createFromMinAndSize(W_1, 0, W_2, C_H));
        verify(canvas).drawWidget(item1x3, Rect2i.createFromMinAndSize(W_1 + W_2, 0, W_3, C_H));
    }
}