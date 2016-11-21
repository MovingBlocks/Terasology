/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.registry;

import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Context;
import org.terasology.module.sandbox.ModuleClassLoader;
import org.terasology.naming.Name;
import org.terasology.util.reflection.ParameterProvider;
import org.terasology.util.reflection.SimpleClassFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

/**
 */
public final class InjectionHelper {
    private static final Logger logger = LoggerFactory.getLogger(InjectionHelper.class);

    private InjectionHelper() {
    }

    public static void inject(final Object object, Context context) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
                Object value = context.get(field.getType());

                if (value instanceof DynamicInstanceProvider) {
                    // Really had to go out of my way for this. It works.
                    if (Arrays.stream(value.getClass().getGenericInterfaces())
                            .filter(x -> x instanceof ParameterizedType)
                            .map(x -> (ParameterizedType) x)
                            .filter(x -> x.getRawType() == DynamicInstanceProvider.class)
                            .filter(x -> Arrays.equals(x.getActualTypeArguments(), new java.lang.reflect.Type[]{field.getType()}))
                            .findAny()
                            .isPresent()) {
                        if (object.getClass().getClassLoader() instanceof ModuleClassLoader) {
                            Name moduleId = ((ModuleClassLoader) object.getClass().getClassLoader()).getModuleId();
                            value = ((DynamicInstanceProvider) value).getInstanceForModule(moduleId);
                        }
                    }
                }

                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                    }
                }
            }

            return null;
        });
    }

    public static void inject(final Object object) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
                Object value = CoreRegistry.get(field.getType());
                if (value instanceof DynamicInstanceProvider) {
                    if (Arrays.stream(value.getClass().getGenericInterfaces())
                            .filter(x -> x instanceof ParameterizedType)
                            .map(x -> (ParameterizedType) x)
                            .filter(x -> x.getRawType() == DynamicInstanceProvider.class)
                            .filter(x -> Arrays.equals(x.getActualTypeArguments(), new java.lang.reflect.Type[]{field.getType()}))
                            .findAny()
                            .isPresent()) {
                        if (object.getClass().getClassLoader() instanceof ModuleClassLoader) {
                            Name moduleId = ((ModuleClassLoader) object.getClass().getClassLoader()).getModuleId();
                            value = ((DynamicInstanceProvider) value).getInstanceForModule(moduleId);
                        }
                    }
                }
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                    }
                }
            }

            return null;
        });
    }

    public static <T> void inject(final Object object, final Class<? extends Annotation> annotation, final Map<Class<? extends T>, T> source) {
        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(annotation))) {
                Object value = source.get(field.getType());
                if (value != null) {
                    try {
                        field.setAccessible(true);
                        field.set(object, value);
                    } catch (IllegalAccessException e) {
                        logger.error("Failed to inject value {} into field {}", value, field, e);
                    }
                } else {
                    logger.error("Failed to inject into field {}, nothing to inject", field);
                }
            }

            return null;
        });
    }

    public static void share(Object object) {
        Share share = object.getClass().getAnnotation(Share.class);
        if (share != null && share.value() != null) {
            for (Class interfaceType : share.value()) {
                CoreRegistry.put(interfaceType, object);
            }
        }
    }


    public static <E> E createWithConstructorInjection(Class<? extends E> clazz, Context context) {
        SimpleClassFactory simpleClassFactory = new SimpleClassFactory(new ParameterProvider() {
            @Override
            public <T> Optional<T> get(Class<T> x) {
                return Optional.of(context.get(x));
            }
        });
        return simpleClassFactory.instantiateClass(clazz).get();
    }
}
