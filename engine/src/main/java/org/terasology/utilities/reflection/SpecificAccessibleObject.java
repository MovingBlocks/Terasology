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
package org.terasology.utilities.reflection;

/**
 */

import com.google.common.base.Preconditions;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Used to tie an object reference and an {@link AccessibleObject}
 *
 * @see Field
 * @see Method
 */
public class SpecificAccessibleObject<T extends AccessibleObject> {
    private T accessibleObject;
    private Object target;

    public SpecificAccessibleObject(T accessibleObject, Object target) {
        Preconditions.checkNotNull(accessibleObject);
        Preconditions.checkNotNull(target);

        this.accessibleObject = accessibleObject;
        this.target = target;
    }

    public static SpecificAccessibleObject<Method> method(Object target, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Method method = targetClass.getMethod(name, parameterTypes);

        return new SpecificAccessibleObject<>(method, target);
    }

    public static SpecificAccessibleObject<Field> field(Object target, String name) throws NoSuchFieldException {
        Class<?> targetClass = target.getClass();
        Field field = targetClass.getField(name);

        return new SpecificAccessibleObject<>(field, target);
    }

    public static SpecificAccessibleObject<Method> declaredMethod(Object target, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Method method = targetClass.getDeclaredMethod(name, parameterTypes);

        return new SpecificAccessibleObject<>(method, target);
    }

    public static SpecificAccessibleObject<Field> declaredField(Object target, String name) throws NoSuchFieldException {
        Class<?> targetClass = target.getClass();
        Field field = targetClass.getDeclaredField(name);

        return new SpecificAccessibleObject<>(field, target);
    }

    public Object getTarget() {
        return target;
    }

    public T getAccessibleObject() {
        return accessibleObject;
    }
}
