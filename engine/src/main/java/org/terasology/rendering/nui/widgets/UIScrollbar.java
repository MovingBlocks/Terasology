/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.widgets;

import org.terasology.input.MouseInput;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

/**
 */
public class UIScrollbar extends CoreWidget {

    @LayoutConfig
    private Binding<Integer> minimum = new DefaultBinding<>(0);
    @LayoutConfig
    private Binding<Integer> range = new DefaultBinding<>(100);

    private Binding<Integer> value = new DefaultBinding<>(0);

    @LayoutConfig
    private boolean vertical;

    private int sliderSize;
    private int handleSize;
    private boolean dragging;
    private int mouseOffset;

    private InteractionListener handleListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                dragging = true;
                Vector2i pos = event.getRelativeMousePosition();
                if (vertical) {
                    mouseOffset = pos.y - pixelOffsetFor(getValue());
                } else {
                    mouseOffset = pos.x - pixelOffsetFor(getValue());
                }
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            dragging = false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2i pos = event.getRelativeMousePosition();
            if (vertical) {
                updatePosition(pos.y - mouseOffset);
            } else {
                updatePosition(pos.x - mouseOffset);
            }
        }
    };

    private InteractionListener sliderListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                Vector2i pos = event.getRelativeMousePosition();
                mouseOffset = (sliderSize > handleSize) ? (handleSize / 2) : 0;
                if (vertical) {
                    updatePosition(pos.y - mouseOffset);

                    setValue(TeraMath.clamp(pos.y - mouseOffset, 0, sliderSize) * getRange() / sliderSize);
                } else {
                    updatePosition(pos.x - mouseOffset);

                    setValue(TeraMath.clamp(pos.x - mouseOffset, 0, sliderSize) * getRange() / sliderSize);
                }
                dragging = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            Vector2i pos = event.getRelativeMousePosition();
            if (vertical) {
                updatePosition(pos.y - mouseOffset);
            } else {
                updatePosition(pos.x - mouseOffset);
            }
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            dragging = false;
        }
    };

    public UIScrollbar() {
        this(true);
    }

    public UIScrollbar(boolean vertical) {
        this.vertical = vertical;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (vertical) {
            canvas.setPart("sliderVertical");
        } else {
            canvas.setPart("sliderHorizontal");
        }
        canvas.drawBackground();
        canvas.addInteractionRegion(sliderListener);

        canvas.setPart("handle");
        if (vertical) {
            sliderSize = canvas.size().y - canvas.getCurrentStyle().getFixedHeight();
        } else {
            sliderSize = canvas.size().x - canvas.getCurrentStyle().getFixedWidth();
        }

        if (sliderSize > handleSize) {
            int drawLocation = pixelOffsetFor(getValue());
            Rect2i handleRegion;
            if (vertical) {
                handleSize = canvas.getCurrentStyle().getFixedHeight();
                handleRegion = Rect2i.createFromMinAndSize(0, drawLocation, canvas.getCurrentStyle().getFixedWidth(), handleSize);
            } else {
                handleSize = canvas.getCurrentStyle().getFixedWidth();
                handleRegion = Rect2i.createFromMinAndSize(drawLocation, 0, handleSize, canvas.getCurrentStyle().getFixedHeight());
            }
            canvas.drawBackground(handleRegion);
            canvas.addInteractionRegion(handleListener, handleRegion);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        canvas.setPart("handle");
        int x = canvas.getCurrentStyle().getFixedWidth();
        if (x == 0) {
            x = canvas.getCurrentStyle().getMinWidth();
        }
        int y = canvas.getCurrentStyle().getFixedHeight();
        if (y == 0) {
            y = canvas.getCurrentStyle().getMinHeight();
        }
        return new Vector2i(x, y);
    }

    private int pixelOffsetFor(int newValue) {
        final int r = getRange();
        return (r > 0) ? (sliderSize * newValue / r) : 0;
    }

    @Override
    public String getMode() {
        if (dragging) {
            return ACTIVE_MODE;
        } else if (handleListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    public void bindMinimum(Binding<Integer> binding) {
        minimum = binding;
    }

    public int getMinimum() {
        return minimum.get();
    }

    public void setMinimum(int val) {
        minimum.set(val);
    }

    public void bindRange(Binding<Integer> binding) {
        range = binding;
    }

    public int getRange() {
        return range.get();
    }

    public void setRange(int val) {
        range.set(val);
    }

    public void bindValue(Binding<Integer> binding) {
        value = binding;
    }

    public int getValue() {
        return TeraMath.clamp(value.get(), getMinimum(), getMinimum() + getRange());
    }

    public void setValue(int val) {
        value.set(val);
    }

    private void updatePosition(int pixelPos) {
        int newPosition = TeraMath.clamp(pixelPos, 0, sliderSize);
        setValue((sliderSize > 0) ? (newPosition * getRange() / sliderSize) : 0);
    }

}
