// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.event.internal.EventReceiver;
import org.terasology.engine.entitySystem.event.internal.EventSystemImpl;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.unittest.stubs.IntegerComponent;
import org.terasology.unittest.stubs.StringComponent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class PojoEventSystemTests {

    ComponentLibrary compLibrary;
    EventSystemImpl eventSystem;
    PojoEntityManager entityManager;
    EntityRef entity;

    @BeforeEach
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary serializationLibrary = new TypeHandlerLibraryImpl(reflections);

        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibrary(context, serializationLibrary);
        compLibrary = entitySystemLibrary.getComponentLibrary();
        entityManager = new PojoEntityManager();
        entityManager.setComponentLibrary(entitySystemLibrary.getComponentLibrary());
        entityManager.setPrefabManager(new PojoPrefabManager(context));
        eventSystem = new EventSystemImpl(true);
        entityManager.setEventSystem(eventSystem);
        entity = entityManager.create();
    }

    @Test
    public void testSendEventToEntity() {
        entity.addComponent(new StringComponent());

        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        entity.send(event);

        assertEquals(1, handler.receivedList.size());
        assertEquals(event, handler.receivedList.get(0).event);
        assertEquals(entity, handler.receivedList.get(0).entity);

    }

    @Test
    public void testSendEventToEntityWithMultipleComponents() {
        entity.addComponent(new StringComponent());
        entity.addComponent(new IntegerComponent());

        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        entity.send(event);

        assertEquals(2, handler.receivedList.size());
        for (TestEventHandler.Received received : handler.receivedList) {
            assertEquals(event, received.event);
            assertEquals(entity, received.entity);
        }

    }

    @Test
    public void testSendEventToEntityComponent() {
        entity.addComponent(new StringComponent());
        IntegerComponent intComponent = entity.addComponent(new IntegerComponent());

        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        eventSystem.send(entity, event, intComponent);

        assertEquals(1, handler.receivedList.size());
        assertEquals(event, handler.receivedList.get(0).event);
        assertEquals(entity, handler.receivedList.get(0).entity);
    }

    @Test
    public void testNoReceiveEventWhenMissingComponents() {
        entity.addComponent(new StringComponent());

        TestCompoundComponentEventHandler handler = new TestCompoundComponentEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        eventSystem.send(entity, event);

        assertEquals(0, handler.receivedList.size());
    }

    @Test
    public void testReceiveEventRequiringMultipleComponents() {
        entity.addComponent(new StringComponent());
        entity.addComponent(new IntegerComponent());

        TestCompoundComponentEventHandler handler = new TestCompoundComponentEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        eventSystem.send(entity, event);

        assertEquals(1, handler.receivedList.size());
        assertEquals(event, handler.receivedList.get(0).event);
        assertEquals(entity, handler.receivedList.get(0).entity);
    }

    @Test
    public void testPriorityAndCancel() {
        entity.addComponent(new StringComponent());

        TestEventHandler handlerNormal = new TestEventHandler();
        TestHighPriorityEventHandler handlerHigh = new TestHighPriorityEventHandler();
        handlerHigh.cancel = true;
        eventSystem.registerEventHandler(handlerNormal);
        eventSystem.registerEventHandler(handlerHigh);

        TestEvent event = new TestEvent();
        eventSystem.send(entity, event);
        assertEquals(1, handlerHigh.receivedList.size());
        assertEquals(0, handlerNormal.receivedList.size());
    }

    @Test
    public void testChildEvent() {
        entity.addComponent(new IntegerComponent());
        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEvent(new ResourceUrn("test:childEvent"), TestChildEvent.class);
        eventSystem.registerEventHandler(handler);

        TestChildEvent event = new TestChildEvent();
        eventSystem.send(entity, event);
        assertEquals(1, handler.childEventReceived.size());
        assertEquals(1, handler.receivedList.size());
    }

    @Test
    public void testChildEventReceivedByUnfilteredHandler() {
        entity.addComponent(new IntegerComponent());
        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEvent(new ResourceUrn("test:childEvent"), TestChildEvent.class);
        eventSystem.registerEventHandler(handler);

        TestChildEvent event = new TestChildEvent();
        eventSystem.send(entity, event);
        assertEquals(1, handler.unfilteredEvents.size());
    }

    @Test
    public void testEventReceiverRegistration() {
        TestEventReceiver receiver = new TestEventReceiver();
        eventSystem.registerEventReceiver(receiver, TestEvent.class);

        entity.send(new TestEvent());
        assertEquals(1, receiver.eventList.size());

        eventSystem.unregisterEventReceiver(receiver, TestEvent.class);
        entity.send(new TestEvent());
        assertEquals(1, receiver.eventList.size());
    }

    public static class TestEvent extends AbstractConsumableEvent {

    }

    public static class TestChildEvent extends TestEvent {

    }

    public static class TestEventHandler extends BaseComponentSystem {

        List<Received> receivedList = Lists.newArrayList();
        List<Received> childEventReceived = Lists.newArrayList();
        List<Received> unfilteredEvents = Lists.newArrayList();

        @ReceiveEvent(components = StringComponent.class)
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        @ReceiveEvent(components = IntegerComponent.class)
        public void handleIntegerEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        @ReceiveEvent(components = IntegerComponent.class)
        public void handleChildEvent(TestChildEvent event, EntityRef entity) {
            childEventReceived.add(new Received(event, entity));
        }

        @ReceiveEvent
        public void handleUnfilteredTestEvent(TestEvent event, EntityRef entity) {
            unfilteredEvents.add(new Received(event, entity));
        }

        public void initialise() {

        }

        @Override
        public void shutdown() {
        }

        public static class Received {
            TestEvent event;
            EntityRef entity;

            public Received(TestEvent event, EntityRef entity) {
                this.event = event;
                this.entity = entity;
            }
        }
    }


    public static class TestHighPriorityEventHandler extends BaseComponentSystem {

        public boolean cancel;

        List<Received> receivedList = Lists.newArrayList();

        @Priority(EventPriority.PRIORITY_HIGH)
        @ReceiveEvent(components = StringComponent.class)
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
            if (cancel) {
                event.consume();
            }
        }

        @Priority(EventPriority.PRIORITY_HIGH)
        @ReceiveEvent(components = IntegerComponent.class)
        public void handleIntegerEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        public void initialise() {

        }

        @Override
        public void shutdown() {

        }

        public static class Received {
            TestEvent event;
            EntityRef entity;

            public Received(TestEvent event, EntityRef entity) {
                this.event = event;
                this.entity = entity;
            }
        }
    }

    public static class TestCompoundComponentEventHandler extends BaseComponentSystem {

        List<Received> receivedList = Lists.newArrayList();

        @ReceiveEvent(components = {StringComponent.class, IntegerComponent.class})
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        public void initialise() {

        }

        @Override
        public void shutdown() {
        }

        public static class Received {
            TestEvent event;
            EntityRef entity;

            public Received(TestEvent event, EntityRef entity) {
                this.event = event;
                this.entity = entity;
            }
        }
    }

    public static class TestEventReceiver implements EventReceiver<TestEvent> {
        List<Event> eventList = Lists.newArrayList();

        @Override
        public void onEvent(TestEvent event, EntityRef entity) {
            eventList.add(event);
        }
    }

}
