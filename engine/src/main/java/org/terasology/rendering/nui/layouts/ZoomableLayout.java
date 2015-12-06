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

import com.google.common.collect.Lists;

import org.terasology.input.Keyboard;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutHint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseOverEvent;
import org.terasology.rendering.nui.events.NUIMouseWheelEvent;

import java.util.Iterator;
import java.util.List;

/**
 * A layout that allows positioning to a virtual coordinate system, which is mapped to screen coordinates using a
 * viewport.
 *
 */
public class ZoomableLayout extends CoreLayout {
    private List<PositionalWidget> widgets = Lists.newArrayList();
    private Vector2f pixelSize;
    private Vector2i screenSize;
    private Vector2f windowPosition = new Vector2f();
    private Vector2f windowSize = new Vector2f(50, 50);

    private Vector2i last;

    private InteractionListener dragListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(NUIMouseOverEvent event) {
            last = new Vector2i(event.getRelativeMousePosition());
        }

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            return true;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2f p = screenToWorld(last);
            p.sub(screenToWorld(event.getRelativeMousePosition()));
            p.add(windowPosition);

            setWindowPosition(p);
        }

        @Override
        public boolean onMouseWheel(NUIMouseWheelEvent event) {
            if (event.getKeyboard().isKeyDown(Keyboard.Key.LEFT_SHIFT.getId())) {
                float scale = 1 + event.getWheelTurns() * 0.05f;
                zoom(scale, scale, event.getRelativeMousePosition());
            }
            return false;
        }
    };

    public ZoomableLayout() {
    }

    public ZoomableLayout(String id) {
        super(id);
    }

    @Override
    public void addWidget(UIWidget element, LayoutHint hint) {
        if (element instanceof PositionalWidget) {
            PositionalWidget positionalWidget = (PositionalWidget) element;
            addWidget(positionalWidget);
        }
    }

    public void addWidget(PositionalWidget widget) {
        if (widget != null) {
            widgets.add(widget);
            widget.onAdded(this);
        }
    }

    @Override
    public void removeWidget(UIWidget element) {
        if (element instanceof PositionalWidget) {
            PositionalWidget positionalWidget = (PositionalWidget) element;
            removeWidget(positionalWidget);
        }
    }

    public void removeWidget(PositionalWidget widget) {
        if (widget != null) {
            widget.onRemoved(this);
            widgets.remove(widget);
        }
    }

    public void removeAll() {
        for (PositionalWidget widget : widgets) {
            widget.onRemoved(this);
        }
        widgets.clear();
    }

    @Override
    public void onDraw(Canvas canvas) {
        setScreenSize(canvas.size());
        calculateSizes();

        canvas.addInteractionRegion(dragListener);
        for (PositionalWidget widget : widgets) {
            if (!widget.isVisible()) {
                continue;
            }
            Vector2i screenStart = worldToScreen(widget.getPosition());
            Vector2f worldEnd = new Vector2f(widget.getPosition());
            worldEnd.add(widget.getSize());
            Vector2i screenEnd = worldToScreen(worldEnd);
            canvas.drawWidget(widget, Rect2i.createFromMinAndMax(screenStart, screenEnd));
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return Vector2i.zero();
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return new Vector2i(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public void update(float delta) {
        for (PositionalWidget widget : widgets) {
            widget.update(delta);
        }
    }

    public List<PositionalWidget> getWidgets() {
        return widgets;
    }

    @Override
    public Iterator<UIWidget> iterator() {
        return new Iterator<UIWidget>() {
            private Iterator<PositionalWidget> delegate = widgets.iterator();

            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public UIWidget next() {
                return delegate.next();
            }

            @Override
            public void remove() {
                delegate.remove();
            }
        };
    }

    public Vector2f screenToWorld(Vector2i screenPos) {
        Vector2f world = new Vector2f(screenPos.x / pixelSize.x, screenPos.y / pixelSize.y);
        world.add(windowPosition);
        return world;
    }

    public Vector2i worldToScreen(Vector2f world) {
        return new Vector2i(TeraMath.ceilToInt((world.x - windowPosition.x) * pixelSize.x), TeraMath.ceilToInt((world.y - windowPosition.y) * pixelSize.y));
    }

    public void setWindowPosition(Vector2f pos) {
        windowPosition = pos;
    }

    public void setWindowSize(Vector2f size) {
        windowSize = size;
    }

    public void setScreenSize(Vector2i size) {
        screenSize = size;
    }

    public Vector2f getPixelSize() {
        return pixelSize;
    }

    public Vector2i getScreenSize() {
        return screenSize;
    }

    public Vector2f getWindowPosition() {
        return windowPosition;
    }

    public Vector2f getWindowSize() {
        return windowSize;
    }

    public void calculateSizes() {
        if (windowSize.x > windowSize.y) {
            windowSize.x = windowSize.y;
        }

        if (windowSize.x < windowSize.y) {
            windowSize.y = windowSize.x;
        }

        if ((screenSize.x != 0) && (screenSize.y != 0)) {
            if (screenSize.x > screenSize.y) {
                windowSize.x *= (float) screenSize.x / screenSize.y;
            } else {
                windowSize.y *= (float) screenSize.y / screenSize.x;
            }
        }

        if ((windowSize.x > 0) && (windowSize.y > 0)) {
            pixelSize = new Vector2f(screenSize.x / windowSize.x, screenSize.y / windowSize.y);
        } else {
            pixelSize = new Vector2f();
        }
    }

    public void zoom(float zoomX, float zoomY, Vector2i mousePos) {
        Vector2f mouseBefore = screenToWorld(mousePos);

        windowSize.x *= zoomX;
        windowSize.y *= zoomY;
        calculateSizes();

        Vector2f mouseAfter = screenToWorld(mousePos);

        windowPosition.x -= mouseAfter.x - mouseBefore.x;
        windowPosition.y -= mouseAfter.y - mouseBefore.y;
    }


    public interface PositionalWidget<L extends ZoomableLayout> extends UIWidget {
        Vector2f getPosition();

        Vector2f getSize();

        void onAdded(L layout);

        void onRemoved(L layout);
    }
}
