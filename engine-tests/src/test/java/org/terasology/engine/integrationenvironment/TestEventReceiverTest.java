// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.integrationenvironment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.registry.In;
import org.terasology.unittest.stubs.DummyComponent;
import org.terasology.unittest.stubs.DummyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

@IntegrationEnvironment
public class TestEventReceiverTest {

    @In
    private ModuleTestingHelper helper;

    @In
    private EntityManager entityManager;

    @Test
    public void componentFilterTest() {
        EntityRef entityWithDummy = entityManager.create(new DummyComponent());
        EntityRef entityWithDummyAndLocation = entityManager.create(new DummyComponent(), new LocationComponent());

        // AtomicInteger is used because Java complained when just using a primitive int
        AtomicInteger callbackInvocations = new AtomicInteger();
        BiConsumer<DummyEvent, EntityRef> callback = (event, entity) -> callbackInvocations.addAndGet(1);

        try (TestEventReceiver<DummyEvent> receiver =
                     new TestEventReceiver<>(getHostContext(), DummyEvent.class, callback, DummyComponent.class, LocationComponent.class)) {
            entityWithDummy.send(new DummyEvent());
            entityWithDummyAndLocation.send(new DummyEvent());
            List<EntityRef> actualEntities = receiver.getEntityRefs();

            // Only the entity with both Dummy and Location should get hit
            Assertions.assertEquals(1, callbackInvocations.get());
            Assertions.assertTrue(actualEntities.contains(entityWithDummyAndLocation));
            Assertions.assertFalse(actualEntities.contains(entityWithDummy));
        }

        entityWithDummy.destroy();
        entityWithDummyAndLocation.destroy();
    }

    @Test
    public void repeatedEventTest() {
        final List<EntityRef> expectedEntities = new ArrayList<>();
        try (TestEventReceiver<DummyEvent> receiver = new TestEventReceiver<>(getHostContext(), DummyEvent.class)) {
            List<EntityRef> actualEntities = receiver.getEntityRefs();
            Assertions.assertTrue(actualEntities.isEmpty());
            for (int i = 0; i < 5; i++) {
                expectedEntities.add(sendEvent());
                Assertions.assertEquals(i + 1, actualEntities.size());
                Assertions.assertEquals(expectedEntities.get(i), actualEntities.get(i));
            }
        }
    }

    @Test
    public void properClosureTest() {
        final List<EntityRef> entities;
        try (TestEventReceiver<DummyEvent> receiver = new TestEventReceiver<>(getHostContext(), DummyEvent.class)) {
            entities = receiver.getEntityRefs();
        }
        sendEvent();
        Assertions.assertTrue(entities.isEmpty());
    }

    @Test
    public void userCallbackTest() {
        final List<DummyEvent> events = new ArrayList<>();

        TestEventReceiver<DummyEvent> receiver = new TestEventReceiver<>(getHostContext(), DummyEvent.class, (event, entity) -> {
            events.add(event);
        });

        for (int i = 0; i < 3; i++) {
            sendEvent();
        }

        // ensure all interesting events were caught
        Assertions.assertEquals(3, events.size());

        // shouldn't receive events after closing
        receiver.close();
        sendEvent();
        Assertions.assertEquals(3, events.size());
    }

    /**
     * Drops a generic item into the world.
     *
     * @return the item
     */
    private EntityRef sendEvent() {
        final EntityRef entityRef = getHostContext().get(EntityManager.class)
                .create(new DummyComponent());
        entityRef.send(new DummyEvent());
        return entityRef;
    }

    private Context getHostContext() {
        return helper.getHostContext();
    }
}
