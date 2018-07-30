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

package org.terasology.utilities;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.terasology.rendering.nui.UIWidget;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 *
 */
public final class ReflectionUtil {
    private ReflectionUtil() {
    }

    private static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Returns true if {@link Type} {@code a} and {@code b} are equal.
     */
    public static boolean typeEquals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            // TODO: save a .clone() call
            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType())
                    && pa.getRawType().equals(pb.getRawType())
                    && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return typeEquals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds())
                    && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration()
                    && va.getName().equals(vb.getName());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }


    /**
     * Attempts to return the type of a parameter of a parameterised field. This uses compile-time information only - the
     * type should be obtained from a field with a the generic types bound.
     *
     * @param type
     * @param index
     * @return The type of the generic parameter at index for the given type, or null if it cannot be obtained.
     */
    // TODO - Improve parameter lookup to go up the inheritance tree more
    public static Type getTypeParameter(Type type, int index) {
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        if (parameterizedType.getActualTypeArguments().length < index + 1) {
            return null;
        }
        return parameterizedType.getActualTypeArguments()[index];
    }

    public static Class<?> getClassOfType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            return Array.newInstance(getClassOfType(genericArrayType.getGenericComponentType()), 0).getClass();
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            return getClassOfType(wildcardType.getUpperBounds()[0]);
        }
        return Object.class;
    }

    public static Method findGetter(Field field) {
        return findGetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findGetter(String propertyName, Class<?> beanClass, Class<?> propertyType) {
        Method result = findGetter(propertyName, beanClass);
        if (result != null && propertyType.equals(result.getReturnType())) {
            return result;
        }
        return null;
    }

    public static Method findGetter(String propertyName, Class<?> beanClass) {
        Method result = findMethod(beanClass, "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null) {
            result.setAccessible(true);
            return result;
        }
        result = findMethod(beanClass, "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1));
        if (result != null) {
            result.setAccessible(true);
            return result;
        }
        return null;
    }

    public static Method findSetter(Field field) {
        return findSetter(field.getName(), field.getDeclaringClass(), field.getType());
    }

    public static Method findSetter(String propertyName, Class<?> beanClass, Class<?> propertyType) {
        String setterName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method result = findMethod(beanClass, setterName, propertyType);
        if (result != null) {
            result.setAccessible(true);
        }
        return result;
    }

    public static Method findMethod(Class<?> targetType, String methodName, Class<?>... parameters) {
        try {
            return targetType.getMethod(methodName, parameters);
        } catch (NoSuchMethodException me) {
            // We're expecting not to find methods
            return null;
        }
    }

    /**
     * Returns an ordered list of super classes and interfaces for the given class, that have a common base class.
     * The set is ordered with the deepest interface first, through all the interfaces, and then all the super classes.
     *
     * @param forClass
     * @param baseClass
     * @return an ordered list of super classes and interfaces for the given class, that have a common base class.
     */
    public static <T> List<Class<? extends T>> getInheritanceTree(Class<? extends T> forClass, Class<T> baseClass) {
        Set<Class<? extends T>> result = Sets.newLinkedHashSet();
        for (Class<?> interfaceType : forClass.getInterfaces()) {
            if (baseClass.isAssignableFrom(interfaceType)) {
                addInterfaceToInheritanceTree((Class<? extends T>) interfaceType, baseClass, result);
            }
        }
        addClassToInheritanceTree(forClass, baseClass, result);
        return Lists.newArrayList(result);
    }

    private static <T> void addClassToInheritanceTree(Class<? extends T> element, Class<T> baseClass, Set<Class<? extends T>> result) {
        for (Class<?> interfaceType : element.getInterfaces()) {
            if (baseClass.isAssignableFrom(interfaceType)) {
                addInterfaceToInheritanceTree((Class<? extends T>) interfaceType, baseClass, result);
            }
        }
        if (element.getSuperclass() != null && baseClass.isAssignableFrom(element.getSuperclass())) {
            addClassToInheritanceTree((Class<? extends T>) element.getSuperclass(), baseClass, result);
        }
        result.add(element);
    }

    private static <T> void addInterfaceToInheritanceTree(Class<? extends T> interfaceType, Class<T> baseClass, Set<Class<? extends T>> result) {
        for (Class<?> parentInterface : interfaceType.getInterfaces()) {
            if (UIWidget.class.isAssignableFrom(parentInterface)) {
                addInterfaceToInheritanceTree((Class<? extends T>) parentInterface, baseClass, result);
            }
        }
        result.add(interfaceType);
    }

    public static <T> Type getTypeParameterForSuper(Type target, Class<T> superClass, int index) {
        Class targetClass = getClassOfType(target);
        Preconditions.checkArgument(superClass.isAssignableFrom(targetClass), "Target must be a child of superClass");

        if (superClass.isInterface()) {
            return getTypeParameterForSuperInterface(target, superClass, index);
        } else {
            return getTypeParameterForSuperClass(target, superClass, index);
        }
    }

    private static <T> Type getTypeParameterForSuperClass(Type target, Class<T> superClass, int index) {
        for (Class targetClass = getClassOfType(target);
             !Object.class.equals(targetClass);
             target = targetClass.getGenericSuperclass(), targetClass = getClassOfType(target)) {
            if (superClass.equals(targetClass)) {
                return getTypeParameter(target, index);
            }
        }

        return null;
    }

    private static <T> Type getTypeParameterForSuperInterface(Type target, Class<T> superClass, int index) {
        Class targetClass = getClassOfType(target);

        if (Object.class.equals(targetClass)) {
            return null;
        }

        if (targetClass.equals(superClass)) {
            return getTypeParameter(target, index);
        }

        Type genericSuperclass = targetClass.getGenericSuperclass();

        if (!Object.class.equals(genericSuperclass) && genericSuperclass != null) {
            return getTypeParameterForSuperInterface(genericSuperclass, superClass, index);
        }

        for (Type genericInterface : targetClass.getGenericInterfaces()) {
            Type typeParameter = getTypeParameterForSuperInterface(genericInterface, superClass, index);

            if (typeParameter != null) {
                return typeParameter;
            }
        }

        return null;
    }

    public static Type resolveFieldType(Type classType, Type fieldType) {
        Class<?> classClass = getClassOfType(classType);

        // T field;
        if (fieldType instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) fieldType;

            Preconditions.checkArgument(typeVariable.getGenericDeclaration() instanceof Class,
                    "fieldType has not been declared in a class and cannot be a field's type");

            Class<?> declaringClass = (Class<?>) typeVariable.getGenericDeclaration();

            Preconditions.checkArgument(declaringClass.isAssignableFrom(classClass),
                    "Field was not declared in class " + classClass);

            List<TypeVariable<?>> typeParameters =
                    Arrays.asList(declaringClass.getTypeParameters());

            int typeParameterIndex = typeParameters.indexOf(typeVariable);

            if (!classClass.equals(declaringClass)) {
                return getTypeParameterForSuper(classType, declaringClass, typeParameterIndex);
            }
            return getTypeParameter(classType, typeParameterIndex);
        }

        // T[] field || List<T>[] field;
        if (fieldType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) fieldType;

            Type componentType = arrayType.getGenericComponentType();
            Type resolvedComponentType = resolveFieldType(classType, componentType);

            if (resolvedComponentType == componentType) {
                return fieldType;
            } else {
                return new GenericArrayType() {
                    final Type genericComponentType = resolvedComponentType;
                    @Override
                    public Type getGenericComponentType() {
                        return genericComponentType;
                    }
                };
            }
        }

        return fieldType;
    }

    public static Object readField(Object object, String fieldName) {
        Class<?> cls = object.getClass();
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                final Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(object);
            } catch (final NoSuchFieldException e) {
                // Try parent
            } catch (Exception e) {
                throw new IllegalArgumentException(
                        "Cannot access field " + cls.getName() + "." + fieldName, e);
            }
        }
        throw new IllegalArgumentException(
                "Cannot find field " + cls.getName() + "." + fieldName);
    }
}
