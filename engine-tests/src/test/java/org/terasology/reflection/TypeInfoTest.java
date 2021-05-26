// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.reflection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeInfoTest {
    @Test
    public void testSimpleTypeInfo() {
        TypeInfo<String> typeInfo = TypeInfo.of(String.class);

        assertEquals(String.class, typeInfo.getRawType());
        assertTrue(typeInfo.getType() instanceof Class);
        assertEquals(typeInfo.getRawType(), typeInfo.getType());
    }

    @Test
    public void testListTypeInfo() {
        TypeInfo<List<Integer>> typeInfo = new TypeInfo<List<Integer>>() {
        };

        assertEquals(List.class, typeInfo.getRawType());
        assertTrue(typeInfo.getType() instanceof ParameterizedType);
        assertEquals(Integer.class, ((ParameterizedType) typeInfo.getType()).getActualTypeArguments()[0]);
    }

    @Test
    public void testArrayTypeInfo() {
        TypeInfo<List<Integer>[]> typeInfo = new TypeInfo<List<Integer>[]>() {
        };

        assertEquals(List[].class, typeInfo.getRawType());
        assertTrue(typeInfo.getType() instanceof GenericArrayType);
        assertEquals(
                new TypeInfo<List<Integer>>() {
                }
                        .getType(),
                ((GenericArrayType) typeInfo.getType()).getGenericComponentType()
        );
    }

    @Test
    public void testWildcardGenericTypeInfo() {
        TypeInfo<List<? extends Number>> typeInfo = new TypeInfo<List<? extends Number>>() {
        };

        assertEquals(List.class, typeInfo.getRawType());
        assertTrue(typeInfo.getType() instanceof ParameterizedType);
        Type genericType = ((ParameterizedType) typeInfo.getType()).getActualTypeArguments()[0];
        assertTrue(genericType instanceof WildcardType);
    }
}
