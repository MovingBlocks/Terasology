// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.registry.In;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
public class NestedTest {
    @In
    public static Engines outerEngines;

    @In
    public static EntityManager outerManager;

    @Test
    public void outerTest() {
        Assertions.assertNotNull(outerEngines);
        Assertions.assertNotNull(outerManager);
    }

    @Nested
    class NestedTestClass {
        @In
        Engines innerEngines;

        @In
        EntityManager innerManager;

        @Test
        public void innerTest() {
            Assertions.assertSame(innerManager, outerManager);
            Assertions.assertSame(innerEngines, outerEngines);
        }
    }
}
