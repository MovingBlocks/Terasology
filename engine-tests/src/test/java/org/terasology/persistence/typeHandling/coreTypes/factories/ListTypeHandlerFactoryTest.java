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
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ListTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ListTypeHandlerFactoryTest {
    private final TypeSerializationLibrary typeSerializationLibrary = mock(TypeSerializationLibrary.class);
    private final ListTypeHandlerFactory typeHandlerFactory = new ListTypeHandlerFactory();

    @Test
    public void testList() {
        TypeInfo<List<Integer>> listTypeInfo = new TypeInfo<List<Integer>>() {};

        Optional<TypeHandler<List<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, typeSerializationLibrary);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ListTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeSerializationLibrary
        verify(typeSerializationLibrary).getHandlerFor(ArgumentMatchers.eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    public void testNonList() {
        TypeInfo<Set<Integer>> listTypeInfo = new TypeInfo<Set<Integer>>() {};

        Optional<TypeHandler<Set<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, typeSerializationLibrary);

        assertFalse(typeHandler.isPresent());
    }
}
