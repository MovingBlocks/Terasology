// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.integrationenvironment;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Ensure a test class with a per-method Jupiter lifecycle does not share data between tests.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)  // The default, but here for explicitness.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@IntegrationEnvironment
public class MTEExtensionTestWithPerMethodLifecycle {

    @In
    public EntityManager entityManager;

    private final ConcurrentHashMap.KeySetView<Object, Boolean> seenNames = ConcurrentHashMap.newKeySet();

    @BeforeEach
    void createEntity(TestInfo testInfo) {
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
        // There should be one entity, created by the @BeforeEach method
        assertEquals(1, entities.size());

        DummyComponent component = entities.get(0).getComponent(DummyComponent.class);
        assertThat(component.eventReceived).isTrue();
        assertThat(seenNames).isEmpty();

        // Remember that a test has seen this one.
        assertThat(component.name).isNotNull();
        assertThat(seenNames).isEmpty();
        seenNames.add(component.name);
    }

    @Test
    @Order(2)
    public void thingsDoNotPolluteSecondTest() {
        List<EntityRef> entities = Lists.newArrayList(entityManager.getEntitiesWith(DummyComponent.class));
        // There should be one entity, created by the @BeforeEach method for this one test
        assertEquals(1, entities.size());

        // FIXME: what to assert here does this make sense or should we drop the whole thing
        //     and say it's covered by LifecyclePerMethodInjectionTest?
        DummyComponent component = entities.get(0).getComponent(DummyComponent.class);
        assertThat(component.eventReceived).isTrue();
        assertThat(component.name).isNotNull();
        assertThat(seenNames).isEmpty();
    }
}
