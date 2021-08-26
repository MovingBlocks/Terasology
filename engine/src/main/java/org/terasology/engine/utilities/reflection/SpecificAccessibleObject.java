// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.reflection;


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

    public static SpecificAccessibleObject<Method> method(Object target, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Class<?> targetClass = target.getClass();
        Method method = targetClass.getMethod(name, parameterTypes);

        return new SpecificAccessibleObject<>(method, target);
    }

    public static SpecificAccessibleObject<Field> field(Object target, String name) throws NoSuchFieldException {
        Class<?> targetClass = target.getClass();
        Field field = targetClass.getField(name);

        return new SpecificAccessibleObject<>(field, target);
    }

    public static SpecificAccessibleObject<Method> declaredMethod(Object target, String name, Class<?>... parameterTypes)
            throws NoSuchMethodException {
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
