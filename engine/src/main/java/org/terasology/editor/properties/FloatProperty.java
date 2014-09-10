/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.editor.properties;

import org.terasology.reflection.metadata.FieldMetadata;

import java.text.DecimalFormat;

/**
 * @author Immortius
 */
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
