// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.utilities;

import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.utilities.ReflectionUtil;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategy;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReflectionUtilTest {
    @Test
    public void testGetClassOfTypeWildcard() {
        class C<T> { }

        ParameterizedType cType = (ParameterizedType) new TypeInfo<C<?>>() { }.getType();
        Type wildcardType = cType.getActualTypeArguments()[0];

        assertEquals(Object.class, ReflectionUtil.getRawType(wildcardType));
    }

    @Test
    public void testGetParameterForField() throws Exception {
        assertEquals(EntityRef.class, ReflectionUtil.getTypeParameter(LocationComponent.class.getDeclaredField("children").getGenericType(), 0));
    }

    @Test
    public void testGetParameterForGenericInterface() {
        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(SubInterfaceImplementor.class, CopyStrategy.class, 0));
    }

    @Test
    public void testGetParameterForBuriedGenericInterface() {
        class Base<T> { }
        class Sub extends Base<Integer> implements MarkerA<String> { }

        Type parameter = ReflectionUtil.getTypeParameterForSuper(Sub.class, MarkerA.class, 0);

        assertEquals(String.class, parameter);
    }

    @Test
    public void testGetParameterForUnboundGenericInterface() {
        Type parameter = ReflectionUtil.getTypeParameterForSuper(new TypeInfo<UnboundInterfaceImplementor<?>>() { }.getType(), CopyStrategy.class, 0);

        assertTrue(parameter instanceof WildcardType);
    }

    @Test
    public void testGetTypeParameterForGenericSupertypeInGenericSubclass() {
        class SubInterface<T> implements CopyStrategy<T> {
            @Override
            public T copy(T value) {
                return null;
            }
        }

        class SubClass extends SubInterface<String> { }

        Type subInterfaceType = new TypeInfo<SubInterface<Integer>>() { }.getType();

        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(subInterfaceType, CopyStrategy.class, 0));

        Type subClassType = new TypeInfo<SubClass>() { }.getType();

        assertEquals(String.class, ReflectionUtil.getTypeParameterForSuper(subClassType, CopyStrategy.class, 0));
    }

    @Test
    public void testResolveTypeVariable() {
        class SomeClass<T> {
            private T t;
        }

        TypeInfo<SomeClass<Float>> typeInfo = new TypeInfo<SomeClass<Float>>() {
        };

        Type resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        assertEquals(Float.class, resolvedFieldType);
    }

    @Test
    public void testResolveParameterizedType() {
        class SomeClass<T> {
            private CopyStrategy<T> t;
        }

        TypeInfo<SomeClass<Float>> typeInfo = new TypeInfo<SomeClass<Float>>() {
        };

        Type resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        assertEquals(new TypeInfo<CopyStrategy<Float>>() { }.getType(), resolvedFieldType);
    }

    @Test
    public void testResolveRawParameterizedType() {
        class SomeClass<T> {
            private CopyStrategy<T> t;
            private T o;
        }

        TypeInfo<SomeClass> typeInfo = new TypeInfo<SomeClass>() {
        };

        Type resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        assertEquals(new TypeInfo<CopyStrategy<Object>>() { }.getType(), resolvedFieldType);

        resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[1].getGenericType()
        );

        assertEquals(TypeInfo.of(Object.class).getType(), resolvedFieldType);
    }

    @Test
    public void testResolveNothing() {
        class SomeClass {
            private CopyStrategy<Integer> t;
            private String o;
        }

        TypeInfo<SomeClass> typeInfo = new TypeInfo<SomeClass>() {
        };

        Type resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        assertEquals(new TypeInfo<CopyStrategy<Integer>>() { }.getType(), resolvedFieldType);

        resolvedFieldType = ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[1].getGenericType()
        );

        assertEquals(TypeInfo.of(String.class).getType(), resolvedFieldType);
    }

    @Test
    public void testResolveGenericArray() {
        class SomeClass<T> {
            private T[] t;
        }

        TypeInfo<SomeClass<Float>> typeInfo = new TypeInfo<SomeClass<Float>>() {
        };

        GenericArrayType resolvedFieldType = (GenericArrayType) ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        assertEquals(Float[].class.getComponentType(), resolvedFieldType.getGenericComponentType());
    }

    @Test
    public void testResolveWildcardType() {
        class SomeClass<T, U> {
            private CopyStrategy<? extends T> t;
            private CopyStrategy<? super U> u;
        }

        TypeInfo<SomeClass<Float, Integer>> typeInfo = new TypeInfo<SomeClass<Float, Integer>>() {
        };

        ParameterizedType resolvedFieldType = (ParameterizedType) ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[0].getGenericType()
        );

        WildcardType resolvedWildcardType = (WildcardType) resolvedFieldType.getActualTypeArguments()[0];

        assertEquals(Float.class, resolvedWildcardType.getUpperBounds()[0]);

        resolvedFieldType = (ParameterizedType) ReflectionUtil.resolveType(
            typeInfo.getType(),
            typeInfo.getRawType().getDeclaredFields()[1].getGenericType()
        );

        resolvedWildcardType = (WildcardType) resolvedFieldType.getActualTypeArguments()[0];

        assertEquals(Integer.class, resolvedWildcardType.getLowerBounds()[0]);
    }

    @Test
    public void testResolveThroughGenericSupertypes() {
        class B<S, T> implements MarkerB<S>, MarkerA<T> { }

        ParameterizedType resolvedTypeForB = (ParameterizedType) ReflectionUtil.resolveType(
            new TypeInfo<MarkerA<String>>() { }.getType(),
            ReflectionUtil.parameterizeRawType(B.class)
        );

        // Could not resolve S
        assertEquals(Object.class, resolvedTypeForB.getActualTypeArguments()[0]);
        // Could resolve T
        assertEquals(String.class, resolvedTypeForB.getActualTypeArguments()[1]);
    }

    @Test
    public void testResolveThroughInheritanceTree() {
        class A<S, T> implements MarkerA<T>, MarkerC<S> { }
        class B<S, T> extends A<S, T> implements MarkerB<T>, MarkerC<S> { }
        class C<T> extends B<Integer, T> { }

        final Type typeToResolve = ReflectionUtil.parameterizeRawType(C.class);

        ParameterizedType resolvedThroughMarkerA = (ParameterizedType) ReflectionUtil.resolveType(
            new TypeInfo<MarkerA<String>>() { }.getType(),
            typeToResolve
        );

        assertEquals(String.class, resolvedThroughMarkerA.getActualTypeArguments()[0]);

        ParameterizedType resolvedThroughMarkerB = (ParameterizedType) ReflectionUtil.resolveType(
            new TypeInfo<MarkerB<String>>() { }.getType(),
            typeToResolve
        );

        assertEquals(String.class, resolvedThroughMarkerB.getActualTypeArguments()[0]);


        ParameterizedType resolvedThroughAWithIncorrectFirstType =
            (ParameterizedType) ReflectionUtil.resolveType(
                new TypeInfo<A<String, String>>() { }.getType(),
                typeToResolve
            );

        assertEquals(String.class, resolvedThroughAWithIncorrectFirstType.getActualTypeArguments()[0]);
    }

    interface MarkerA<T> { }

    interface MarkerB<T> { }

    interface MarkerC<T> { }

    interface GenericInterfaceSubInterface extends CopyStrategy<Integer> { }

    public static class UnboundInterfaceImplementor<T> implements CopyStrategy<T> {

        @Override
        public T copy(T value) {
            return null;
        }
    }

    class SubInterfaceImplementor implements GenericInterfaceSubInterface {
        @Override
        public Integer copy(Integer value) {
            return null;
        }
    }

}
