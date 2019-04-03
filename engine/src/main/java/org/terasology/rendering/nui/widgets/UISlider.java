/*
 * Copyright 2016 MovingBlocks
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

import com.google.common.base.Function;
import org.terasology.input.Keyboard;
import org.terasology.input.MouseInput;
import org.terasology.input.events.MouseWheelEvent;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.ActivatableWidget;
import org.terasology.rendering.nui.BaseInteractionListener;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.InteractionListener;
import org.terasology.rendering.nui.LayoutConfig;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.TabbingManager;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.events.NUIKeyEvent;
import org.terasology.rendering.nui.events.NUIMouseClickEvent;
import org.terasology.rendering.nui.events.NUIMouseDragEvent;
import org.terasology.rendering.nui.events.NUIMouseReleaseEvent;

/**
 * A simple value slider bar with one handle
 */
public class UISlider extends ActivatableWidget {
    public static final String SLIDER = "slider";
    public static final String TICKER = "ticker";

    private UISliderOnChangeTriggeredListener uiSliderOnChangeTriggeredListener;

    private InteractionListener tickerListener = new BaseInteractionListener() {
        private Vector2i offset = new Vector2i();

        @Override
        public boolean onMouseClick(NUIMouseClickEvent event) {
            if (event.getMouseButton() == MouseInput.MOUSE_LEFT) {
                active = true;
                offset.set(event.getRelativeMousePosition());
                offset.x -= pixelOffsetFor(getValue(), sliderWidth);
                return true;
            }
            return false;
        }

        @Override
        public void onMouseRelease(NUIMouseReleaseEvent event) {
            active = false;
            if (uiSliderOnChangeTriggeredListener != null) {
                uiSliderOnChangeTriggeredListener.onSliderValueChanged(getValue());
            }
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
                setValue(newValue);
            }
        }
    };

    @LayoutConfig
    private Binding<Float> minimum = new DefaultBinding<>(0.0f);

    @LayoutConfig
    private Binding<Float> range = new DefaultBinding<>(1.0f);

    @LayoutConfig
    private Binding<Float> increment = new DefaultBinding<>(0.1f);

    @LayoutConfig
    private int precision = 1;

    @LayoutConfig
    private Binding<Float> value = new DefaultBinding<>(0.7f);

    private int sliderWidth;
    private boolean active;
    private Function<? super Float, String> labelFunction;

    public UISlider() {
    }

    public UISlider(String id) {
        super(id);
    }

    private String getDisplayText() {
        if (labelFunction != null) {
            return labelFunction.apply(value.get());
        } else {
            return String.format("%." + precision + "f", value.get());
        }
    }

    public void setLabelFunction(Function<? super Float, String> labelFunction) {
        this.labelFunction = labelFunction;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.setPart(SLIDER);
        canvas.drawBackground();

        canvas.setPart(TICKER);
        String display = getDisplayText();
        int tickerWidth = canvas.getCurrentStyle().getFont().getWidth(display);
        tickerWidth += canvas.getCurrentStyle().getMargin().getTotalWidth();

        sliderWidth = canvas.size().x - tickerWidth;
        int drawLocation = pixelOffsetFor(getValue(), sliderWidth);
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
        canvas.setPart(SLIDER);
        result.x = canvas.getCurrentStyle().getFixedWidth();
        if (result.x == 0) {
            result.x = canvas.getCurrentStyle().getMinWidth();
        }
        result.y = canvas.getCurrentStyle().getFixedHeight();
        if (result.y == 0) {
            result.y = canvas.getCurrentStyle().getMinHeight();
        }

        canvas.setPart(TICKER);
        int tickerWidth = canvas.getCurrentStyle().getFont().getWidth(getDisplayText());
        tickerWidth += canvas.getCurrentStyle().getMargin().getTotalWidth();
        result.x = Math.max(result.x, tickerWidth);
        if (canvas.getCurrentStyle().getFixedWidth() != 0) {
            result.x = Math.max(result.x, canvas.getCurrentStyle().getFixedWidth());
        } else {
            result.x = Math.max(result.x, canvas.getCurrentStyle().getMinWidth());
        }
        if (canvas.getCurrentStyle().getFixedHeight() != 0) {
            result.y = Math.max(result.y, canvas.getCurrentStyle().getFixedHeight());
        } else {
            result.y = Math.max(result.y, canvas.getCurrentStyle().getMinHeight());
        }
        return result;
    }

    @Override
    public String getMode() {
        if (!isEnabled()) {
            return DISABLED_MODE;
        }

        if (active || this.equals(TabbingManager.focusedWidget)) {
            return ACTIVE_MODE;
        } else if (tickerListener.isMouseOver()) {
            return HOVER_MODE;
        }
        return DEFAULT_MODE;
    }

    private void changeValue(float delta) {
        float newValue = TeraMath.clamp(getValue() + delta, getMinimum(), getRange() + getMinimum());
        setValue(newValue);
    }

    @Override
    public boolean onKeyEvent(NUIKeyEvent event) {
        if (event.isDown() && this.equals(TabbingManager.focusedWidget)) {
            int keyId = event.getKey().getId();
            if (keyId == Keyboard.KeyId.RIGHT || keyId == Keyboard.KeyId.UP) {
                this.changeValue(getIncrement());
                return true;
            } else if (keyId == Keyboard.KeyId.LEFT || keyId == Keyboard.KeyId.DOWN) {
                this.changeValue(-1 * getIncrement());
                return true;
            }
        }
        return super.onKeyEvent(event);
    }

    @Override
    public void onMouseWheelEvent(MouseWheelEvent event) {
        event.consume();
    }

    public void bindMinimum(Binding<Float> binding) {
        this.minimum = binding;
    }

    /**
     * @return The minimum value possible.
     */
    public float getMinimum() {
        return minimum.get();
    }

    /**
     * @param min The new minimum value
     */
    public void setMinimum(float min) {
        this.minimum.set(min);
    }

    public void bindRange(Binding<Float> binding) {
        this.range = binding;
    }

    /**
     * @return The maxiumum value possible.
     */
    public float getRange() {
        return range.get();
    }

    /**
     * @param val The new maximum value.
     */
    public void setRange(float val) {
        range.set(val);
    }

    public void bindIncrement(Binding<Float> binding) {
        increment = binding;
    }

    /**
     * @return The smallest increment possible.
     */
    public float getIncrement() {
        return increment.get();
    }

    /**
     * @param val The new smallest increment to set to.
     */
    public void setIncrement(float val) {
        increment.set(val);
    }

    public void bindValue(Binding<Float> binding) {
        value = binding;
    }

    /**
     * @return The current value.
     */
    public float getValue() {
        return value.get();
    }

    /**
     * @param val The new current value.
     */
    public void setValue(float val) {
        value.set(val);
    }

    /**
     * @return The number of decimal points used.
     */
    public int getPrecision() {
        return precision;
    }

    /**
     * @param precision The number of decimal points.
     */
    public void setPrecision(int precision) {
        this.precision = precision;
    }

    private int pixelOffsetFor(float val, int width) {
        return TeraMath.floorToInt(width * (val - getMinimum()) / getRange());
    }


    public void setUiSliderOnChangeTriggeredListener(UISliderOnChangeTriggeredListener listener) {
        uiSliderOnChangeTriggeredListener = listener;
    }
}
