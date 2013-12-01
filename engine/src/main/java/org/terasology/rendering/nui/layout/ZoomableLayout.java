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
import org.terasology.input.MouseInput;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreLayout;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.UIWidget;

import javax.vecmath.Vector2f;
import java.util.Iterator;
import java.util.List;

/**
 * A layout that allows positioning to a virtual coordinate system, which is mapped to screen coordinates using a
 * viewport.
 *
 * @author synopia
 */
public class ZoomableLayout extends CoreLayout {
    private List<PositionalWidget> widgets = Lists.newArrayList();
    private float pixelSizeX;
    private float pixelSizeY;
    private int screenSizeX;
    private int screenSizeY;
    private float windowPositionX;
    private float windowPositionY;
    private float windowSizeX;
    private float windowSizeY;

    private int lastX;
    private int lastY;
    private InteractionListener dragListener = new BaseInteractionListener() {
        @Override
        public void onMouseOver(Vector2i pos, boolean topMostElement) {
            lastX = pos.x;
            lastY = pos.y;
        }

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            return true;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            float windowPosX = screenToWorldX(lastX) - screenToWorldX(pos.x) + getWindowPositionX();
            float windowPosY = screenToWorldY(lastY) - screenToWorldY(pos.y) + getWindowPositionY();

            setWindowPosition(windowPosX, windowPosY);
        }
    };

    public void addWidget(PositionalWidget widget) {
        widgets.add(widget);
        widget.onAdded(this);
    }

    public void removeWidget(PositionalWidget widget) {
        widget.onRemoved(this);
        widgets.remove(widget);
    }

    public void removeAll() {
        for (PositionalWidget widget : widgets) {
            widget.onRemoved(this);
        }
        widgets.clear();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.addInteractionRegion(dragListener);
        for (PositionalWidget widget : widgets) {
            Vector2f position = widget.getPosition();
            Vector2f size = widget.getSize();
            int sx = worldToScreenX(position.x);
            int sy = worldToScreenY(position.y);
            int ex = worldToScreenX(position.x + size.x);
            int ey = worldToScreenY(position.y + size.y);
            canvas.drawElement(widget, Rect2i.createFromMinAndMax(sx, sy, ex, ey));
        }
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

    public float screenToWorldX(int screenPosX) {
        return screenPosX / pixelSizeX + windowPositionX;
    }

    public float screenToWorldY(int screenPosY) {
        return screenPosY / pixelSizeY + windowPositionY;
    }

    public int worldToScreenX(float worldX) {
        return (int) ((worldX - windowPositionX) * pixelSizeX);
    }

    public int worldToScreenY(float worldY) {
        return (int) ((worldY - windowPositionY) * pixelSizeY);
    }

    public int screenUnitX(float x) {
        int dx = (int) (pixelSizeX * x);

        return Math.max(1, Math.abs(dx));
    }

    public int screenUnitY(float y) {
        int dy = (int) (pixelSizeY * y);

        return Math.max(1, Math.abs(dy));
    }

    public void setWindowPosition(float x, float y) {
        this.windowPositionX = x;
        this.windowPositionY = y;
    }

    public void setWindowSize(float x, float y) {
        this.windowSizeX = x;
        this.windowSizeY = y;
    }

    public void setScreenSize(Vector2i size) {
        this.screenSizeX = size.x;
        this.screenSizeY = size.y;
    }

    public float getPixelSizeX() {
        return pixelSizeX;
    }

    public float getPixelSizeY() {
        return pixelSizeY;
    }

    public int getScreenSizeX() {
        return screenSizeX;
    }

    public int getScreenSizeY() {
        return screenSizeY;
    }

    public float getWindowSizeX() {
        return windowSizeX;
    }

    public float getWindowSizeY() {
        return windowSizeY;
    }

    public float getWindowPositionX() {
        return windowPositionX;
    }

    public float getWindowPositionY() {
        return windowPositionY;
    }

    public int getWindowStartX() {
        return (int) windowPositionX;
    }

    public int getWindowStartY() {
        return (int) windowPositionY;
    }

    public int getWindowEndX() {
        return (int) (windowPositionX + windowSizeX);
    }

    public int getWindowEndY() {
        return (int) (windowPositionY + windowSizeY);
    }

    public void init(float posX, float posY, float sizeX, float sizeY, int screenWidth, int screenHeight) {
        setWindowPosition(posX, posY);
        setWindowSize(sizeX, sizeY);
        setScreenSize(new Vector2i(screenWidth, screenHeight));
        calculateSizes();
    }

    public void calculateSizes() {
        if (windowSizeX > windowSizeY) {
            windowSizeX = windowSizeY;
        }

        if (windowSizeX < windowSizeY) {
            windowSizeY = windowSizeX;
        }

        if ((screenSizeX != 0) && (screenSizeY != 0)) {
            if (screenSizeX > screenSizeY) {
                windowSizeX *= (float) screenSizeX / screenSizeY;
            } else {
                windowSizeY *= (float) screenSizeY / screenSizeX;
            }
        }

        if ((windowSizeX > 0) && (windowSizeY > 0)) {
            pixelSizeX = screenSizeX / windowSizeX;
            pixelSizeY = screenSizeY / windowSizeY;
        } else {
            pixelSizeX = 0;
            pixelSizeY = 0;
        }
    }

    public void zoom(float zoomX, float zoomY, Vector2i mousePos) {
        float midX = windowPositionX + windowSizeX / 2.f;
        float midY = windowPositionY + windowSizeY / 2.f;
        float posX = screenToWorldX(mousePos.x);
        float posY = screenToWorldY(mousePos.y);

        windowSizeX *= zoomX;
        windowSizeY *= zoomY;
        calculateSizes();
        windowPositionX += (posX - midX) * 0.1;
        windowPositionY += (posY - midY) * 0.1;
    }


    public interface PositionalWidget extends UIWidget {
        Vector2f getPosition();

        Vector2f getSize();

        void onAdded(ZoomableLayout layout);

        void onRemoved(ZoomableLayout layout);
    }
}
