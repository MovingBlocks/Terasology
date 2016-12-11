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

public class ZoomableLayoutTest {
    private static final int CANVAS_WIDTH = 100;
    private static final int CANVAS_HEIGHT = 50;
    private static final float WORLD_WIDTH = 100;
    private static final float WORLD_HEIGHT = 100;

    private ZoomableLayout zoomableLayout;

    private Canvas canvas;

    private ZoomableLayout.PositionalWidget item1;
    private ZoomableLayout.PositionalWidget item2;
    private ZoomableLayout.PositionalWidget item3;

    private Vector2f pos1;
    private Vector2f pos2;
    private Vector2f pos3;

    private Vector2f size1;
    private Vector2f size2;
    private Vector2f size3;

    @Before
    public void setup() {
        zoomableLayout = new ZoomableLayout();

        item1 = mock(ZoomableLayout.PositionalWidget.class);
        item2 = mock(ZoomableLayout.PositionalWidget.class);
        item3 = mock(ZoomableLayout.PositionalWidget.class);

        canvas = mock(Canvas.class);

        //    
        //   +------+
        //   |  1   |
        //   +------+
        //             +-+
        //             |2|
        //             +-+
        //
        //                        +---+
        //                        | 3 |
        //                        |   |
        //                        +---+

        //positions of the widgets in the world
        pos1 = new Vector2f(10, 10);
        pos2 = new Vector2f(40, 40);
        pos3 = new Vector2f(80, 70);

        when(item1.getPosition()).thenReturn(pos1);
        when(item2.getPosition()).thenReturn(pos2);
        when(item3.getPosition()).thenReturn(pos3);

        //size of widgets
        size1 = new Vector2f(20, 10);
        size2 = new Vector2f(5, 10);
        size3 = new Vector2f(10, 20);

        when(item1.getSize()).thenReturn(size1);
        when(item2.getSize()).thenReturn(size2);
        when(item3.getSize()).thenReturn(size3);

        when(item1.isVisible()).thenReturn(true);
        when(item2.isVisible()).thenReturn(true);
        when(item3.isVisible()).thenReturn(true);

        Vector2i availableSize = new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT);
        when(canvas.size()).thenReturn(availableSize);
        zoomableLayout.setWindowSize(new Vector2f(WORLD_WIDTH, WORLD_HEIGHT));

        zoomableLayout.addWidget(item1);
        zoomableLayout.addWidget(item2);
        zoomableLayout.addWidget(item3);

    }

    @Test
    public void testScaling() throws Exception {

        zoomableLayout.onDraw(canvas);

        //world size scaled to fit ratio of screen size - world size now 200 x 100
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(WORLD_WIDTH * 2, WORLD_HEIGHT));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(CANVAS_WIDTH / (WORLD_WIDTH * 2), CANVAS_HEIGHT / WORLD_HEIGHT));

        //coordinates on widgets scaled down by half
        verify(canvas).drawWidget(item1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos1.x / 2), ceilToInt(pos1.y / 2)), new Vector2i(ceilToInt((pos1.x + size1.x) / 2), ceilToInt((pos1.y + size1.y) / 2))));
        verify(canvas).drawWidget(item2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos2.x / 2), ceilToInt(pos2.y / 2)), new Vector2i(ceilToInt((pos2.x + size2.x) / 2), ceilToInt((pos2.y + size2.y) / 2))));
        verify(canvas).drawWidget(item3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos3.x / 2), ceilToInt(pos3.y / 2)), new Vector2i(ceilToInt((pos3.x + size3.x) / 2), ceilToInt((pos3.y + size3.y) / 2))));

    }

    @Test
    public void testZoomOut() throws Exception {

        zoomableLayout.onDraw(canvas);

        //zoom out 2x from top left corner
        zoomableLayout.zoom(2, 2, new Vector2i(0, 0));
        zoomableLayout.onDraw(canvas);

        //world size doubled
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(WORLD_WIDTH * 2 * 2, WORLD_HEIGHT * 2));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(CANVAS_WIDTH / (WORLD_WIDTH * 2 * 2), CANVAS_HEIGHT / (WORLD_HEIGHT * 2)));

        verify(canvas).drawWidget(item1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos1.x / 4), ceilToInt(pos1.y / 4)), new Vector2i(ceilToInt((pos1.x + size1.x) / 4), ceilToInt((pos1.y + size1.y) / 4))));
        verify(canvas).drawWidget(item2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos2.x / 4), ceilToInt(pos2.y / 4)), new Vector2i(ceilToInt((pos2.x + size2.x) / 4), ceilToInt((pos2.y + size2.y) / 4))));
        verify(canvas).drawWidget(item3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos3.x / 4), ceilToInt(pos3.y / 4)), new Vector2i(ceilToInt((pos3.x + size3.x) / 4), ceilToInt((pos3.y + size3.y) / 4))));

    }

    @Test
    public void testZoomInAndDrag() throws Exception {

        zoomableLayout.onDraw(canvas);

        //zoom in 2x towards left top corner
        zoomableLayout.zoom(0.5f, 0.5f, new Vector2i(0, 0));
        zoomableLayout.onDraw(canvas);

        //world size halved
        assertEquals(zoomableLayout.getWindowSize(), new Vector2f(WORLD_WIDTH, WORLD_HEIGHT / 2));
        assertEquals(zoomableLayout.getScreenSize(), new Vector2i(CANVAS_WIDTH, CANVAS_HEIGHT));
        assertEquals(zoomableLayout.getPixelSize(), new Vector2f(CANVAS_WIDTH / WORLD_WIDTH, CANVAS_HEIGHT / (WORLD_HEIGHT / 2)));

        verify(canvas).drawWidget(item1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos1.x), ceilToInt(pos1.y)), new Vector2i(ceilToInt(pos1.x + size1.x), ceilToInt(pos1.y + size1.y))));
        verify(canvas).drawWidget(item2, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos2.x), ceilToInt(pos2.y)), new Vector2i(ceilToInt(pos2.x + size2.x), ceilToInt(pos2.y + size2.y))));
        verify(canvas).drawWidget(item3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos3.x), ceilToInt(pos3.y)), new Vector2i(ceilToInt(pos3.x + size3.x), ceilToInt(pos3.y + size3.y))));

        //simulate drag to item2
        zoomableLayout.setWindowPosition(pos2);
        zoomableLayout.onDraw(canvas);

        //item1 out of canvas
        verify(canvas).drawWidget(item1, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos1.x - pos2.x), ceilToInt(pos1.y - pos2.y)), new Vector2i(ceilToInt(pos1.x + size1.x - pos2.x), ceilToInt(pos1.y + size1.y - pos2.y))));
        verify(canvas).drawWidget(item2, Rect2i.createFromMinAndMax(Vector2i.zero(), new Vector2i(ceilToInt(size2.x), ceilToInt(size2.y))));
        verify(canvas).drawWidget(item3, Rect2i.createFromMinAndMax(new Vector2i(ceilToInt(pos3.x - pos2.x), ceilToInt(pos3.y - pos2.y)), new Vector2i(ceilToInt(pos3.x + size3.x - pos2.x), ceilToInt(pos3.y + size3.y - pos2.y))));
    }

}
