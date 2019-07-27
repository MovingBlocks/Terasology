/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible.ui;

import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.UISlider;

public class NumberRangeConstraintWidgetFactory<T extends Number & Comparable<? super T>>
    extends AssetBackedConstraintWidgetFactory<T, NumberRangeConstraint<T>> {

    public NumberRangeConstraintWidgetFactory() {
        super("engine:numberRangeSettingWidget");
    }

    @Override
    protected void bindWidgetToSetting(UIWidget widget) {
        UISlider slider = widget.find("slider", UISlider.class);
        assert slider != null;

        updateSliderIfInteger(slider);

        setSliderRange(slider);

        slider.bindValue(new Binding<Float>() {
            @Override
            public Float get() {
                return getSetting().getValue().floatValue();
            }

            @Override
            public void set(Float value) {
                setSettingValue(value);
            }
        });
    }

    private void updateSliderIfInteger(UISlider slider) {
        T settingValue = getSetting().getValue();

        if (settingValue instanceof Float || settingValue instanceof Double) {
            return;
        }

        slider.setIncrement(1.0f);
        slider.setPrecision(0);
    }

    private void setSettingValue(float value) {
        getSetting().setValue(getFloatAsT(value));
    }

    private T getFloatAsT(float value) {
        T settingValue = getSetting().getValue();

        if (settingValue instanceof Byte) {
            return castToT((byte) Math.round(value));
        }

        if (settingValue instanceof Short) {
            return castToT((short) Math.round(value));
        }

        if (settingValue instanceof Integer) {
            return castToT(Math.round(value));
        }

        if (settingValue instanceof Long) {
            return castToT(Math.round((double) value));
        }

        if (settingValue instanceof Double) {
            return castToT((double) value);
        }

        return castToT(value);
    }

    private void setSliderRange(UISlider slider) {
        NumberRangeConstraint<?> constraint = getConstraint();

        float min = constraint.getMin().floatValue();

        if (!constraint.isMinInclusive()) {
            min += slider.getIncrement();
        }

        slider.setMinimum(min);

        float range = constraint.getMax().floatValue() - min;

        if (!constraint.isMaxInclusive()) {
            range -= slider.getIncrement();
        }

        slider.setRange(range);
    }
}
