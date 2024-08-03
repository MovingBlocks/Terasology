// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.DummyComponent;
import org.terasology.unittest.stubs.DummyEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Ensure a test class with a per-class Jupiter lifecycle can share an engine between tests.
 */
@IntegrationEnvironment
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // Lifecycle of the test instance
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MTEExtensionTestWithPerClassLifecycle {
    @In
    public EntityManager entityManager;

    // java 8 doesn't have ConcurrentSet
    private final ConcurrentMap<String, Integer> seenNames = new ConcurrentHashMap<>();

    @BeforeAll
    public void createEntity(TestInfo testInfo) {
        // Create some entity to be shared by all the tests.
        EntityRef entity = entityManager.create(new DummyComponent());

        // Do some stuff to configure it.
        entity.send(new DummyEvent());

        entity.updateComponent(DummyComponent.class, component -> {
            // Mark with something unique (and not reliant on the entity id system)
            component.name = testInfo.getDisplayName() + "#" + UUID.randomUUID();
            return component;
        });
    }

    @Test
    @Order(1)
    public void firstTestFindsThings() {
        List<EntityRef> entities = Lists.newArrayList(entityManager.getEntitiesWith(DummyComponent.class));
        // There should be one entity, created by the @BeforeAll method
        assertEquals(1, entities.size());

        DummyComponent component = entities.get(0).getComponent(DummyComponent.class);
        assertTrue(component.eventReceived);

        // Remember that a test has seen this one.
        assertNotNull(component.name);
        assertFalse(seenNames.containsKey(component.name));
        seenNames.put(component.name, 1);
    }

    @Test
    @Order(2)
    public void thingsStillExistForSecondTest() {
        List<EntityRef> entities = Lists.newArrayList(entityManager.getEntitiesWith(DummyComponent.class));
        // There should be one entity, created by the @BeforeAll method
        assertEquals(1, entities.size());

        // Make sure that this is the same one that the first test saw.
        DummyComponent component = entities.get(0).getComponent(DummyComponent.class);
        assertTrue(component.eventReceived);
        assertNotNull(component.name);
        assertTrue(seenNames.containsKey(component.name), () ->
                String.format("This is not the same entity as seen in the first test!%n"
                        + "Current entity: %s%n"
                        + "Previously seen: %s",
                        component.name, seenNames.keySet()));
    }
}
