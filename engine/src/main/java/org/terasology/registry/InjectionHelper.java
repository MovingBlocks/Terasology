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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

/**
 * @author Immortius
 */
public final class InjectionHelper {
    private static final Logger logger = LoggerFactory.getLogger(InjectionHelper.class);

    private InjectionHelper() {
    }

    public static void inject(final Object object) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(In.class))) {
                    Object value = CoreRegistry.get(field.getType());
                    if (value != null) {
                        try {
                            field.setAccessible(true);
                            field.set(object, value);
                        } catch (IllegalAccessException e) {
                            logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                        }
                    }
                }

                for (Field field : ReflectionUtils.getAllFields(object.getClass(), ReflectionUtils.withAnnotation(org.terasology.entitySystem.systems.In.class))) {
                    Object value = CoreRegistry.get(field.getType());
                    if (value != null) {
                        try {
                            field.setAccessible(true);
                            field.set(object, value);
                            logger.warn("Injection into field {} of {} using old @In annotation - please update", field, object);
                        } catch (IllegalAccessException e) {
                            logger.error("Failed to inject value {} into field {} of {}", value, field, object, e);
                        }
                    }
                }
                return null;
            }
        });
    }

    public static <T> void inject(final Object object, final Class<? extends Annotation> annotation, final Map<Class<? extends T>, T> source) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
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
            }
        });
    }

    public static void share(Object object) {
        Share share = object.getClass().getAnnotation(Share.class);
        if (share != null && share.value() != null) {
            for (Class interfaceType : share.value()) {
                CoreRegistry.put(interfaceType, object);
            }
        }

        org.terasology.entitySystem.systems.Share oldshare = object.getClass().getAnnotation(org.terasology.entitySystem.systems.Share.class);
        if (oldshare != null && oldshare.value() != null) {
            for (Class interfaceType : oldshare.value()) {
                CoreRegistry.put(interfaceType, object);
            }
        }
    }

    public static void unshare(Object object) {
        Share share = object.getClass().getAnnotation(Share.class);
        if (share != null && share.value() != null) {
            for (Class interfaceType : share.value()) {
                CoreRegistry.remove(interfaceType);
            }
        }
    }
}
