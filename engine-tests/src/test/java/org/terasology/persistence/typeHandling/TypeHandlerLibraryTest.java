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
package org.terasology.persistence.typeHandling;

import org.junit.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.RuntimeDelegatingTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.TypeInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeHandlerLibraryTest {
    private final Reflections reflections = new Reflections(getClass().getClassLoader());
    private final TypeHandlerLibrary typeHandlerLibrary = new TypeHandlerLibrary(reflections);

    private enum AnEnum {}

    @MappedContainer
    private static class AMappedContainer {}

    @Test
    public void testEnumHandler() {
        TypeHandler<AnEnum> handler = typeHandlerLibrary.getTypeHandler(AnEnum.class).get();

        assertTrue(handler instanceof EnumTypeHandler);
    }

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

    @Test
    public void testStringMapHandler() {
        TypeHandler<Map<String, Integer>> handler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Map<String, Integer>>() {}).get();

        assertTrue(handler instanceof StringMapTypeHandler);
    }

    @Test
    public void testInvalidTypeHandler() {
        Optional<TypeHandler<Map<Integer, Integer>>> handler =
                typeHandlerLibrary.getTypeHandler(new TypeInfo<Map<Integer, Integer>>() {});

        assertFalse(handler.isPresent());
    }

    @Test
    public void testGetBaseTypeHandler() {
        TypeHandler<Integer> handler = typeHandlerLibrary.getBaseTypeHandler(TypeInfo.of(Integer.class));

        assertTrue(handler instanceof RuntimeDelegatingTypeHandler);
    }
}
