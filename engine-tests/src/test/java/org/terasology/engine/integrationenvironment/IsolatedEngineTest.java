// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.jupiter.IsolatedMTEExtension;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.DummyComponent;
import org.terasology.unittest.stubs.DummyEvent;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("MteTest")
@ExtendWith(IsolatedMTEExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IsolatedEngineTest {
    private final Set<EntityManager> entityManagerSet = Sets.newHashSet();
    private EntityRef entity;

    @In
    private EntityManager entityManager;

    @BeforeEach
    public void prepareEntityForTest() {
        entity = entityManager.create(new DummyComponent());
    }

    @Test
    @Order(1)
    public void someTest() {
        // make sure we don't reuse the EntityManager
        assertFalse(entityManagerSet.contains(entityManager));
        entityManagerSet.add(entityManager);

        entity.send(new DummyEvent());
        assertTrue(entity.getComponent(DummyComponent.class).eventReceived);
    }

    @Test
    @Order(2)
    public void someOtherTest() {
        // make sure we don't reuse the EntityManager
        assertFalse(entityManagerSet.contains(entityManager));

        assertFalse(entity.getComponent(DummyComponent.class).eventReceived,
                "This entity should not have its field set yet!");
    }
}
