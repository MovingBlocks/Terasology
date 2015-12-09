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
package org.terasology.rendering.nui.databinding;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 */
public final class BindHelper {

    private static final Logger logger = LoggerFactory.getLogger(BindHelper.class);

    private BindHelper() {
    }

    public static <T> Binding<T> bindBeanProperty(String property, Object source, Class<T> propertyType) {
        Method getter = ReflectionUtil.findGetter(property, source.getClass(), propertyType);
        Method setter = ReflectionUtil.findSetter(property, source.getClass(), propertyType);
        if (getter == null || setter == null) {
            logger.warn("Failed to resolve property {} of type {} - is the getter or setter missing?", property, source.getClass());
            return new DefaultBinding<>();
        }
        return BeanBinding.create(source, getter, setter);
    }

    public static <T> Binding<List<T>> bindBeanListProperty(String property, Object source, Class<T> propertyType) {
        Method getter = ReflectionUtil.findGetter(property, source.getClass(), List.class);
        Method setter = ReflectionUtil.findSetter(property, source.getClass(), List.class);
        if (getter == null || setter == null) {
            logger.warn("Failed to resolve property {} of type {} - is the getter or setter missing?", property, source.getClass());
            return new DefaultBinding<>();
        }
        return BeanBinding.create(source, getter, setter);
    }

    public static <T, U> Binding<T> bindBoundBeanProperty(String property, Binding<U> source, Class<U> boundType, Class<T> propertyType) {
        Method getter = ReflectionUtil.findGetter(property, boundType, propertyType);
        Method setter = ReflectionUtil.findSetter(property, boundType, propertyType);
        if (getter == null || setter == null) {
            logger.warn("Failed to resolve property {} of type {} - is the getter or setter missing?", property, boundType);
            return new DefaultBinding<>();
        }
        return BeanBinding.createBound(source, getter, setter);
    }
}
