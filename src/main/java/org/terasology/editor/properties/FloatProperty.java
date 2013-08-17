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

import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.core.FloatTypeHandler;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

/**
 * @author Immortius
 */
public class FloatProperty implements Property<Float> {

    private static final DecimalFormat DEFAULT_DECIMAL_FORMAT = new DecimalFormat("0.0000000");

    private final String title;
    private final float min;
    private final float max;
    private final Object target;
    private final FieldMetadata accessor;


    public FloatProperty(Object target, Field field, String title, float min, float max) {
        this.target = target;
        this.min = min;
        this.max = max;
        this.title = title;
        this.accessor = new FieldMetadata(field, new FloatTypeHandler(), false);
    }

    public float getMinValue() {
        return min;
    }

    public float getMaxValue() {
        return max;
    }

    @Override
    public Float getValue() {
        return (Float) accessor.getValue(target);
    }

    @Override
    public void setValue(Float value) {
        accessor.setValue(target, value);
    }

    @Override
    public Class<Float> getValueType() {
        return Float.class;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return DEFAULT_DECIMAL_FORMAT.format(getValue());
    }
}
