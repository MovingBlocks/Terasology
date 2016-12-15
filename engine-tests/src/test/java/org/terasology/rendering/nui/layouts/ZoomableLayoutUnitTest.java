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
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.Canvas;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.terasology.math.TeraMath.ceilToInt;
public class ZoomableLayoutUnitTest {
    private static final int C_W = 100;
    private static final int C_H = 50;
    private static final float W_W = 100;
    private static final float W_H = 100;
    private ZoomableLayout zoomableLayout;
    private Canvas canvas;
    private ZoomableLayout.PositionalWidget i1;
    private ZoomableLayout.PositionalWidget i2;
    private ZoomableLayout.PositionalWidget i3;
    private Vector2f p1;
    private Vector2f p2;
    private Vector2f p3;
    private Vector2f s1;
    private Vector2f s2;
    private Vector2f s3;
    @Before
    public void setup() {
        zoomableLayout = new ZoomableLayout();
        i1 = mock(ZoomableLayout.PositionalWidget.class);
        i2 = mock(ZoomableLayout.PositionalWidget.class);
        i3 = mock(ZoomableLayout.PositionalWidget.class);
        canvas = mock(Canvas.class);
        p1 = new Vector2f(10, 10);
        p2 = new Vector2f(40, 40);
        p3 = new Vector2f(80, 70);
        when(i1.getPosition()).thenReturn(p1);
        when(i2.getPosition()).thenReturn(p2);
        when(i3.getPosition()).thenReturn(p3);
        s1 = new Vector2f(20, 10);
        s2 = new Vector2f(5, 10);
        s3 = new Vector2f(10, 20);
        when(i1.getSize()).thenReturn(s1);
        when(i2.getSize()).thenReturn(s2);
        when(i3.getSize()).thenReturn(s3);
        when(i1.isVisible()).thenReturn(true);
        when(i2.isVisible()).thenReturn(true);
        when(i3.isVisible()).thenReturn(true);
        Vector2i availableSize = new Vector2i(C_W, C_H);
        when(canvas.size()).thenReturn(availableSize);
        zoomableLayout.setWindowSize(new Vector2f(W_W, W_H));
        zoomableLayout.addWidget(i1);
        zoomableLayout.addWidget(i2);
        zoomableLayout.addWidget(i3);
    }
    @Test
    public void testscale() throws Exception {
        zoomableLayout.onDraw(canvas);
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(W_W * 2, W_H));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(C_W, C_H));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(C_W / (W_W * 2), C_H / W_H));
        verify(canvas).drawWidget(i1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p1.x / 2), ceilToInt(p1.y / 2)), new Vector2i(ceilToInt((p1.x + s1.x) / 2), ceilToInt((p1.y + s1.y) / 2))));
        verify(canvas).drawWidget(i2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p2.x / 2), ceilToInt(p2.y / 2)), new Vector2i(ceilToInt((p2.x + s2.x) / 2), ceilToInt((p2.y + s2.y) / 2))));
        verify(canvas).drawWidget(i3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p3.x / 2), ceilToInt(p3.y / 2)), new Vector2i(ceilToInt((p3.x + s3.x) / 2), ceilToInt((p3.y + s3.y) / 2))));
    }
    @Test
    public void testzoomout() throws Exception {
        zoomableLayout.onDraw(canvas);
        zoomableLayout.zoom(2, 2, new Vector2i(0, 0));
        zoomableLayout.onDraw(canvas);
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(W_W * 2 * 2, W_H * 2));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(C_W, C_H));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(C_W / (W_W * 2 * 2), C_H / (W_H * 2)));
        verify(canvas).drawWidget(i1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p1.x / 4), ceilToInt(p1.y / 4)), new Vector2i(ceilToInt((p1.x + s1.x) / 4), ceilToInt((p1.y + s1.y) / 4))));
        verify(canvas).drawWidget(i2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p2.x / 4), ceilToInt(p2.y / 4)), new Vector2i(ceilToInt((p2.x + s2.x) / 4), ceilToInt((p2.y + s2.y) / 4))));
        verify(canvas).drawWidget(i3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p3.x / 4), ceilToInt(p3.y / 4)), new Vector2i(ceilToInt((p3.x + s3.x) / 4), ceilToInt((p3.y + s3.y) / 4))));
    }
    @Test
    public void testzoomin() throws Exception {
        zoomableLayout.onDraw(canvas);
        zoomableLayout.zoom(0.5f, 0.5f, new Vector2i(0, 0));
        zoomableLayout.onDraw(canvas);
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(W_W, W_H / 2));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(C_W, C_H));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(C_W / W_W, C_H / (W_H / 2)));
        verify(canvas).drawWidget(i1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p1.x), ceilToInt(p1.y)), new Vector2i(ceilToInt(p1.x + s1.x), ceilToInt(p1.y + s1.y))));
        verify(canvas).drawWidget(i2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p2.x), ceilToInt(p2.y)), new Vector2i(ceilToInt(p2.x + s2.x), ceilToInt(p2.y + s2.y))));
        verify(canvas).drawWidget(i3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p3.x), ceilToInt(p3.y)), new Vector2i(ceilToInt(p3.x + s3.x), ceilToInt(p3.y + s3.y))));
        zoomableLayout.setWindowPosition(p2);
        zoomableLayout.onDraw(canvas);
        verify(canvas).drawWidget(i1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p1.x - p2.x), ceilToInt(p1.y - p2.y)), new Vector2i(ceilToInt(p1.x + s1.x - p2.x), ceilToInt(p1.y + s1.y - p2.y))));
        verify(canvas).drawWidget(i2, Rect2i.createFromMinAndMax(Vector2i.zero(), new Vector2i(ceilToInt(s2.x), ceilToInt(s2.y))));
        verify(canvas).drawWidget(i3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(p3.x - p2.x), ceilToInt(p3.y - p2.y)), new Vector2i(ceilToInt(p3.x + s3.x - p2.x), ceilToInt(p3.y + s3.y - p2.y))));
    }
}