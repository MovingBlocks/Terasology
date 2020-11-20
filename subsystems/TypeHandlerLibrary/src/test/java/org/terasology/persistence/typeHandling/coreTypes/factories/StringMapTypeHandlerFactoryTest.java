// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StringMapTypeHandlerFactoryTest {
    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final StringMapTypeHandlerFactory typeHandlerFactory = new StringMapTypeHandlerFactory();

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, mock(SerializationSandbox.class));

    @Test
    void testStringMap() {
        TypeInfo<Map<String, Integer>> listTypeInfo = new TypeInfo<Map<String, Integer>>() {};

        Optional<TypeHandler<Map<String, Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof StringMapTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    void testNonStringMap() {
        TypeInfo<Set<Integer>> listTypeInfo = new TypeInfo<Set<Integer>>() {};

        Optional<TypeHandler<Set<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertFalse(typeHandler.isPresent());
    }

    @Test
    void testNonGenericMap() {
        class IntMap extends HashMap<String, Integer> {}

        Optional<TypeHandler<IntMap>> typeHandler =
                typeHandlerFactory.create(TypeInfo.of(IntMap.class), context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof StringMapTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(Integer.class).getType()));
    }
}
