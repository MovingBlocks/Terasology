package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibrary;
import org.terasology.entitySystem.metadata.TypeHandlerLibraryBuilder;
import org.terasology.entitySystem.metadata.internal.EntitySystemLibraryImpl;
import org.terasology.entitySystem.pojo.EventSystemImpl;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkSystem;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;


/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEventSystemTests {

    ComponentLibrary compLibrary;
    EventSystemImpl eventSystem;
    PojoEntityManager entityManager;
    EntityRef entity;

    @Before
    public void setup() {
        TypeHandlerLibrary lib = new TypeHandlerLibraryBuilder().build();

        EntitySystemLibrary entitySystemLibrary = new EntitySystemLibraryImpl(lib);
        compLibrary = entitySystemLibrary.getComponentLibrary();
        entityManager = new PojoEntityManager();
        entityManager.setEntitySystemLibrary(entitySystemLibrary);
        entityManager.setPrefabManager(new PojoPrefabManager(compLibrary));
        eventSystem = new EventSystemImpl(entitySystemLibrary.getEventLibrary(), mock(NetworkSystem.class));
        entityManager.setEventSystem(eventSystem);
        entity = entityManager.create();
    }

    @Test
    public void testSendEventToEntity() {
        StringComponent component = entity.addComponent(new StringComponent());

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
        StringComponent stringComponent = entity.addComponent(new StringComponent());
        IntegerComponent intComponent = entity.addComponent(new IntegerComponent());

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
        StringComponent component = entity.addComponent(new StringComponent());
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
        StringComponent component = entity.addComponent(new StringComponent());

        TestCompoundComponentEventHandler handler = new TestCompoundComponentEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        eventSystem.send(entity, event);

        assertEquals(0, handler.receivedList.size());
    }

    @Test
    public void testReceiveEventRequiringMultipleComponents() {
        StringComponent stringComponent = entity.addComponent(new StringComponent());
        IntegerComponent intComponent = entity.addComponent(new IntegerComponent());

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
        StringComponent stringComponent = entity.addComponent(new StringComponent());

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
        eventSystem.registerEvent("test:childEvent", TestChildEvent.class);
        eventSystem.registerEventHandler(handler);

        TestChildEvent event = new TestChildEvent();
        eventSystem.send(entity, event);
        assertEquals(1, handler.childEventReceived.size());
        assertEquals(1, handler.receivedList.size());
    }

    private static class TestEvent extends AbstractEvent {

    }

    public static class TestChildEvent extends TestEvent {

    }

    public static class TestEventHandler implements ComponentSystem {

        List<Received> receivedList = Lists.newArrayList();
        List<Received> childEventReceived = Lists.newArrayList();

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


    public static class TestHighPriorityEventHandler implements ComponentSystem {

        List<Received> receivedList = Lists.newArrayList();
        public boolean cancel = false;

        @ReceiveEvent(components = StringComponent.class, priority = EventPriority.PRIORITY_HIGH)
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
            if (cancel) {
                event.cancel();
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

    public static class TestCompoundComponentEventHandler implements ComponentSystem {

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


}
