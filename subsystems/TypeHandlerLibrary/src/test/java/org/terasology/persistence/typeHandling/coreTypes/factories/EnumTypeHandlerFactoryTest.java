// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.EnumTypeHandler;
import org.terasology.reflection.TypeInfo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumTypeHandlerFactoryTest {
    private enum SomeEnum {
        A, B
    }

    @Test
    void testEnum() {
        EnumTypeHandlerFactory typeHandlerFactory = new EnumTypeHandlerFactory();
        // EnumTypeHandlerFactory does not require a TypeHandlerLibrary
        Optional<TypeHandler<SomeEnum>> typeHandler = typeHandlerFactory.create(TypeInfo.of(SomeEnum.class), null);

        assertTrue(typeHandler.isPresent());
        assertTrue(typeHandler.get() instanceof EnumTypeHandler);
    }

    @Test
    void testNonEnum() {
        EnumTypeHandlerFactory typeHandlerFactory = new EnumTypeHandlerFactory();

        // EnumTypeHandlerFactory does not require a TypeHandlerLibrary
        Optional<TypeHandler<Integer>> typeHandler = typeHandlerFactory.create(TypeInfo.of(Integer.class), null);

        assertFalse(typeHandler.isPresent());
    }
}
