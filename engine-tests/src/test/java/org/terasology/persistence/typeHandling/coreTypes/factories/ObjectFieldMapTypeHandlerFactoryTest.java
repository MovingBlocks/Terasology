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

import com.google.common.collect.Maps;
import org.junit.Test;
import org.reflections.Reflections;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.persistence.typeHandling.reflection.SerializationSandbox;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
