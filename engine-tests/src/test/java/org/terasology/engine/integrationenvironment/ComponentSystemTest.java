// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.DummyComponent;
import org.terasology.unittest.stubs.DummyEvent;

@IntegrationEnvironment
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
