// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeHandlerLibraryImplTest {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());
    private final TypeHandlerLibrary typeHandlerLibrary = new TypeHandlerLibraryImpl(reflections);


    @MappedContainer
    private static class AMappedContainer {}

    @Test
    public void testMappedContainerHandler() {
        TypeHandler<AMappedContainer> handler = typeHandlerLibrary.getTypeHandler(AMappedContainer.class).get();

        assertTrue(handler instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    public void testCollectionHandler() {
        TypeHandler<Set<Integer>> setHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Set<Integer>>() {}).get();

        assertTrue(setHandler instanceof CollectionTypeHandler);

        TypeHandler<List<Integer>> listHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<List<Integer>>() {}).get();

        assertTrue(listHandler instanceof CollectionTypeHandler);

        TypeHandler<Queue<Integer>> queueHandler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Queue<Integer>>() {}).get();

        assertTrue(queueHandler instanceof CollectionTypeHandler);
    }

}
