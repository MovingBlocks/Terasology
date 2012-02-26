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
        entity = entityManager.createEntity();
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
        assertEquals(component, handler.receivedList.get(0).component);
        
    }

    @Test
    public void testSendEventToEntityWithMultipleComponents() {
        StringComponent stringComponent = entity.addComponent(new StringComponent());
        IntegerComponent intComponent = entity.addComponent(new IntegerComponent());

        TestEventHandler handler = new TestEventHandler();
        eventSystem.registerEventHandler(handler);

        TestEvent event = new TestEvent();
        entity.send(event);

        List<Component> expectedComponents = Lists.newArrayList(stringComponent, intComponent);
        assertEquals(2, handler.receivedList.size());
        for (TestEventHandler.Received received : handler.receivedList) {
            assertEquals(event, received.event);
            assertEquals(entity, received.entity);
            assertTrue(expectedComponents.remove(received.component));
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
        assertEquals(intComponent, handler.receivedList.get(0).component);
    }
    
    private static class TestEvent implements Event {
        
    }
    
    public static class TestEventHandler implements EventHandler {

        List<Received> receivedList = Lists.newArrayList();        
        
        @ReceiveEvent
        public void handleTestEvent(TestEvent event, EntityRef entity, StringComponent component) {
            receivedList.add(new Received(event, entity, component));
        }

        @ReceiveEvent
        public void handleTestEvent(TestEvent event, EntityRef entity, IntegerComponent component) {
            receivedList.add(new Received(event, entity, component));
        }
        
        public static class Received {
            TestEvent event;
            EntityRef entity;
            Component component;
            
            public Received(TestEvent event, EntityRef entity, Component component) {
                this.event = event;
                this.entity = entity;
                this.component = component;
            }
        }
    }
}
