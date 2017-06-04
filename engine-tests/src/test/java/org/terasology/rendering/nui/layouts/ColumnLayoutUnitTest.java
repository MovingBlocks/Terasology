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

public class ColumnLayoutUnitTest {
    private static final int C_H = 200;
    private static final int C_W = 200;

    private ColumnLayout columnLayout;

    private Canvas canvas;

    private UIWidget item1x1;
    private UIWidget item2x1;
    private UIWidget item3x1;
    private UIWidget item1x2;
    private UIWidget item2x2;
    private UIWidget item3x2;

    @Before
    public void setup() {
        columnLayout = new ColumnLayout();

        item1x1 = mock(UIWidget.class);
        item2x1 = mock(UIWidget.class);
        item3x1 = mock(UIWidget.class);
        item1x2 = mock(UIWidget.class);
        item2x2 = mock(UIWidget.class);
        item3x2 = mock(UIWidget.class);

        canvas = mock(Canvas.class);
        when(canvas.calculateRestrictedSize(eq(item1x1), any(Vector2i.class))).thenReturn(new Vector2i(50, 10));
        when(canvas.calculateRestrictedSize(eq(item2x1), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(item3x1), any(Vector2i.class))).thenReturn(new Vector2i(10, 10));
        when(canvas.calculateRestrictedSize(eq(item1x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));
        when(canvas.calculateRestrictedSize(eq(item2x2), any(Vector2i.class))).thenReturn(new Vector2i(5, 5));
        when(canvas.calculateRestrictedSize(eq(item3x2), any(Vector2i.class))).thenReturn(new Vector2i(20, 10));

        Vector2i availableSize = new Vector2i(C_W, C_H);
        when(canvas.size()).thenReturn(availableSize);

        columnLayout.setColumns(3);
        columnLayout.addWidget(item1x1);
        columnLayout.addWidget(item2x1);
        columnLayout.addWidget(item3x1);
        columnLayout.addWidget(item1x2);
        columnLayout.addWidget(item2x2);
        columnLayout.addWidget(item3x2);
    }

    @Test
    public void threecolumnsprop() throws Exception {

        columnLayout.setAutoSizeColumns(false);
        columnLayout.setFillVerticalSpace(false);
        columnLayout.setColumnWidths(0.5f, 0.2f, 0.3f);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(100, result.x);
        assertEquals(20, result.y);

        columnLayout.onDraw(canvas);
        verify(canvas).drawWidget(item1x1, Rect2i.createFromMinAndSize(0, ((C_H - 20) / 2), C_W / 2, 10));
        verify(canvas).drawWidget(item2x1, Rect2i.createFromMinAndSize(C_W / 2, ((C_H - 20) / 2), C_W * 2 / 10, 10)); 
        verify(canvas).drawWidget(item3x1, Rect2i.createFromMinAndSize(C_W / 2 + C_W * 2 / 10, ((C_H - 20) / 2), C_W * 3 / 10, 10));
        verify(canvas).drawWidget(item1x2, Rect2i.createFromMinAndSize(0, ((C_H - 20) / 2) + 10, C_W / 2, 10));
        verify(canvas).drawWidget(item2x2, Rect2i.createFromMinAndSize(C_W / 2, ((C_H - 20) / 2) + 10, C_W * 2 / 10, 10));
        verify(canvas).drawWidget(item3x2, Rect2i.createFromMinAndSize(C_W / 2 + C_W * 2 / 10, ((C_H - 20) / 2) + 10, C_W * 3 / 10, 10));
    }    @Test
    public void threecolumnauto() throws Exception {

        columnLayout.setAutoSizeColumns(true);
        columnLayout.setFillVerticalSpace(false);

        Vector2i result = columnLayout.getPreferredContentSize(canvas, canvas.size());
        assertEquals(75, result.x);
        assertEquals(20, result.y);

        columnLayout.onDraw(canvas);

        verify(canvas).drawWidget(item1x1, Rect2i.createFromMinAndSize(((C_W - 75) / 2), ((C_H - 20) / 2), 50, 10));
        verify(canvas).drawWidget(item2x1, Rect2i.createFromMinAndSize(((C_W - 75) / 2) + 50, ((C_H - 20) / 2), 5, 10));
        verify(canvas).drawWidget(item3x1, Rect2i.createFromMinAndSize(((C_W - 75) / 2) + 50 + 5, ((C_H - 20) / 2), 20, 10));
       verify(canvas).drawWidget(item1x2, Rect2i.createFromMinAndSize(((C_W - 75) / 2), ((C_H - 20) / 2) + 10, 50, 10));
      verify(canvas).drawWidget(item2x2, Rect2i.createFromMinAndSize(((C_W - 75) / 2) + 50, ((C_H - 20) / 2) + 10, 5, 10));
      verify(canvas).drawWidget(item3x2, Rect2i.createFromMinAndSize(((C_W - 75) / 2) + 50 + 5, ((C_H - 20) / 2) + 10, 20, 10));
    }
}