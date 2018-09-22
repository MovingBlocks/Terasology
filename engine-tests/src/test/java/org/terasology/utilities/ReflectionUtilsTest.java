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

import org.junit.Test;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategy;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class ReflectionUtilsTest {
    @Test
    public void testGetClassOfTypeWildcard() {
        class C<T> {}

        ParameterizedType cType = (ParameterizedType) new TypeInfo<C<?>>() {}.getType();
        Type wildcardType = cType.getActualTypeArguments()[0];

        assertEquals(Object.class, ReflectionUtil.getClassOfType(wildcardType));
    }

    @Test
    public void testGetParameterForField() throws Exception {
        assertEquals(EntityRef.class, ReflectionUtil.getTypeParameter(LocationComponent.class.getDeclaredField("children").getGenericType(), 0));
    }

    @Test
    public void testGetParameterForGenericInterface() throws Exception {
        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(SubInterfaceImplementor.class, CopyStrategy.class, 0));
    }

    @Test
    public void testGetParameterForBuriedGenericInterface() throws Exception {
        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(Subclass.class, CopyStrategy.class, 0));
    }

    @Test
    public void testGetParameterForUnboundGenericInterface() throws Exception {
        Type parameter = ReflectionUtil.getTypeParameterForSuper(new TypeInfo<UnboundInterfaceImplementor<?>>() {}.getType(), CopyStrategy.class, 0);

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

        class SuperClass<T> {}

        class SubClass<T> extends SuperClass<T> {}

        Type subInterfaceType = new TypeInfo<SubInterface<Integer>>() {}.getType();

        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(subInterfaceType, CopyStrategy.class, 0));

        Type subClassType = new TypeInfo<SubClass<Integer>>() {}.getType();

        assertEquals(Integer.class, ReflectionUtil.getTypeParameterForSuper(subClassType, SuperClass.class, 0));
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

        assertEquals(new TypeInfo<CopyStrategy<Float>>() {}.getType(), resolvedFieldType);
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

        assertEquals(new TypeInfo<CopyStrategy<Object>>() {}.getType(), resolvedFieldType);

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

        assertEquals(new TypeInfo<CopyStrategy<Integer>>() {}.getType(), resolvedFieldType);

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

    interface GenericInterfaceSubInterface extends CopyStrategy<Integer> {}

    class SubInterfaceImplementor implements GenericInterfaceSubInterface {
        @Override
        public Integer copy(Integer value) {
            return null;
        }
    }

    public static class ParameterisedInterfaceImplementor implements CopyStrategy<Integer> {

        @Override
        public Integer copy(Integer value) {
            return null;
        }
    }

    public static class Subclass extends ParameterisedInterfaceImplementor {
    }

    public static class UnboundInterfaceImplementor<T> implements CopyStrategy<T> {

        @Override
        public T copy(T value) {
            return null;
        }
    }

}
