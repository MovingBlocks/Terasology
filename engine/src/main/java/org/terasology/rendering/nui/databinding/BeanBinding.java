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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 */
public final class BeanBinding<T> implements Binding<T> {
    private static final Logger logger = LoggerFactory.getLogger(BeanBinding.class);

    private Binding<?> bean = new DefaultBinding<>();
    private Method getter;
    private Method setter;

    private BeanBinding(Binding<?> bean, Method getter, Method setter) {
        this.bean = bean;
        this.getter = getter;
        this.setter = setter;
    }

    public static <T> BeanBinding<T> create(Object bean, Method getter, Method setter) {
        return new BeanBinding<>(new DefaultBinding<>(bean), getter, setter);
    }

    public static <T> BeanBinding<T> createBound(Binding<?> binding, Method getter, Method setter) {
        return new BeanBinding<>(binding, getter, setter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get() {
        if (bean.get() != null) {
            try {
                return (T) getter.invoke(bean.get());
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                logger.error("Failed to retrieve value through getter", e);
            }
        }
        return null;
    }

    @Override
    public void set(T value) {
        if (bean.get() != null) {
            try {
                setter.invoke(bean.get(), value);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                logger.error("Failed to set value through setter", e);
            }
        }
    }
}
