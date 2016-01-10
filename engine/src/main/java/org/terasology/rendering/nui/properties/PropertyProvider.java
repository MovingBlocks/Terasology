/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.properties;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.terasology.engine.SimpleUri;
import org.terasology.math.geom.Vector3f;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.ClassMetadata;
import org.terasology.reflection.metadata.DefaultClassMetadata;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.itemRendering.ItemRenderer;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UIDropdown;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UITextEntry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.getAllFields;

/**
 *
 * Provides properties of a given object using annotations.
 *
 * Range:
 *   * creates a slider with given min, max and precision maps to a float
 *
 * Checkbox:
 *   * creates a checkbox that maps to a boolean
 *
 * TextField:
 *   * creates a text box that maps to a string
 *
 * OneOf:
 *   * creates a combobox that maps to a list of strings, an enum or a custom defined item provider
 */
public class PropertyProvider {
    private static final Pattern VECTOR_3F = Pattern.compile("\\((\\d*\\.?\\d), (\\d*\\.?\\d), (\\d*\\.?\\d)\\)");

    private final Map<Class<?>, PropertyFactory<?>> factories = Maps.newHashMap();

    public PropertyProvider() {
        factories.put(Range.class, new RangePropertyFactory());
        factories.put(Checkbox.class, new CheckboxPropertyFactory());
        factories.put(OneOf.List.class, new OneOfListPropertyFactory());
        factories.put(OneOf.Enum.class, new OneOfEnumPropertyFactory());
        factories.put(OneOf.Provider.class, new OneOfProviderPropertyFactory());
        factories.put(TextField.class, new TextPropertyFactory());
    }

