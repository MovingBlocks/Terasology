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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 */
public class ReflectionUtilsTest {

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
        assertTrue(ReflectionUtil.getTypeParameterForSuper(UnboundInterfaceImplementor.class, CopyStrategy.class, 0) instanceof TypeVariable);
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
