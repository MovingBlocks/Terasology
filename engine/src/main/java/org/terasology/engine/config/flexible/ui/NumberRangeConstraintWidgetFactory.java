// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.config.flexible.ui;

import org.terasology.engine.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.nui.UIWidget;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.widgets.UISlider;

/**
 * Creates {@link UISlider} for  {@link Number} types with constraint {@link NumberRangeConstraint}
 * @param <T> value type of {@link org.terasology.engine.config.flexible.Setting}
 */
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
                return getSetting().get().floatValue();
            }

            @Override
            public void set(Float value) {
                setSettingValue(value);
            }
        });
    }

    private void updateSliderIfInteger(UISlider slider) {
        T settingValue = getSetting().get();

        if (settingValue instanceof Float || settingValue instanceof Double) {
            return;
        }

        slider.setIncrement(1.0f);
        slider.setPrecision(0);
    }

    private void setSettingValue(float value) {
        getSetting().set(getFloatAsT(value));
    }

    private T getFloatAsT(float value) {
        T settingValue = getSetting().get();

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
