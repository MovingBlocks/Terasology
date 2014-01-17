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
package org.terasology.rendering.nui.baseWidgets;

import org.terasology.input.MouseInput;
import org.terasology.math.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public class UIScrollbar extends CoreWidget {

    private Binding<Integer> minimum = new DefaultBinding<>(0);
    private Binding<Integer> range = new DefaultBinding<>(100);
    private Binding<Integer> value = new DefaultBinding<>(0);

    private int sliderSize;
    private int handleSize;
    private boolean dragging;
    private int mouseOffset;

    private InteractionListener handleListener = new BaseInteractionListener() {

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                dragging = true;
                mouseOffset = pos.y - pixelOffsetFor(getValue());
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            dragging = false;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            updatePosition(pos.y - mouseOffset);
        }
    };

    private InteractionListener sliderListener = new BaseInteractionListener() {
        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                setValue(TeraMath.clamp(pos.y - handleSize / 2, 0, sliderSize) * getRange() / sliderSize);
                mouseOffset = handleSize / 2;
                dragging = true;
                return true;
            }
            return false;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            updatePosition(pos.y - mouseOffset);
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            dragging = false;
        }
    };

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart("slider");
        canvas.drawBackground();
        canvas.addInteractionRegion(sliderListener);

        canvas.setPart("handle");
        sliderSize = canvas.size().y - canvas.getCurrentStyle().getFixedHeight();

        if (sliderSize > 0) {
            int drawLocation = pixelOffsetFor(getValue());
            handleSize = canvas.getCurrentStyle().getFixedHeight();
            Rect2i handleRegion = Rect2i.createFromMinAndSize(0, drawLocation, canvas.getCurrentStyle().getFixedWidth(), handleSize);
            canvas.drawBackground(handleRegion);
            canvas.addInteractionRegion(handleListener, handleRegion);
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        return new Vector2i(0, sizeHint.y);
    }

    private int pixelOffsetFor(int newValue) {
        return sliderSize * newValue / getRange();
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
        setValue(newPosition * getRange() / sliderSize);
    }

}
