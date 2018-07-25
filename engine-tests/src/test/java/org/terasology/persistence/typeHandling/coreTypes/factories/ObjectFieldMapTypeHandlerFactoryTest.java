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
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ObjectFieldMapTypeHandlerFactoryTest {
    private final TypeSerializationLibrary typeSerializationLibrary = mock(TypeSerializationLibrary.class);

    private final ReflectionReflectFactory reflectFactory = new ReflectionReflectFactory();
    private final CopyStrategyLibrary copyStrategyLibrary = new CopyStrategyLibrary(reflectFactory);
    private final ObjectFieldMapTypeHandlerFactory typeHandlerFactory = new ObjectFieldMapTypeHandlerFactory(
            reflectFactory, copyStrategyLibrary
    );

    public static class SomeClass {
        public int someInt;
        public String someString;
    }

    @Test
    public void testObject() {
        Optional<TypeHandler<SomeClass>> typeHandler =
                typeHandlerFactory.create(TypeInfo.of(SomeClass.class), typeSerializationLibrary);


        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof ObjectFieldMapTypeHandler);

        // Verify that the Integer and String TypeHandlers were loaded from the TypeSerializationLibrary
        verify(typeSerializationLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(Integer.TYPE).getType()));
        verify(typeSerializationLibrary).getTypeHandler(ArgumentMatchers.eq(TypeInfo.of(String.class).getType()));
    }
}
