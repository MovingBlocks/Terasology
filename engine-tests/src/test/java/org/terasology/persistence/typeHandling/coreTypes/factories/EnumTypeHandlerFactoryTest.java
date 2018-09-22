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
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

import static org.junit.Assert.*;

public class EnumTypeHandlerFactoryTest {
    private enum SomeEnum {
        A, B
    }

    @Test
    public void testEnum() {
        EnumTypeHandlerFactory typeHandlerFactory = new EnumTypeHandlerFactory();
        // EnumTypeHandlerFactory does not require a TypeSerializationLibrary
        Optional<TypeHandler<SomeEnum>> typeHandler = typeHandlerFactory.create(TypeInfo.of(SomeEnum.class), null);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof EnumTypeHandler);
    }

    @Test
    public void testNonEnum() {
        EnumTypeHandlerFactory typeHandlerFactory = new EnumTypeHandlerFactory();

        // EnumTypeHandlerFactory does not require a TypeSerializationLibrary
        Optional<TypeHandler<Integer>> typeHandler = typeHandlerFactory.create(TypeInfo.of(Integer.class), null);

        assertFalse(typeHandler.isPresent());
    }
}
