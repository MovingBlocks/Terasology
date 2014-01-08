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
import org.terasology.rendering.nui.baseWidgets.TextEventListener;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIDropdown;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.baseWidgets.UIText;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;

import javax.vecmath.Vector3f;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.or;
import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.withAnnotation;
import static org.reflections.ReflectionUtils.withType;

/**
 * @author synopia
 */
public class PropertyProvider<T> {
    private final static Pattern VECTOR_3F= Pattern.compile("\\((\\d*\\.?\\d), (\\d*\\.?\\d), (\\d*\\.?\\d)\\)");
    private T target;
    private List<Property<?,?>> properties = Lists.newArrayList();
    private NumberFormat floatFormat;

    public PropertyProvider(T target) {
        floatFormat = NumberFormat.getNumberInstance();

        try {
            this.target = target;
            Class<?> type = target.getClass();
            ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
            CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
            ClassMetadata<?, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, copyStrategies);
            for (Field field : getAllFields(type, and(withAnnotation(Range.class), or(withType(Float.TYPE), withType(Float.class), withType(Integer.class), withType(Integer.TYPE))))) {
                FieldMetadata<Object, ?> fieldMetadata = (FieldMetadata<Object, ?>) classMetadata.getField(field.getName());
                Range range = field.getAnnotation(Range.class);

                Property<Float, UISlider> property = createRangeProperty(fieldMetadata, field.getName(), range);
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
            for (Field field : getAllFields(type, and(withAnnotation(TextField.class)))) {
                FieldMetadata<Object, ?> fieldMetadata = (FieldMetadata<Object, ?>) classMetadata.getField(field.getName());
                TextField textField = field.getAnnotation(TextField.class);

                Property<String, UIText> property = createTextProperty(fieldMetadata, field.getName());
                properties.add(property);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Property<String, UIText> createTextProperty(final FieldMetadata<Object, ?> fieldMetadata, String label) {
        UIText text = new UIText();
        StringBinding binding = createStringBinding(fieldMetadata);
        text.bindText(binding);
        text.subscribe(binding);
        return new Property<>(label, binding, text);
    }

    private Property<Boolean, UICheckbox> createCheckboxProperty(final FieldMetadata<Object, Boolean> fieldMetadata, String label) {
        UICheckbox checkbox = new UICheckbox();
        Binding<Boolean> binding = createBooleanBinding(fieldMetadata);
        checkbox.bindChecked(binding);
        return new Property<>(label, binding, checkbox);
    }

    private Property<String, UIDropdown<String>> createStringDropdownProperty(final FieldMetadata<Object, String> fieldMetadata, String label, String[] items) {
        UIDropdown<String> dropdown = new UIDropdown<>();
        dropdown.bindOptions(new DefaultBinding<>(Arrays.asList(items)));
        Binding<String> binding = createStringBinding(fieldMetadata);
        dropdown.bindSelection(binding);
        return new Property<>(label, binding, dropdown);
    }

    public List<Property<?, ?>> getProperties() {
        return properties;
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    private Property<Float, UISlider> createRangeProperty(final FieldMetadata<Object, ?> fieldMetadata, String label, Range range) {
        UISlider slider = new UISlider();
        slider.setMinimum(range.min());
        slider.setRange(range.max() - range.min());
        slider.setPrecision(range.precision());
        slider.setIncrement(range.increment());
        Binding<Float> binding = createFloatBinding(fieldMetadata);
        slider.bindValue(binding);
        return new Property<>(label, binding, slider);
    }

    private StringBinding createStringBinding(final FieldMetadata<Object, ?> fieldMetadata) {
        Class<?> type = fieldMetadata.getType();
        Binding<String> converter;
        if( type==String.class ) {
            converter = new Binding<String>() {
                @Override
                public String get() {
                    return (String) fieldMetadata.getValueChecked(target);
                }

                @Override
                public void set(String value) {
                    fieldMetadata.setValue(target, value);
                }
            };
        } else if( type==Integer.TYPE || type==Integer.class) {
            converter = new Binding<String>() {
                @Override
                public String get() {
                    Integer result = (Integer) fieldMetadata.getValueChecked(target);
                    return result!=null ? result.toString() : "";
                }

                @Override
                public void set(String value) {
                    int result = Integer.parseInt(value);
                    fieldMetadata.setValue(target, result);
                }
            };
        } else if( type==Float.TYPE || type==Float.class) {
            converter = new Binding<String>() {
                @Override
                public String get() {
                    Float result = (Float) fieldMetadata.getValueChecked(target);
                    return result!=null ? result.toString() : "";
                }

                @Override
                public void set(String value) {
                    float result = Float.parseFloat(value);
                    fieldMetadata.setValue(target, result);
                }
            };
        } else if (type== Vector3f.class ) {
            converter = new Binding<String>() {
                @Override
                public String get() {
                    Vector3f vector = (Vector3f) fieldMetadata.getValueChecked(target);
                    return vector!=null ? vector.toString() : "";
                }

                @Override
                public void set(String value) {
                    Matcher matcher = VECTOR_3F.matcher(value);
                    if(matcher.matches()) {
                        Vector3f result = new Vector3f(Float.parseFloat(matcher.group(1)), Float.parseFloat(matcher.group(2)), Float.parseFloat(matcher.group(3)) );
                        fieldMetadata.setValue(target, result);
                    } else {
                        throw new IllegalArgumentException("Cannot parse "+value+ " to Vector3f");
                    }
                }
            };
        } else {
            throw new IllegalArgumentException("Cannot create Binding<String> for a field of type "+type);
        }
        return new StringBinding(converter);
    }

    private Binding<Boolean> createBooleanBinding(final FieldMetadata<Object, Boolean> fieldMetadata) {
        return new Binding<Boolean>() {
            @Override
            public Boolean get() {
                return fieldMetadata.getValueChecked(target);
            }

            @Override
            public void set(Boolean value) {
                fieldMetadata.setValue(target, value);
            }
        };
    }

    private Binding<Float> createFloatBinding(final FieldMetadata<Object, ?> fieldMetadata) {
        Class<?> type = fieldMetadata.getType();
        if( type==Integer.class || type==Integer.TYPE ) {
            return new Binding<Float>() {
                @Override
                public Float get() {
                    return (Float) fieldMetadata.getValueChecked(target);
                }

                @Override
                public void set(Float value) {
                    fieldMetadata.setValue(target, value.intValue());
                }
            };
        } else if( type==Float.class || type==Float.TYPE ) {
            return new Binding<Float>() {
                @Override
                public Float get() {
                    return (Float) fieldMetadata.getValueChecked(target);
                }

                @Override
                public void set(Float value) {
                    fieldMetadata.setValue(target, value);
                }
            };
        } else {
            throw new IllegalArgumentException("Cannot create Binding<Float> for a field of type "+type);
        }
    }

    private static class StringBinding implements Binding<String>, TextEventListener {
        private String tempValue;
        private Binding<String> binding;

        private StringBinding(Binding<String> binding) {
            this.binding = binding;
        }

        @Override
        public String get() {
            if( tempValue==null ) {
                return binding.get();
            }
            return tempValue;
        }

        @Override
        public void set(String value) {
            tempValue = value;
        }

        @Override
        public void onEnterPressed(UIText text) {
            try {
                if( tempValue!=null ) {
                    binding.set(tempValue);
                    tempValue = null;
                }
            } catch (IllegalArgumentException e) {
                // ignore
            }
        }
    }
}
