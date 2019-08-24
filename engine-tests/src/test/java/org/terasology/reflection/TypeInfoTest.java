/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.reflection;

import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
