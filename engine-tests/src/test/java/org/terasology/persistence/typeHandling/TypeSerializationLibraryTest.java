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
import org.terasology.persistence.typeHandling.coreTypes.CollectionTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.StringMapTypeHandler;
import org.terasology.reflection.MappedContainer;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TypeSerializationLibraryTest {
    private final ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
    private final CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
    private final TypeSerializationLibrary typeSerializationLibrary =
            new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary);

    private enum AnEnum {}

    @MappedContainer
    private static class AMappedContainer {}

    @Test
    public void testEnumHandler() {
        TypeHandler<AnEnum> handler = typeSerializationLibrary.getTypeHandler(AnEnum.class);

        assertTrue(handler instanceof EnumTypeHandler);
    }

    @Test
    public void testMappedContainerHandler() {
        TypeHandler<AMappedContainer> handler = typeSerializationLibrary.getTypeHandler(AMappedContainer.class);

        assertTrue(handler instanceof ObjectFieldMapTypeHandler);
    }

    @Test
    public void testCollectionHandler() {
        TypeHandler<Set<Integer>> setHandler =
                typeSerializationLibrary.getTypeHandler(new TypeInfo<Set<Integer>>() {});

        assertTrue(setHandler instanceof CollectionTypeHandler);

        TypeHandler<List<Integer>> listHandler =
                typeSerializationLibrary.getTypeHandler(new TypeInfo<List<Integer>>() {});

        assertTrue(listHandler instanceof CollectionTypeHandler);

        TypeHandler<Queue<Integer>> queueHandler =
                typeSerializationLibrary.getTypeHandler(new TypeInfo<Queue<Integer>>() {});

        assertTrue(queueHandler instanceof CollectionTypeHandler);
    }

    @Test
    public void testStringMapHandler() {
        TypeHandler<Map<String, Integer>> handler =
                typeSerializationLibrary.getTypeHandler(new TypeInfo<Map<String, Integer>>() {});

        assertTrue(handler instanceof StringMapTypeHandler);
    }

    @Test
    public void testInvalidTypeHandler() {
        TypeHandler<Map<Integer, Integer>> handler =
                typeSerializationLibrary.getTypeHandler(new TypeInfo<Map<Integer, Integer>>() {});

        assertNull(handler);
    }
}
