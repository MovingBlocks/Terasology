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
package org.terasology.rendering.nui.properties;

import com.google.common.collect.Lists;
import org.terasology.classMetadata.ClassMetadata;
import org.terasology.classMetadata.DefaultClassMetadata;
import org.terasology.classMetadata.FieldMetadata;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.baseWidgets.UIText;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withType;

/**
 * @author synopia
 */
public class PropertyProvider<T> {
    private T target;
    private List<Property<?,?>> properties = Lists.newArrayList();

    public PropertyProvider(T target) {
        try {
            this.target = target;
            Class<?> type = target.getClass();
            ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
            CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
            ClassMetadata<?, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, copyStrategies);
            for (Field field : getAllFields(type, and(withAnnotation(Range.class), or(withType(Float.TYPE), withType(Float.class))))) {
                FieldMetadata<Object, Float> fieldMetadata = (FieldMetadata<Object, Float>) classMetadata.getField(field.getName());
                Range range = field.getAnnotation(Range.class);

                Property<Float, UISlider> property = createFloatRangeProperty(fieldMetadata, field.getName(), range);
                properties.add(property);
            }
            for (Field field : getAllFields(type, and(withAnnotation(Range.class), or(withType(Integer.TYPE), withType(Integer.class))))) {
                FieldMetadata<Object, Integer> fieldMetadata = (FieldMetadata<Object, Integer>) classMetadata.getField(field.getName());
                Range range = field.getAnnotation(Range.class);

                Property<Float, UISlider> property = createIntRangeProperty(fieldMetadata, field.getName(), range);
                properties.add(property);
            }
            for (Field field : getAllFields(type, and(withAnnotation(Checkbox.class), or(withType(Boolean.TYPE), withType(Boolean.class))))) {
                FieldMetadata<Object, Boolean> fieldMetadata = (FieldMetadata<Object, Boolean>) classMetadata.getField(field.getName());
                Checkbox checkbox = field.getAnnotation(Checkbox.class);

                Property<Boolean, UICheckbox> property = createCheckboxProperty(fieldMetadata, field.getName());
                properties.add(property);
            }
            for (Field field : getAllFields(type, and(withAnnotation(OneOf.List.class), withType(String.class)))) {
                FieldMetadata<Object, String> fieldMetadata = (FieldMetadata<Object, String>) classMetadata.getField(field.getName());
                OneOf.List list = field.getAnnotation(OneOf.List.class);

                Property<String, UIDropdown<String>> property = createStringDropdownProperty(fieldMetadata, field.getName(), list.items());
                properties.add(property);
            }
            for (Field field : getAllFields(type, and(withAnnotation(TextField.class), withType(String.class)))) {
                FieldMetadata<Object, String> fieldMetadata = (FieldMetadata<Object, String>) classMetadata.getField(field.getName());
                TextField textField = field.getAnnotation(TextField.class);

                Property<String, UIText> property = createStringTextProperty(fieldMetadata, field.getName());
                properties.add(property);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Property<String, UIText> createStringTextProperty(final FieldMetadata<Object, String> fieldMetadata, String label) {
        UIText text = new UIText();
        Binding<String> binding = new Binding<String>() {
            @Override
            public String get() {
                return fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(String value) {
                fieldMetadata.setValue(target, value);
            }
        };
        text.bindText(binding);
        return new Property<>(label, binding, text);
    }

    private Property<Boolean, UICheckbox> createCheckboxProperty(final FieldMetadata<Object, Boolean> fieldMetadata, String label) {
        UICheckbox checkbox = new UICheckbox();
        Binding<Boolean> binding = new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(Boolean value) {
                fieldMetadata.setValue(target, value);
            }
        };
        checkbox.bindChecked(binding);
        return new Property<>(label, binding, checkbox);
    }

    private Property<String, UIDropdown<String>> createStringDropdownProperty(final FieldMetadata<Object, String> fieldMetadata, String label, String[] items) {
        UIDropdown<String> dropdown = new UIDropdown<>();
        dropdown.bindOptions(new DefaultBinding<>(Arrays.asList(items)));
        Binding<String> binding = new Binding<String>() {
            @Override
            public String get() {
                return fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(String value) {
                fieldMetadata.setValue(target, value);
            }
        };
        dropdown.bindSelection(binding);
        return new Property<>(label, binding, dropdown);
    }

    public List<Property<?, ?>> getProperties() {
        return properties;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    private Property<Float, UISlider> createFloatRangeProperty(final FieldMetadata<Object, Float> fieldMetadata, String label, Range range) {
        UISlider slider = new UISlider();
        slider.setMinimum(range.min());
        slider.setRange(range.max()-range.min());
        Binding<Float> binding = new Binding<Float>() {
            @Override
            public Float get() {
                return fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(Float value) {
                fieldMetadata.setValue(target, value);
            }
        };
        slider.bindValue(binding);
        return new Property<>(label, binding, slider);
    }
    private Property<Float, UISlider> createIntRangeProperty(final FieldMetadata<Object, Integer> fieldMetadata, String label, Range range) {
        UISlider slider = new UISlider();
        slider.setMinimum(range.min());
        slider.setRange(range.max() - range.min());
        slider.setPrecision(0);
        slider.setIncrement(1);
        Binding<Float> binding = new Binding<Float>() {
            @Override
            public Float get() {
                return (float) fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(Float value) {
                fieldMetadata.setValue(target, value.intValue());
            }
        };
        slider.bindValue(binding);
        return new Property<>(label, binding, slider);
    }
}
