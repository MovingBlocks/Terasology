/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering.nui.layout;

import com.google.common.collect.Lists;
import org.terasology.math.Rect2f;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * @author Immortius
 */
public class ArbitraryLayout extends AbstractWidget {

    private List<WidgetInfo> widgets = Lists.newArrayList();

    public void addFixedWidget(UIWidget widget, Vector2i fixedSize, Vector2f position) {
        widgets.add(new CenteredWidgetInfo(widget, fixedSize, position));
    }

    public void addFillWidget(UIWidget widget, Rect2f region) {
        widgets.add(new RegionedWidgetInfo(widget, region));
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (WidgetInfo widget : widgets) {
            widget.draw(canvas);
        }
    }

    @Override
    public void update(float delta) {
        for (WidgetInfo widgetInfo : widgets) {
            widgetInfo.widget.update(delta);
        }
    }

    private abstract static class WidgetInfo {
        private UIWidget widget;

        public WidgetInfo(UIWidget widget) {
            this.widget = widget;
        }

        private UIWidget getWidget() {
            return widget;
        }

        public void draw(Canvas canvas) {
            canvas.drawWidget(widget, Rect2i.createFromMinAndSize(getPosition(canvas.size()), getSize(canvas.size())));
        }

        public abstract Vector2i getSize(Vector2i canvasSize);

        public abstract Vector2i getPosition(Vector2i canvasSize);
    }

    private static class RegionedWidgetInfo extends WidgetInfo  {
        private Rect2f region;

        public RegionedWidgetInfo(UIWidget widget, Rect2f region) {
            super(widget);
            this.region = region;
        }

        @Override
        public Vector2i getSize(Vector2i canvasSize) {
            return new Vector2i(TeraMath.floorToInt(canvasSize.x * region.width()), TeraMath.floorToInt(canvasSize.y * region.height()));
        }

        @Override
        public Vector2i getPosition(Vector2i canvasSize) {
            return new Vector2i(TeraMath.ceilToInt(canvasSize.x * region.minX()), TeraMath.floorToInt(canvasSize.y * region.minY()));
        }
    }

    private abstract static class FixedSizedWidgetInfo extends WidgetInfo {
        private Vector2i size;

        public FixedSizedWidgetInfo(UIWidget widget, Vector2i size) {
            super(widget);
            this.size = size;
        }

        @Override
        public Vector2i getSize(Vector2i canvasSize) {
            return size;
        }
    }

    private static class CenteredWidgetInfo extends FixedSizedWidgetInfo {
        private Vector2f position;

        public CenteredWidgetInfo(UIWidget widget, Vector2i size, Vector2f position) {
            super(widget, size);
            this.position = position;
        }

        @Override
        public Vector2i getPosition(Vector2i canvasSize) {
            int x = TeraMath.ceilToInt(canvasSize.x * position.x) - getSize(canvasSize).x / 2;
            int y = TeraMath.ceilToInt(canvasSize.y * position.y) - getSize(canvasSize).y / 2;
            return new Vector2i(x, y);
        }
    }

    private static class Constraint {

    }
}
