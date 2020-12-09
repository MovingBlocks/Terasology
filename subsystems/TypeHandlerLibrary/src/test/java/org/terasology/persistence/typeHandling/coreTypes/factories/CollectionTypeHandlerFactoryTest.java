// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CollectionTypeHandlerFactoryTest {
    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);
    private final CollectionTypeHandlerFactory typeHandlerFactory = new CollectionTypeHandlerFactory(new ConstructorLibrary(Maps.newHashMap()));

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, mock(SerializationSandbox.class));

    @Test
    public void testList() {
        TypeInfo<List<Integer>> listTypeInfo = new TypeInfo<List<Integer>>() {};

        Optional<TypeHandler<List<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof CollectionTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    public void testSet() {
        TypeInfo<Set<Integer>> listTypeInfo = new TypeInfo<Set<Integer>>() {};

        Optional<TypeHandler<Set<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof CollectionTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    public void testQueue() {
        TypeInfo<Queue<Integer>> listTypeInfo = new TypeInfo<Queue<Integer>>() {};

        Optional<TypeHandler<Queue<Integer>>> typeHandler =
                typeHandlerFactory.create(listTypeInfo, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof CollectionTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
    }

    @Test
    public void testNonGenericCollection() {
        class IntList extends ArrayList<Integer> {}

        Optional<TypeHandler<IntList>> typeHandler =
                typeHandlerFactory.create(TypeInfo.of(IntList.class), context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof CollectionTypeHandler);

        // Verify that the Integer TypeHandler was loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));
    }
}
