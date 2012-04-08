package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoEventSystem;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;

import java.util.List;

import static org.junit.Assert.*;


/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEventSystemTests {

    PojoEventSystem eventSystem;
    PojoEntityManager entityManager;
    EntityRef entity;
    
    @Before
    public void setup() {

        entityManager = new PojoEntityManager();
        eventSystem = new PojoEventSystem(entityManager);
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
    
    private static class TestEvent implements Event {
        
    }
    
    public static class TestEventHandler implements EventHandlerSystem {

        List<Received> receivedList = Lists.newArrayList();        
        
        @ReceiveEvent(components = StringComponent.class)
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        @ReceiveEvent(components = IntegerComponent.class)
        public void handleIntegerEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        public void initialise() {

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

    public static class TestCompoundComponentEventHandler implements EventHandlerSystem {

        List<Received> receivedList = Lists.newArrayList();

        @ReceiveEvent(components = {StringComponent.class, IntegerComponent.class})
        public void handleStringEvent(TestEvent event, EntityRef entity) {
            receivedList.add(new Received(event, entity));
        }

        public void initialise() {

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
