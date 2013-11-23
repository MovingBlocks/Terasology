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
import org.terasology.rendering.nui.AbstractWidget;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

/**
 * @author Immortius
 */
public class UISlider extends AbstractWidget {
    public static final String HOVER_MODE = "hover";
    public static final String ACTIVE_MODE = "active";
    public static final String SLIDER = "slider";
    public static final String TICKER = "ticker";

    private InteractionListener tickerListener = new BaseInteractionListener() {
        private Vector2i offset = new Vector2i();

        @Override
        public boolean onMouseClick(MouseInput button, Vector2i pos) {
            if (button == MouseInput.MOUSE_LEFT) {
                active = true;
                offset.set(pos);
                offset.x -= pixelOffsetFor(getValue(), sliderWidth);
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(MouseInput button, Vector2i pos) {
            active = false;
        }

        @Override
        public void onMouseDrag(Vector2i pos) {
            int maxSlot = TeraMath.floorToInt(getRange() / getIncrement());
            int slotWidth = sliderWidth / maxSlot;
            int nearestSlot = maxSlot * (pos.x - offset.x + slotWidth / 2) / sliderWidth;
            nearestSlot = TeraMath.clamp(nearestSlot, 0, maxSlot);
            float newValue = TeraMath.clamp(getIncrement() * nearestSlot, 0, getRange()) + getMinimum();
            setValue(newValue);
        }
    };
    private boolean active;

    private Binding<Float> minimum = new DefaultBinding<>(0.0f);
    private Binding<Float> range = new DefaultBinding<>(1.0f);
    private Binding<Float> increment = new DefaultBinding<>(0.1f);
    private Binding<Float> value = new DefaultBinding<>(0.7f);

    private int precision = 1;

    private int sliderWidth;

    public UISlider() {
    }

    public UISlider(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(SLIDER);
        canvas.drawBackground();

        canvas.setPart(TICKER);
        String display = String.format("%." + precision + "f", value.get());
        int tickerWidth = canvas.getCurrentStyle().getFont().getWidth(display);
        tickerWidth += canvas.getCurrentStyle().getMargin().getTotalWidth();

        sliderWidth = canvas.size().x - tickerWidth;
        int drawLocation = pixelOffsetFor(getValue(), sliderWidth);
        Rect2i tickerRegion = Rect2i.createFromMinAndSize(drawLocation, 0, tickerWidth, canvas.size().y);
        canvas.drawBackground(tickerRegion);
        canvas.drawText(display, tickerRegion);
        canvas.addInteractionRegion(tickerListener, tickerRegion);
    }

    @Override
    public String getMode() {
        if (active) {
            return ACTIVE_MODE;
        } else if (tickerListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    public void bindMinimum(Binding<Float> binding) {
        this.minimum = binding;
    }

    public float getMinimum() {
        return minimum.get();
    }

    public void setMinimum(float min) {
        this.minimum.set(min);
    }

    public void bindRange(Binding<Float> binding) {
        this.range = binding;
    }

    public float getRange() {
        return range.get();
    }

    public void setRange(float val) {
        range.set(val);
    }

    public void bindIncrement(Binding<Float> binding) {
        increment = binding;
    }

    public float getIncrement() {
        return increment.get();
    }

    public void setIncrement(float val) {
        increment.set(val);
    }

    public void bindValue(Binding<Float> binding) {
        value = binding;
    }

    public float getValue() {
        return value.get();
    }

    public void setValue(float val) {
        value.set(val);
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    private int pixelOffsetFor(float val, int width) {
        return TeraMath.floorToInt(width * (val - getMinimum()) / getRange());
    }
}