    public List<Property<?, ?>> createProperties(Object target) {
        List<Property<?, ?>> properties = Lists.newArrayList();
        try {
            Class<?> type = target.getClass();

            ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
            CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
            ClassMetadata<?, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, copyStrategies);
            for (Field field : getAllFields(type)) {
                Annotation annotation = getFactory(field);
                if (annotation != null) {
                    FieldMetadata<Object, ?> fieldMetadata = (FieldMetadata<Object, ?>) classMetadata.getField(field.getName());
                    PropertyFactory factory = factories.get(annotation.annotationType());
                    Property property = factory.create(target, fieldMetadata, field.getName(), annotation);
                    properties.add(property);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return properties;
    }

    private Annotation getFactory(Field field) {
        Annotation[] annotations = field.getAnnotations();
        for (Annotation annotation : annotations) {
            if (factories.containsKey(annotation.annotationType())) {
                return annotation;
            }
        }
        return null;
    }

    private <T> TextMapper<T> createTextMapping(Class<?> type) {
        TextMapper<?> textBinding = null;
        if (type == String.class) {
            textBinding = new StringTextBinding();
        } else if (type == Boolean.TYPE || type == Boolean.class) {
            textBinding = new BooleanTextBinding();
        } else if (type == Integer.TYPE || type == Integer.class) {
            textBinding = new IntegerTextBinding();
        } else if (type == Float.TYPE || type == Float.class) {
            textBinding = new FloatTextBinding();
        } else if (type == Vector3f.class) {
            textBinding = new Vector3fTextBinding();
        } else {
            throw new IllegalArgumentException("Cannot create Binding<String> for a field of type " + type);
        }
        return (TextMapper<T>) textBinding;
    }

    protected <T> Binding<T> createTextBinding(Object target, final FieldMetadata<Object, T> fieldMetadata) {
        return new TextBinding<>(target, fieldMetadata);
    }

    protected Binding<Float> createFloatBinding(Object target, final FieldMetadata<Object, ?> fieldMetadata) {
        Class<?> type = fieldMetadata.getType();
        if (type == Integer.class || type == Integer.TYPE) {
            return new Binding<Float>() {
                @Override
                public Float get() {
                    return ((Integer) fieldMetadata.getValueChecked(target)).floatValue();
                }

                @Override
                public void set(Float value) {
                    fieldMetadata.setValue(target, value.intValue());
                }
            };
        } else if (type == Float.class || type == Float.TYPE) {
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
        } else if (type == Double.class || type == double.class) {
            return new Binding<Float>() {
                @Override
                public Float get() {
                    return ((Double) fieldMetadata.getValueChecked(target)).floatValue();
                }

                @Override
                public void set(Float value) {
                    fieldMetadata.setValue(target, value.doubleValue());
                }
            };
        } else {
            throw new IllegalArgumentException("Cannot create Binding<Float> for a field of type " + type);
        }
    }

    private String fromLabelOrId(String label, String id) {
        if (Strings.isNullOrEmpty(label)) {
            char first = Character.toUpperCase(id.charAt(0));
            return first + id.substring(1);
        } else {
            return label;
        }
    }

    private interface PropertyFactory<T> {
        Property<?, ?> create(Object target, FieldMetadata<Object, ?> fieldMetadata, String id, T info);
    }

    private class RangePropertyFactory implements PropertyFactory<Range> {
        @Override
        public Property create(Object target, FieldMetadata<Object, ?> fieldMetadata, String id, Range range) {
            UISlider slider = new UISlider();
            slider.setMinimum(range.min());
            slider.setRange(range.max() - range.min());
            slider.setPrecision(range.precision());
            slider.setIncrement(range.increment());
            Binding<Float> binding = createFloatBinding(target, fieldMetadata);
            slider.bindValue(binding);
            String label = fromLabelOrId(range.label(), id);
            return new Property<>(label, binding, slider, range.description());
        }
    }

    private class CheckboxPropertyFactory implements PropertyFactory<Checkbox> {
        @Override
        public Property create(Object target, FieldMetadata<Object, ?> fieldMetadata, String id, Checkbox info) {
            UICheckbox checkbox = new UICheckbox();
            Binding<Boolean> binding = createTextBinding(target, (FieldMetadata<Object, Boolean>) fieldMetadata);
            checkbox.bindChecked(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, checkbox, info.description());
        }
    }

    private class OneOfListPropertyFactory implements PropertyFactory<OneOf.List> {
        @Override
        public Property create(Object target, FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.List info) {
            UIDropdown<String> dropdown = new UIDropdown<>();
            dropdown.bindOptions(new DefaultBinding<>(Arrays.asList(info.items())));
            Binding<String> binding = createTextBinding(target, (FieldMetadata<Object, String>) fieldMetadata);
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class OneOfEnumPropertyFactory implements PropertyFactory<OneOf.Enum> {
        @Override
        public Property create(Object target, final FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.Enum info) {
            Class<?> cls = fieldMetadata.getType();
            Object[] items = cls.getEnumConstants();
            UIDropdown dropdown = new UIDropdown();
            dropdown.bindOptions(new DefaultBinding(Arrays.asList(items)));
            Binding binding = createTextBinding(target, fieldMetadata);
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class OneOfProviderPropertyFactory implements PropertyFactory<OneOf.Provider> {
        @Override
        public Property create(Object target, final FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.Provider info) {
            UIDropdown dropdown = new UIDropdown();
            OneOfProviderFactory factory = CoreRegistry.get(OneOfProviderFactory.class);
            Binding<?> listBinding = factory.get(info.name());
            if (listBinding != null) {
                dropdown.bindOptions(listBinding);
            }
            ItemRenderer<?> itemRenderer = factory.getItemRenderer(info.name());
            if (itemRenderer != null) {
                dropdown.setOptionRenderer(itemRenderer);
            }
            Binding binding = createTextBinding(target, fieldMetadata);
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class TextPropertyFactory<T> implements PropertyFactory<TextField> {
        @Override
        public Property create(Object target, FieldMetadata<Object, ?> fieldMetadata, String id, TextField info) {
            UITextEntry<T> text = new UITextEntry<>();

            Binding<T> textBinding = createTextBinding(target, (FieldMetadata<Object, T>) fieldMetadata);
            TextMapper<T> textMapper = createTextMapping(fieldMetadata.getType());
            text.setFormatter(textMapper);
            text.setParser(textMapper);
            text.bindValue(textBinding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, textBinding, text, info.description());
        }
    }

    private interface TextMapper<T> extends UITextEntry.Formatter<T>, UITextEntry.Parser<T> {

    }

    private class TextBinding<T> implements Binding<T> {
        private FieldMetadata<Object, T> fieldMetadata;
        private final Object target;

        protected TextBinding(Object target, FieldMetadata<Object, T> fieldMetadata) {
            this.target = target;
            this.fieldMetadata = fieldMetadata;
        }

        @Override
        public T get() {
            return fieldMetadata.getValueChecked(target);
        }

        @Override
        public void set(T value) {
            fieldMetadata.setValue(target, value);
        }
    }

    private final class StringTextBinding implements TextMapper<String> {

        @Override
        public String toString(String value) {
            return value;
        }

        @Override
        public String parse(String value) {
            return value;
        }
    }

    private final class IntegerTextBinding implements TextMapper<Integer> {

        @Override
        public String toString(Integer value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }
    }

    private final class FloatTextBinding implements TextMapper<Float> {

        @Override
        public String toString(Float value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Float parse(String value) {
            return Float.parseFloat(value);
        }

    }

    private final class BooleanTextBinding implements TextMapper<Boolean> {

        @Override
        public String toString(Boolean value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    }

    private final class Vector3fTextBinding implements TextMapper<Vector3f> {

        @Override
        public String toString(Vector3f value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Vector3f parse(String value) {
            Matcher matcher = VECTOR_3F.matcher(value);
            if (matcher.matches()) {
                return new Vector3f(Float.parseFloat(matcher.group(1)), Float.parseFloat(matcher.group(2)), Float.parseFloat(matcher.group(3)));
            }
            throw new IllegalArgumentException("Cannot parse " + value + " to Vector3f");
        }
    }
}
