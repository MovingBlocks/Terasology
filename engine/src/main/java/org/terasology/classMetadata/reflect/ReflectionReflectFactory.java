/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.classMetadata.reflect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.utilities.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection based implementation of ReflectFactory. Uses standard Java reflection to provide the necessary reflection functionality.
 *
 * @author Immortius
 */
public class ReflectionReflectFactory implements ReflectFactory {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionReflectFactory.class);

    @Override
    public <T> ObjectConstructor<T> createConstructor(Class<T> type) throws NoSuchMethodException {
        return new ReflectionConstructor<>(type);
    }

    @Override
    public <T> FieldAccessor<T, ?> createFieldAccessor(Class<T> ownerType, Field field) {
        return new ReflectionFieldAccessor<>(field);
    }

    @Override
    public <T, U> FieldAccessor<T, U> createFieldAccessor(Class<T> ownerType, Field field, Class<U> fieldType) {
        return new ReflectionFieldAccessor<>(field);
    }

    /**
     * ObjectConstructor using a Java Constructor instance to construct the object
     *
     * @param <T>
     */
    private static class ReflectionConstructor<T> implements ObjectConstructor<T> {
        private Class<T> type;
        private Constructor<T> constructor;

        public ReflectionConstructor(Class<T> type) throws NoSuchMethodException {
            this.type = type;
            constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
        }

        @Override
        public T construct() {
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                logger.error("Exception instantiating type: {}", type, e);
            }
            return null;
        }
    }

    /**
     * Field accessor using reflection to access the getter and setter methods, or the field directly if necessary.
     *
     * @param <T>
     * @param <U>
     */
    private static class ReflectionFieldAccessor<T, U> implements FieldAccessor<T, U> {

        private Field field;
        private Method getter;
        private Method setter;

        @SuppressWarnings("unchecked")
        public ReflectionFieldAccessor(Field field) {
            this.field = field;
            getter = ReflectionUtil.findGetter(field);
            setter = ReflectionUtil.findSetter(field);
            if (getter == null || setter == null) {
                field.setAccessible(true);
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public U getValue(T target) {
            try {
                if (getter != null) {
                    return (U) (getter.invoke(target));
                }
                return (U) field.get(target);
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error("Exception during access of {} from {}", field.getName(), target.getClass(), e);
            }
            return null;
        }

        @Override
        public void setValue(T target, U value) {
            try {
                if (setter != null) {
                    setter.invoke(target, value);
                } else {
                    field.set(target, value);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                logger.error("Exception during setting of {} from {}", field.getName(), target.getClass(), e);
            }
        }
    }
}
