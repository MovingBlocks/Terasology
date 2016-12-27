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
package org.terasology.rendering.nui.widgets;

import org.terasology.input.MouseInput;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

/**
 */
public class UIDoubleSlider extends CoreWidget {
    public static final String SLIDER_PART = "slider";
    public static final String TICKER_LEFT_PART = "tickerLeft";
    public static final String TICKER_RIGHT_PART = "tickerRight";
    @LayoutConfig
    private Binding<Float> minimum = new DefaultBinding<>(0.0f);
    @LayoutConfig
    private Binding<Float> range = new DefaultBinding<>(1.0f);
    @LayoutConfig
    private Binding<Float> increment = new DefaultBinding<>(0.1f);
    @LayoutConfig
    private int precision = 1;
    @LayoutConfig
    private Binding<Float> valueLeft = new DefaultBinding<>(0.3f);
    @LayoutConfig
    private Binding<Float> valueRight = new DefaultBinding<>(0.7f);
    private int sliderWidth;
    private String formatString = "0.0";
    private boolean active;
    private InteractionListener tickerListenerLeft = new BaseInteractionListener() {
        private Vector2i offset = new Vector2i();

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                active = true;
                offset.set(event.getRelativeMousePosition());
                offset.x -= pixelOffsetFor(getValueLeft(), sliderWidth);
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            active = false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            if (sliderWidth > 0) {
                Vector2i pos = event.getRelativeMousePosition();
                int maxSlot = TeraMath.floorToInt(getRange() / getIncrement());
                int slotWidth = sliderWidth / maxSlot;
                int nearestSlot = maxSlot * (pos.x - offset.x + slotWidth / 2) / sliderWidth;
                nearestSlot = TeraMath.clamp(nearestSlot, 0, maxSlot);
                float newValue = TeraMath.clamp(getIncrement() * nearestSlot, 0, getRange()) + getMinimum();
                setValueLeft(newValue);
            }
        }
    };
    private InteractionListener tickerListenerRight = new BaseInteractionListener() {
        private Vector2i offset = new Vector2i();

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                active = true;
                offset.set(event.getRelativeMousePosition());
                offset.x -= pixelOffsetFor(getValueRight(), sliderWidth);
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            active = false;
        }

        @Override
        public void onMouseDrag(NUIMouseDragEvent event) {
            if (sliderWidth > 0) {
                Vector2i pos = event.getRelativeMousePosition();
                int maxSlot = TeraMath.floorToInt(getRange() / getIncrement());
                int slotWidth = sliderWidth / maxSlot;
                int nearestSlot = maxSlot * (pos.x - offset.x + slotWidth / 2) / sliderWidth;
                nearestSlot = TeraMath.clamp(nearestSlot, 0, maxSlot);
                float newValue = TeraMath.clamp(getIncrement() * nearestSlot, 0, getRange()) + getMinimum();
                setValueRight(newValue);
            }
        }
    };

    public UIDoubleSlider() {
    }

    public UIDoubleSlider(String id) {
        super(id);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(SLIDER_PART);
        canvas.drawBackground();

        drawTicker(canvas, TICKER_LEFT_PART, valueLeft, tickerListenerLeft, false);
        drawTicker(canvas, TICKER_RIGHT_PART, valueRight, tickerListenerRight, true);
    }

    private void drawTicker(Canvas canvas, String part, Binding<Float> value, InteractionListener tickerListener, boolean rightTicker) {
        canvas.setPart(part);
        String display = String.format("%." + precision + "f", value.get());
        int tickerWidth = canvas.getCurrentStyle().getFont().getWidth(formatString);
        tickerWidth += canvas.getCurrentStyle().getMargin().getTotalWidth();

        sliderWidth = canvas.size().x - tickerWidth * 2;
        int drawLocation = pixelOffsetFor(value.get(), sliderWidth);
        if (rightTicker) {
            drawLocation += tickerWidth;
        }
        Rect2i tickerRegion = Rect2i.createFromMinAndSize(drawLocation, 0, tickerWidth, canvas.size().y);
        try (SubRegion ignored = canvas.subRegion(tickerRegion, false)) {
            canvas.drawBackground();
            canvas.drawText(display);
            if (isEnabled()) {
                canvas.addInteractionRegion(tickerListener);
            }
        }
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i areaHint) {
        Vector2i result = new Vector2i();
        canvas.setPart(SLIDER_PART);
        result.x = canvas.getCurrentStyle().getFixedWidth();
        if (result.x == 0) {
            result.x = canvas.getCurrentStyle().getMinWidth();
        }
        result.y = canvas.getCurrentStyle().getFixedHeight();
        if (result.y == 0) {
            result.y = canvas.getCurrentStyle().getMinHeight();
        }

        Vector2i left = getTickerPreferredContentSize(canvas, TICKER_LEFT_PART);
        Vector2i right = getTickerPreferredContentSize(canvas, TICKER_RIGHT_PART);

        result.y = Math.max(result.y, Math.max(left.y, right.y));
        result.x = Math.max(result.x, left.x + left.y);
        return result;
    }

    private Vector2i getTickerPreferredContentSize(Canvas canvas, String part) {
        Vector2i result = new Vector2i();

        canvas.setPart(part);
        int tickerWidth = canvas.getCurrentStyle().getFont().getWidth(formatString);
        tickerWidth += canvas.getCurrentStyle().getMargin().getTotalWidth();
        result.x = tickerWidth;

        if (canvas.getCurrentStyle().getFixedWidth() != 0) {
            result.x = Math.max(result.x, canvas.getCurrentStyle().getFixedWidth());
        } else {
            result.x = Math.max(result.x, canvas.getCurrentStyle().getMinWidth());
        }
        if (canvas.getCurrentStyle().getFixedHeight() != 0) {
            result.y = canvas.getCurrentStyle().getFixedHeight();
        } else {
            result.y = canvas.getCurrentStyle().getMinHeight();
        }

        return result;
    }

    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        }

        if (active) {
            return ACTIVE_MODE;
        } else if (tickerListenerLeft.isMouseOver() || tickerListenerRight.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    public void bindMinimum(Binding<Float> binding) {
        this.minimum = binding;
    }

    /**
     * Get the minimum value settable.
     *
     * @return A Float indicating the minimum value.
     */
    public float getMinimum() {
        return minimum.get();
    }

    /**
     * Set the minimum value possible.
     *
     * @param min A Float indicating the minimum value settable.
     */
    public void setMinimum(float min) {
        this.minimum.set(min);
        generateFormatString();
    }

    public void bindRange(Binding<Float> binding) {
        this.range = binding;
    }

    /**
     * The range of possible values (from the minimum value).
     *
     * @return A Float indicating the range of values.
     */
    public float getRange() {
        return range.get();
    }

    /**
     * Set the range of possible values (from the minimum value).
     *
     * @param val A Float specifying the range of values.
     */
    public void setRange(float val) {
        range.set(val);
        generateFormatString();
    }

    public void bindIncrement(Binding<Float> binding) {
        increment = binding;
    }

    /**
     * Get the smallest increment the value can be set to.
     *
     * @return A Float indicating the smallest increment.
     */
    public float getIncrement() {
        return increment.get();
    }

    /**
     * Set the smallest increment the value can be set to.
     *
     * @param val A Float specifying the smallest increment.
     */
    public void setIncrement(float val) {
        increment.set(val);
    }

    public void bindValueLeft(Binding<Float> binding) {
        valueLeft = binding;
    }

    public void bindValueRight(Binding<Float> binding) {
        valueRight = binding;
    }

    /**
     * Get the value of the left bar.
     *
     * @return A Float containing the value of the left bar.
     */
    public float getValueLeft() {
        return valueLeft.get();
    }

    /**
     * Set the value of the left bar.
     * The right bar will adjust as needed such that it is always higher or equal to the left bar.
     *
     * @param val The new value of the left bar
     */
    public void setValueLeft(float val) {
        valueLeft.set(val);

        if (val > valueRight.get()) {
            valueRight.set(val);
        }
    }

    /**
     * Get the value of the right bar.
     *
     * @return A Float containing the value of the right bar.
     */
    public float getValueRight() {
        return valueRight.get();
    }

    /**
     * Set the value of the right bar.
     * The left bar will adjust as needed such that it is always lower or equal to the right bar.
     *
     * @param val The new value of the right bar.
     */
    public void setValueRight(float val) {
        valueRight.set(val);

        if (val < valueLeft.get()) {
            valueLeft.set(val);
        }
    }

    /**
     * Get the number of decimal points displayed on either bar.
     *
     * @return The number of decimal points displayed.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * Set the number of decimal points to display on each bar.
     *
     * @param precision The number of decimal points to display.
     */
    public void setPrecision(int precision) {
        this.precision = precision;
        generateFormatString();
    }

    private void generateFormatString() {
        float maxValue = getRange() + getMinimum();
        int leadingValues = String.format("%.0f", maxValue).length();
        StringBuilder newFormat = new StringBuilder();
        if (getMinimum() < 0) {
            newFormat.append('-');
        }
        for (int i = 0; i < leadingValues; ++i) {
            newFormat.append('0');
        }
        if (precision > 0) {
            newFormat.append('.');
            for (int i = 0; i < precision; ++i) {
                newFormat.append('0');
            }
        }
        formatString = newFormat.toString();
    }

    private int pixelOffsetFor(float val, int width) {
        return TeraMath.floorToInt(width * (val - getMinimum()) / getRange());
    }

}
