// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ObjectFieldMapTypeHandlerFactoryTest {
    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);

    private final ConstructorLibrary constructorLibrary = new ConstructorLibrary(Maps.newHashMap());
    private final ObjectFieldMapTypeHandlerFactory typeHandlerFactory = new ObjectFieldMapTypeHandlerFactory(
            constructorLibrary);

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, mock(SerializationSandbox.class));

    private static class SomeClass<T> {
        private T t;
        private List<T> list;
    }

    @Test
    public void testObject() {
        Optional<TypeHandler<SomeClass<Integer>>> typeHandler =
                typeHandlerFactory.create(new TypeInfo<SomeClass<Integer>>() {}, context);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ObjectFieldMapTypeHandler);

        // Verify that the Integer and List<Integer> TypeHandlers were loaded from the TypeHandlerLibrary
        verify(typeHandlerLibrary).getTypeHandler(eq(TypeInfo.of(Integer.class).getType()));

        verify(typeHandlerLibrary).getTypeHandler(eq(new TypeInfo<List<Integer>>() {}.getType()));
    }
}
