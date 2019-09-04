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
package org.terasology.rendering.nui.widgets.types.builtin.util;

import com.google.common.collect.Maps;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.reflection.TypeInfo;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.terasology.rendering.nui.widgets.types.TypeWidgetFactory.LABEL_WIDGET_ID;

public class FieldsWidgetBuilder<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldsWidgetBuilder.class);

    private TypeInfo<T> type;
    private TypeWidgetLibrary library;

    private Map<Field, TypeInfo<?>> fields = Maps.newLinkedHashMap();

    public FieldsWidgetBuilder(TypeInfo<T> type, TypeWidgetLibrary library) {
        this(type, library, field -> true);
    }

    public FieldsWidgetBuilder(TypeInfo<T> type, TypeWidgetLibrary library,
                               Predicate<Field> fieldPredicate) {
        this.type = type;
        this.library = library;

        for (Field field : ReflectionUtils.getAllFields(type.getRawType())) {
            if (Modifier.isTransient(field.getModifiers()) || Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (!fieldPredicate.test(field)) {
                continue;
            }

            Type resolvedFieldType = ReflectionUtil.resolveType(type.getType(), field.getGenericType());

            fields.put(field, TypeInfo.of(resolvedFieldType));
        }
    }

    public static <F> Optional<Binding<F>> getFieldBinding(Binding<?> binding, Field field) {
        if (Modifier.isPublic(field.getModifiers())) {
            return Optional.of(
                getAccessibleFieldBinding(binding, field)
            );
        }

        return getPropertyBinding(binding, field);
    }

    private static <F> Optional<Binding<F>> getPropertyBinding(Binding<?> binding, Field field) {
        Method setter = ReflectionUtil.findSetter(field);

        if (setter == null) {
            return Optional.empty();
        }

        Method getter = ReflectionUtil.findGetter(field);

        if (getter == null) {
            LOGGER.error("Cannot bind field {} with setter {} but no getter", field, setter);
            return Optional.empty();
        }

        Binding<F> fieldBinding = new Binding<F>() {
            @Override
            public F get() {
                try {
                    return (F) getter.invoke(binding.get());
                } catch (IllegalAccessException e) {
                    // Method is always accessible
                    throw new RuntimeException("Unreachable", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            }

            @Override
            public void set(F value) {
                try {
                    setter.invoke(binding.get(), value);
                } catch (IllegalAccessException e) {
                    // Method is always accessible
                    throw new RuntimeException("Unreachable", e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        };

        return Optional.of(binding.makeChildBinding(fieldBinding));
    }

    private static <F> Binding<F> getAccessibleFieldBinding(Binding<?> binding, Field field) {
        // For final fields
        field.setAccessible(true);

        Binding<F> fieldBinding = new Binding<F>() {
            @Override
            public F get() {
                try {
                    return (F) field.get(binding.get());
                } catch (IllegalAccessException e) {
                    // Field is public, should never be an illegal access
                    throw new RuntimeException("Unreachable", e);
                }
            }

            @Override
            public void set(F value) {
                try {
                    field.set(binding.get(), value);
                } catch (IllegalAccessException e) {
                    // Field is public, should never be an illegal access
                    throw new RuntimeException("Unreachable", e);
                }
            }
        };

        return binding.makeChildBinding(fieldBinding);
    }

    public List<UIWidget> getFieldWidgets(Binding<T> binding) {
        return fields.entrySet().stream()
                   .map(entry -> {
                       Field field = entry.getKey();
                       Optional<UIWidget> optionalFieldWidget = getFieldWidget(binding, field, entry.getValue());

                       if (!optionalFieldWidget.isPresent()) {
                           return null;
                       }

                       UIWidget fieldWidget = optionalFieldWidget.get();
                       String fieldLabel = field.getName();

                       return WidgetUtil.labelize(fieldWidget, fieldLabel, LABEL_WIDGET_ID);
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toList());
    }

    private <F> Optional<UIWidget> getFieldWidget(Binding<T> binding, Field field, TypeInfo<F> fieldType) {
        Optional<Binding<F>> fieldBinding = getFieldBinding(binding, field);

        if (!fieldBinding.isPresent()) {
            return Optional.empty();
        }

        Optional<UIWidget> widget = library.getBaseTypeWidget(fieldBinding.get(), fieldType);

        if (!widget.isPresent()) {
            LOGGER.warn("Could not create a UIWidget for field {}", field);
            return Optional.empty();
        }

        return widget;
    }
}
