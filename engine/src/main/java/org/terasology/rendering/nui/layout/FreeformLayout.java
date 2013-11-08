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
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.UIWidget;

import javax.vecmath.Vector2f;
import java.util.List;

/**
 * This layout allows positioning widgets centered on different relative locations, with either fixed or relative size
 * @author Immortius
 */
public class FreeformLayout extends AbstractWidget {

    private List<PositionedWidget> contents = Lists.newArrayList();

    public void addWidget(UIWidget widget, Vector2f center, Vector2f relativeSize) {
        contents.add(new RelativeWidget(widget, center, relativeSize));
    }

    public void addWidget(UIWidget widget, Vector2f center, Vector2i fixedSize) {
        contents.add(new AbsoluteWidget(widget, center, fixedSize));
    }

    @Override
    public void onDraw(Canvas canvas) {
        for (PositionedWidget widget : contents) {
            Vector2i size = widget.getSize(canvas.size());
            Vector2i position = new Vector2i(TeraMath.floorToInt(widget.center.x * canvas.size().x), TeraMath.floorToInt(widget.center.y * canvas.size().y));
            position.x -= size.x / 2;
            position.y -= size.y / 2;
            canvas.drawWidget(widget.widget, Rect2i.createFromMinAndSize(position, size));
        }
    }

    @Override
    public void update(float delta) {
        for (PositionedWidget widget : contents) {
            widget.widget.update(delta);
        }
    }

    private abstract static class PositionedWidget {
        private UIWidget widget;
        private Vector2f center;

        public PositionedWidget(UIWidget widget, Vector2f center) {
            this.widget = widget;
            this.center = center;
        }

        public UIWidget getWidget() {
            return widget;
        }

        public Vector2f getCenter() {
            return center;
        }

        public abstract Vector2i getSize(Vector2i canvasSize);
    }

    private static class RelativeWidget extends PositionedWidget {
        private Vector2f size;

        public RelativeWidget(UIWidget widget, Vector2f center, Vector2f size) {
            super(widget, center);
            this.size = size;
        }

        @Override
        public Vector2i getSize(Vector2i canvasSize) {
            return new Vector2i(TeraMath.ceilToInt(canvasSize.x * size.x), TeraMath.ceilToInt(canvasSize.y * size.y));
        }
    }

    private static class AbsoluteWidget extends PositionedWidget {
        private Vector2i size;

        public AbsoluteWidget(UIWidget widget, Vector2f center, Vector2i size) {
            super(widget, center);
            this.size = size;
        }

        @Override
        public Vector2i getSize(Vector2i canvasSize) {
            return size;
        }
    }


}
