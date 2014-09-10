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

import javax.vecmath.Vector3f;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.getAllFields;

/**
 * @author synopia
 */
public class PropertyProvider<T> {
    private static final Pattern VECTOR_3F = Pattern.compile("\\((\\d*\\.?\\d), (\\d*\\.?\\d), (\\d*\\.?\\d)\\)");
    private T target;
    private List<Property<?, ?>> properties = Lists.newArrayList();

    private Map<Class, PropertyFactory> factories = Maps.newHashMap();

    public PropertyProvider(T target) {
        factories.put(Range.class, new RangePropertyFactory());
        factories.put(Checkbox.class, new CheckboxPropertyFactory());
        factories.put(OneOf.List.class, new OneOfListPropertyFactory());
        factories.put(OneOf.Enum.class, new OneOfEnumPropertyFactory());
        factories.put(OneOf.Provider.class, new OneOfProviderPropertyFactory());
        factories.put(TextField.class, new TextPropertyFactory());

        try {
            this.target = target;
            Class<?> type = target.getClass();
            ReflectFactory reflectFactory = CoreRegistry.get(ReflectFactory.class);
            CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
            ClassMetadata<?, ?> classMetadata = new DefaultClassMetadata<>(new SimpleUri(), type, reflectFactory, copyStrategies);
            for (Field field : getAllFields(type)) {
                Annotation annotation = getFactory(field);
                if (annotation != null) {
                    FieldMetadata<Object, ?> fieldMetadata = (FieldMetadata<Object, ?>) classMetadata.getField(field.getName());
                    PropertyFactory factory = factories.get(annotation.annotationType());
                    Property property = factory.create(fieldMetadata, field.getName(), annotation);
                    properties.add(property);
                }
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
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

    public List<Property<?, ?>> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    private <T> TextBinding<T> createTextBinding(final FieldMetadata<Object, T> fieldMetadata) {
        Class<?> type = fieldMetadata.getType();
        TextBinding<?> textBinding;
        if (type == String.class) {
            textBinding = new StringTextBinding((FieldMetadata<Object, String>) fieldMetadata);
        } else if (type == Integer.TYPE || type == Integer.class) {
            textBinding = new IntegerTextBinding((FieldMetadata<Object, Integer>) fieldMetadata);
        } else if (type == Float.TYPE || type == Float.class) {
            textBinding = new FloatTextBinding((FieldMetadata<Object, Float>) fieldMetadata);
        } else if (type == Vector3f.class) {
            textBinding = new Vector3fTextBinding((FieldMetadata<Object, Vector3f>) fieldMetadata);
        } else {
            throw new IllegalArgumentException("Cannot create Binding<String> for a field of type " + type);
        }
        return (TextBinding<T>) textBinding;
    }


    private Binding<Float> createFloatBinding(final FieldMetadata<Object, ?> fieldMetadata) {
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
        Property create(FieldMetadata<Object, ?> fieldMetadata, String id, T info);
    }

    private class RangePropertyFactory implements PropertyFactory<Range> {
        @Override
        public Property create(FieldMetadata<Object, ?> fieldMetadata, String id, Range range) {
            UISlider slider = new UISlider();
            slider.setMinimum(range.min());
            slider.setRange(range.max() - range.min());
            slider.setPrecision(range.precision());
            slider.setIncrement(range.increment());
            Binding<Float> binding = createFloatBinding(fieldMetadata);
            slider.bindValue(binding);
            String label = fromLabelOrId(range.label(), id);
            return new Property<>(label, binding, slider, range.description());
        }
    }

    private class CheckboxPropertyFactory implements PropertyFactory<Checkbox> {
        @Override
        public Property create(FieldMetadata<Object, ?> fieldMetadata, String id, Checkbox info) {
            UICheckbox checkbox = new UICheckbox();
            Binding<Boolean> binding = new BooleanTextBinding((FieldMetadata<Object, Boolean>) fieldMetadata);
            checkbox.bindChecked(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, checkbox, info.description());
        }
    }

    private class OneOfListPropertyFactory implements PropertyFactory<OneOf.List> {
        @Override
        public Property create(FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.List info) {
            UIDropdown<String> dropdown = new UIDropdown<>();
            dropdown.bindOptions(new DefaultBinding<>(Arrays.asList(info.items())));
            Binding<String> binding = createTextBinding((FieldMetadata<Object, String>) fieldMetadata);
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class OneOfEnumPropertyFactory implements PropertyFactory<OneOf.Enum> {
        @Override
        public Property create(final FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.Enum info) {
            Class cls = fieldMetadata.getType();
            Object[] items = cls.getEnumConstants();
            UIDropdown dropdown = new UIDropdown();
            dropdown.bindOptions(new DefaultBinding(Arrays.asList(items)));
            Binding binding = new Binding() {
                @Override
                public Object get() {
                    return fieldMetadata.getValueChecked(target);
                }

                @Override
                public void set(Object value) {
                    fieldMetadata.setValue(target, value);
                }
            };
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class OneOfProviderPropertyFactory implements PropertyFactory<OneOf.Provider> {
        @Override
        public Property create(final FieldMetadata<Object, ?> fieldMetadata, String id, OneOf.Provider info) {
            UIDropdown dropdown = new UIDropdown();
            OneOfProviderFactory factory = CoreRegistry.get(OneOfProviderFactory.class);
            dropdown.bindOptions(factory.get(info.name()));
            ItemRenderer<?> itemRenderer = factory.getItemRenderer(info.name());
            if (itemRenderer != null) {
                dropdown.setOptionRenderer(itemRenderer);
            }
            Binding binding = new Binding() {
                @Override
                public Object get() {
                    return fieldMetadata.getValueChecked(target);
                }

                @Override
                public void set(Object value) {
                    fieldMetadata.setValue(target, value);
                }
            };
            dropdown.bindSelection(binding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, binding, dropdown, info.description());
        }
    }

    private class TextPropertyFactory implements PropertyFactory<TextField> {
        @Override
        public Property create(FieldMetadata<Object, ?> fieldMetadata, String id, TextField info) {
            UITextEntry<T> text = new UITextEntry<>();

            TextBinding<T> textBinding = createTextBinding((FieldMetadata<Object, T>) fieldMetadata);
            text.setFormatter(textBinding);
            text.setParser(textBinding);
            text.bindValue(textBinding);
            String label = fromLabelOrId(info.label(), id);
            return new Property<>(label, textBinding, text, info.description());
        }
    }

    private abstract class TextBinding<T> implements UITextEntry.Formatter<T>, UITextEntry.Parser<T>, Binding<T> {
        private FieldMetadata<Object, T> fieldMetadata;

        protected TextBinding(FieldMetadata<Object, T> fieldMetadata) {
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

    private final class StringTextBinding extends TextBinding<String> {
        private StringTextBinding(FieldMetadata<Object, String> fieldMetadata) {
            super(fieldMetadata);
        }

        @Override
        public String toString(String value) {
            return value;
        }

        @Override
        public String parse(String value) {
            return value;
        }
    }

    private final class IntegerTextBinding extends TextBinding<Integer> {
        private IntegerTextBinding(FieldMetadata<Object, Integer> fieldMetadata) {
            super(fieldMetadata);
        }

        @Override
        public String toString(Integer value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Integer parse(String value) {
            return Integer.parseInt(value);
        }
    }

    private final class FloatTextBinding extends TextBinding<Float> {

        private FloatTextBinding(FieldMetadata<Object, Float> fieldMetadata) {
            super(fieldMetadata);
        }

        @Override
        public String toString(Float value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Float parse(String value) {
            return Float.parseFloat(value);
        }

    }

    private final class BooleanTextBinding extends TextBinding<Boolean> {
        private BooleanTextBinding(FieldMetadata<Object, Boolean> fieldMetadata) {
            super(fieldMetadata);
        }

        @Override
        public String toString(Boolean value) {
            return value != null ? value.toString() : "";
        }

        @Override
        public Boolean parse(String value) {
            return Boolean.parseBoolean(value);
        }
    }

    private final class Vector3fTextBinding extends TextBinding<Vector3f> {

        private Vector3fTextBinding(FieldMetadata<Object, Vector3f> fieldMetadata) {
            super(fieldMetadata);
        }

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
