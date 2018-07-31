/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.junit.Test;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ArrayTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ArrayTypeHandlerFactoryTest {
    private final TypeSerializationLibrary typeSerializationLibrary = mock(TypeSerializationLibrary.class);
    private final ArrayTypeHandlerFactory typeHandlerFactory = new ArrayTypeHandlerFactory();

    @Test
    public void testArray() {
        TypeInfo<int[]> arrayTypeInfo = TypeInfo.of(int[].class);

        Optional<TypeHandler<int[]>> typeHandler =
                typeHandlerFactory.create(arrayTypeInfo, typeSerializationLibrary);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ArrayTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeSerializationLibrary
        verify(typeSerializationLibrary).getTypeHandler(eq(TypeInfo.of(int.class).getType()));
    }

    @Test
    public void testGenericArray() {
        TypeInfo<List<Integer>[]> arrayTypeInfo = new TypeInfo<List<Integer>[]>() {};

        Optional<TypeHandler<List<Integer>[]>> typeHandler =
                typeHandlerFactory.create(arrayTypeInfo, typeSerializationLibrary);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ArrayTypeHandler);

        // Verify that the List<Integer> TypeHandler was loaded from the TypeSerializationLibrary
        verify(typeSerializationLibrary).getTypeHandler(eq(new TypeInfo<List<Integer>>() {}.getType()));
    }

    @Test
    public void testNonArray() {
        TypeInfo<List<Integer>> arrayTypeInfo = new TypeInfo<List<Integer>>() {};

        Optional<TypeHandler<List<Integer>>> typeHandler =
                typeHandlerFactory.create(arrayTypeInfo, typeSerializationLibrary);

        assertFalse(typeHandler.isPresent());
    }
}
