// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.event.EventPriority;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.event.internal.EventReceiver;
import org.terasology.entitySystem.event.internal.EventSystemImpl;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.prefab.internal.PojoPrefabManager;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.recording.EventCatcher;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.CoreRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 */
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
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        EventCatcher eventCatcher = new EventCatcher(null, null);
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = new RecordAndReplayCurrentStatus();
        eventSystem = new EventSystemImpl(entitySystemLibrary.getEventLibrary(), networkSystem, eventCatcher, recordAndReplayCurrentStatus);
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
        eventSystem.registerEvent(new SimpleUri("test:childEvent"), TestChildEvent.class);
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
        eventSystem.registerEvent(new SimpleUri("test:childEvent"), TestChildEvent.class);
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

    private static class TestEvent extends AbstractConsumableEvent {

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

        @ReceiveEvent(components = StringComponent.class, priority = EventPriority.PRIORITY_HIGH)
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
            if (cancel) {
                event.consume();
            }
        }

        @ReceiveEvent(components = IntegerComponent.class, priority = EventPriority.PRIORITY_HIGH)
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
