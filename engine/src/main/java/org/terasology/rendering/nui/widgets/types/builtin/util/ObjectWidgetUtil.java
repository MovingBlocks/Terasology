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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class ObjectWidgetUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectWidgetUtil.class);

    public static <F> Optional<Binding<F>> getFieldBinding(Binding<?> objectBinding, Field field) {
        if (Modifier.isPublic(field.getModifiers())) {
            return Optional.of(
                getAccessibleFieldBinding(objectBinding, field)
            );
        }

        return getPropertyBinding(objectBinding, field);
    }

    private static <F> Optional<Binding<F>> getPropertyBinding(Binding<?> objectBinding, Field field) {
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
                    return (F) getter.invoke(objectBinding.get());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unreachable");
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            }

            @Override
            public void set(F value) {
                try {
                    setter.invoke(objectBinding.get(), value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Unreachable");
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e.getCause());
                }
            }
        };

        return Optional.of(objectBinding.makeChildBinding(fieldBinding));
    }

    private static <F> Binding<F> getAccessibleFieldBinding(Binding<?> objectBinding, Field field) {
        // For final fields
        field.setAccessible(true);

        Binding<F> fieldBinding = new Binding<F>() {
            @Override
            public F get() {
                try {
                    return (F) field.get(objectBinding.get());
                } catch (IllegalAccessException e) {
                    // Field is public
                    throw new RuntimeException("Unreachable");
                }
            }

            @Override
            public void set(F value) {
                try {
                    field.set(objectBinding.get(), value);
                } catch (IllegalAccessException e) {
                    // Field is public
                    throw new RuntimeException("Unreachable");
                }
            }
        };

        return objectBinding.makeChildBinding(fieldBinding);
    }
}
