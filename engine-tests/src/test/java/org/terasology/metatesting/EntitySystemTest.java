package org.terasology.metatesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.event.internal.EventSystemImpl;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.gestalt.entitysystem.component.Component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class EntitySystemTest {

    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private Context context;

    @BeforeEach
    public void setUp() {
        context = new ContextImpl();
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        context.put(ModuleManager.class, mock(ModuleManager.class));

        eventSystem = new EventSystemImpl(networkSystem);
        context.put(EventSystem.class, eventSystem);

        entityManager = new PojoEntityManager();
        entityManager.setEventSystem(eventSystem);
        context.put(EngineEntityManager.class, entityManager);
        context.put(org.terasology.engine.entitySystem.entity.EntityManager.class, entityManager);
    }

    @Test
    public void testEntityLifecycle() {
        
        // 1. Create Entity
        EntityRef entity = entityManager.create();
        System.out.println("Starting testEntityLifecycle with entity: " + entity);
        assertNotNull(entity);
        assertTrue(entity.exists());
        long id = entity.getId();
        System.out.println("Entity created with ID: " + id);

        // 2. Destroy Entity
        entity.destroy();
        assertFalse(entity.exists());
        assertFalse(entityManager.contains(id));
        System.out.println("Entity destroyed.");
    }

    @Test
    public void testComponentManagement() {
        EntityRef entity = entityManager.create();
        System.out.println("Starting testComponentManagement with entity: " + entity);

        // 1. Add Component
        TestComponent comp = new TestComponent();
        comp.name = "Test Name";
        entity.addComponent(comp);
        System.out.println("Component added: " + comp.name);

        assertTrue(entity.hasComponent(TestComponent.class));
        assertEquals("Test Name", entity.getComponent(TestComponent.class).name);

        // 2. Update Component
        TestComponent comp2 = entity.getComponent(TestComponent.class);
        comp2.name = "Updated Name";
        entity.saveComponent(comp2);
        System.out.println("Component updated: " + comp2.name);

        assertEquals("Updated Name", entity.getComponent(TestComponent.class).name);

        // 3. Remove Component
        entity.removeComponent(TestComponent.class);
        assertFalse(entity.hasComponent(TestComponent.class));
        System.out.println("Component removed.");
    }

    @Test
    public void testSystemLifecycle() {
        org.terasology.engine.core.ComponentSystemManager systemManager = new org.terasology.engine.core.ComponentSystemManager(context);
        org.terasology.engine.entitySystem.systems.ComponentSystem mockSystem = mock(org.terasology.engine.entitySystem.systems.ComponentSystem.class);

        systemManager.register(mockSystem);
        System.out.println("System registered to systemManager: " + systemManager + " with mockSystem: " + mockSystem);

        // Initialise should not be called yet
        verify(mockSystem, never()).initialise();

        systemManager.initialise();
        System.out.println("System manager initialised.");
        verify(mockSystem).initialise();

        systemManager.shutdown();
        System.out.println("System manager shutdown.");
        verify(mockSystem).shutdown();
    }

    @Test
    public void testEventProcessing() {
        // 1. Setup Event System and Entity
        EntityRef entity = entityManager.create();
        TestEvent event = new TestEvent();
        System.out.println("Starting testEventProcessing with entity: " + entity + " and event: " + event);

        // 2. Register Handlers via ComponentSystemManager (simulating real engine flow)
        org.terasology.engine.core.ComponentSystemManager systemManager = new org.terasology.engine.core.ComponentSystemManager(context);

        TestEventHandler handlerNormal = new TestEventHandler();
        TestEventHandlerHighPriority handlerHigh = new TestEventHandlerHighPriority();

        systemManager.register(handlerNormal);
        systemManager.register(handlerHigh);
        systemManager.initialise();
        System.out.println("Handlers registered and manager initialised to systemManager: " + systemManager);

        // 3. Fire Event
        System.out.println("Sending event: " + event);
        entity.send(event);

        // 4. Verify Priority (High should run first)
        assertTrue(handlerHigh.received, "High priority handler should have received event");
        assertTrue(handlerNormal.received, "Normal priority handler should have received event");

        // In a real scenario we'd verify order, but basic receipt is a good start.
        // Since we didn't consume it, both should get it.
    }

    @Test
    public void testEventConsumption() {
        EntityRef entity = entityManager.create();
        TestConsumableEvent event = new TestConsumableEvent();
        System.out.println("Starting testEventConsumption with entity: " + entity + " and event: " + event);

        org.terasology.engine.core.ComponentSystemManager systemManager = new org.terasology.engine.core.ComponentSystemManager(context);

        ConsumingHandler consumingHandler = new ConsumingHandler();
        TestEventHandler normalHandler = new TestEventHandler();

        systemManager.register(consumingHandler); // High priority, consumes
        systemManager.register(normalHandler); // Normal priority
        systemManager.initialise();
        System.out.println("Handlers registered (Consuming & Normal) to systemManager: " + systemManager);

        System.out.println("Sending consumable event: " + event);
        entity.send(event);

        assertTrue(consumingHandler.received, "Consuming handler should receive event");
        assertFalse(normalHandler.received, "Normal handler should NOT receive event (consumed)");
        System.out.println("Event should have been consumed");
    }

    public static class TestComponent implements Component<TestComponent> {
        public String name;

        @Override
        public void copyFrom(TestComponent other) {
            this.name = other.name;
        }
    }

    public static class TestEvent implements org.terasology.gestalt.entitysystem.event.Event {
    }

    public static class TestConsumableEvent extends org.terasology.engine.entitySystem.event.AbstractConsumableEvent {
    }

    public static class TestEventHandler extends org.terasology.engine.entitySystem.systems.BaseComponentSystem {
        public boolean received = false;

        @org.terasology.gestalt.entitysystem.event.ReceiveEvent
        public void onEvent(TestEvent event, EntityRef entity) {
            received = true;
        }

        @org.terasology.gestalt.entitysystem.event.ReceiveEvent
        public void onConsumable(TestConsumableEvent event, EntityRef entity) {
            received = true;
        }
    }

    public static class TestEventHandlerHighPriority
            extends org.terasology.engine.entitySystem.systems.BaseComponentSystem {
        public boolean received = false;

        @org.terasology.engine.entitySystem.event.Priority(org.terasology.engine.entitySystem.event.EventPriority.PRIORITY_HIGH)
        @org.terasology.gestalt.entitysystem.event.ReceiveEvent
        public void onEvent(TestEvent event, EntityRef entity) {
            received = true;
        }
    }

    public static class ConsumingHandler extends org.terasology.engine.entitySystem.systems.BaseComponentSystem {
        public boolean received = false;

        @org.terasology.engine.entitySystem.event.Priority(org.terasology.engine.entitySystem.event.EventPriority.PRIORITY_HIGH)
        @org.terasology.gestalt.entitysystem.event.ReceiveEvent
        public void onEvent(TestConsumableEvent event, EntityRef entity) {
            received = true;
            event.consume();
        }
    }
}
