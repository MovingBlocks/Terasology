// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.extension.Dependencies;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.DummyComponent;
import org.terasology.unittest.stubs.DummyEvent;

@Tag("MteTest")
@ExtendWith(MTEExtension.class)
@Dependencies({"engine", "ModuleTestingEnvironment"})
public class ComponentSystemTest {
    @In
    private EntityManager entityManager;

    @Test
    public void simpleEventTest() {
        EntityRef entity = entityManager.create(new DummyComponent());
        entity.send(new DummyEvent());
        Assertions.assertTrue(entity.getComponent(DummyComponent.class).eventReceived);
    }
}
