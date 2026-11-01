// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.GenericMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeHandlerLibraryTest {
    private static TypeHandlerLibrary typeHandlerLibrary;

    @BeforeAll
    public static void setup() {
        Reflections reflections = new Reflections(TypeHandlerLibraryTest.class.getClassLoader());
        typeHandlerLibrary = new TypeHandlerLibrary(reflections);
        TypeHandlerLibrary.populateBuiltInHandlers(typeHandlerLibrary);
    }

    @Test
    public void testMappedContainerHandler() {
        TypeHandler<AMappedContainer> handler = typeHandlerLibrary.getTypeHandler(AMappedContainer.class).get();

        assertTrue(handler instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    public void testCollectionHandler() {
        TypeHandler<Set<Integer>> setHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Set<Integer>>() { }).get();

        assertTrue(setHandler instanceof CollectionTypeHandler);

        TypeHandler<List<Integer>> listHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<List<Integer>>() { }).get();

        assertTrue(listHandler instanceof CollectionTypeHandler);

        TypeHandler<Queue<Integer>> queueHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Queue<Integer>>() { }).get();

        assertTrue(queueHandler instanceof CollectionTypeHandler);
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
    void testGenericMapHandler() {
        TypeHandler<Map<Integer, Integer>> handler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Map<Integer, Integer>>() {
                }).get();

        assertTrue(handler instanceof GenericMapTypeHandler);
    }

    @Test
    void testGetBaseTypeHandler() {
        TypeHandler<Integer> handler = typeHandlerLibrary.getBaseTypeHandler(TypeInfo.of(Integer.class));

        assertTrue(handler instanceof RuntimeDelegatingTypeHandler);
    }

    private enum AnEnum { }

    @MappedContainer
    private static class AMappedContainer { }
}
