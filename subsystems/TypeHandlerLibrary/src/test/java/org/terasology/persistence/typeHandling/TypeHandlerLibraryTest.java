// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeHandlerLibraryTest {
    private static Reflections reflections;
    private static TypeHandlerLibrary typeHandlerLibrary;

    @BeforeAll
    public static void setup() {
        reflections = new Reflections(TypeHandlerLibraryTest.class.getClassLoader());
        typeHandlerLibrary = new TypeHandlerLibrary(reflections);
        TypeHandlerLibrary.populateBuiltInHandlers(typeHandlerLibrary);
    }

    @Test
    void testEnumHandler() {
        TypeHandler<AnEnum> handler = typeHandlerLibrary.getTypeHandler(AnEnum.class).get();

        assertTrue(handler instanceof EnumTypeHandler);
    }

    @Test
    void testStringMapHandler() {
        TypeHandler<Map<String, Integer>> handler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Map<String, Integer>>() {
                }).get();

        assertTrue(handler instanceof StringMapTypeHandler);
    }

    @Test
    void testInvalidTypeHandler() {
        Optional<TypeHandler<Map<Integer, Integer>>> handler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Map<Integer, Integer>>() {
                });

        assertFalse(handler.isPresent());
    }

    @Test
    void testGetBaseTypeHandler() {
        TypeHandler<Integer> handler = typeHandlerLibrary.getBaseTypeHandler(TypeInfo.of(Integer.class));

        assertTrue(handler instanceof RuntimeDelegatingTypeHandler);
    }

    private enum AnEnum {}
}
