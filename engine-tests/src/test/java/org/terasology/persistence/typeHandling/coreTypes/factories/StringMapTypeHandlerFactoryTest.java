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
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class StringMapTypeHandlerFactoryTest {
    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final StringMapTypeHandlerFactory typeHandlerFactory = new StringMapTypeHandlerFactory();

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, mock(SerializationSandbox.class));

    @Test
    public void testStringMap() {
        TypeInfo<Map<String, Integer>> listTypeInfo = new TypeInfo<Map<String, Integer>>() {};

        Optional<TypeHandler<Map<String, Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof StringMapTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    public void testNonStringMap() {
        TypeInfo<Set<Integer>> listTypeInfo = new TypeInfo<Set<Integer>>() {};

        Optional<TypeHandler<Set<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertFalse(typeHandler.isPresent());
    }

    @Test
    public void testNonGenericMap() {
        class IntMap extends HashMap<String, Integer> {}

        Optional<TypeHandler<IntMap>> typeHandler =
                typeHandlerFactory.create(TypeInfo.of(IntMap.class), context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof StringMapTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(Integer.class).getType()));
    }
}
