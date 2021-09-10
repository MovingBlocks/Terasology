// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.editor.properties;

import org.terasology.reflection.metadata.FieldMetadata;

import java.text.DecimalFormat;

public class FloatProperty<T> implements Property<Float> {

    private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("0.0000000");

    private final float min;
    private final float max;
    private final T target;
    private final FieldMetadata<T, Float> field;


    public FloatProperty(T target, FieldMetadata<T, Float> field, float min, float max) {
        this.target = target;
        this.min = min;
        this.max = max;
        this.field = field;
    }

    public float getMinValue() {
        return min;
    }

    public float getMaxValue() {
        return max;
    }

    @Override
    public Float getValue() {
        return field.getValueChecked(target);
    }

    @Override
    public void setValue(Float value) {
        field.setValue(target, value);
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

    @Override
    public String getTitle() {
        return field.getName();
    }

    @Override
    public String toString() {
        return DEFAULT_DECIMAL_FORMAT.format(getValue());
    }
}
