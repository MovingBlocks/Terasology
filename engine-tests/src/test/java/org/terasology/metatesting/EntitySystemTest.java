package org.terasology.metatesting;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.event.AbstractConsumableEvent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.Priority;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.event.internal.EventSystemImpl;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EntitySystemTest {

    private EngineEntityManager entityManager;
    private EventSystem eventSystem;
    private Context context;

    @BeforeEach
    public void setUp() {
        context = new ContextImpl();
        CoreRegistry.setContext(context);

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

        // 2. Destroy Entity
        entity.destroy();
        assertFalse(entity.exists());
        assertFalse(entityManager.contains(id));
    }

    @Test
    public void testComponentManagement() {
        EntityRef entity = entityManager.create();
        System.out.println("Starting testComponentManagement with entity: " + entity);

        // 1. Add Component
        TestComponent comp = new TestComponent();
        comp.name = "Test Name";
        entity.addComponent(comp);

        assertTrue(entity.hasComponent(TestComponent.class));
        assertEquals("Test Name", entity.getComponent(TestComponent.class).name);

        // 2. Update Component
        TestComponent comp2 = entity.getComponent(TestComponent.class);
        comp2.name = "Updated Name";
        entity.saveComponent(comp2);

        assertEquals("Updated Name", entity.getComponent(TestComponent.class).name);

        // 3. Remove Component
        entity.removeComponent(TestComponent.class);
        assertFalse(entity.hasComponent(TestComponent.class));
    }

    @Test
    public void testSystemLifecycle() {
        ComponentSystemManager systemManager = new ComponentSystemManager(context);
        ComponentSystem mockSystem = mock(ComponentSystem.class);

        systemManager.register(mockSystem);
        System.out.println("System registered to systemManager: " + systemManager + " with mockSystem: " + mockSystem);

        // Initialise should not be called yet
        verify(mockSystem, never()).initialise();

        systemManager.initialise();
        verify(mockSystem).initialise();

        systemManager.shutdown();
        verify(mockSystem).shutdown();
    }

    @Test
    public void testEventProcessing() {
        // 1. Setup Event System and Entity
        EntityRef entity = entityManager.create();
        TestEvent event = new TestEvent();
        System.out.println("Starting testEventProcessing with entity: " + entity + " and event: " + event);

        // 2. Register Handlers via ComponentSystemManager (simulating real engine flow)
        ComponentSystemManager systemManager = new ComponentSystemManager(context);

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

        ComponentSystemManager systemManager = new ComponentSystemManager(context);

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

    @Test
    public void testLiveEntitySystem() {
        // 1. Setup Managers
        ComponentSystemManager systemManager = new ComponentSystemManager(context);
        System.out.println("Starting testLiveEntitySystem with systemManager: " + systemManager);

        // 2. Register Test System
        TestUpdateSubscriber testSystem = new TestUpdateSubscriber();
        systemManager.register(testSystem);

        // 3. Initialise (triggers injection)
        systemManager.initialise();
        System.out.println("System manager initialised with test UpdateSubscriberSystem: " + testSystem);

        // Verify Injection
        assertNotNull(testSystem.entityManager, "EntityManager should be injected");
        assertNotNull(testSystem.eventSystem, "EventSystem should be injected");

        // 4. Simulate Game Loop (3 ticks)
        for (int i = 0; i < 3; i++) {
            System.out.println("Tick " + (i + 1));
            float delta = 0.1f;

            // Process Events
            eventSystem.process();

            // Update Systems
            for (UpdateSubscriberSystem system : systemManager.iterateUpdateSubscribers()) {
                system.update(delta);
            }
        }

        // 5. Verify Execution
        System.out.println("Number of updates: " + testSystem.updateCount + " and event received: " + testSystem.eventReceived);
        assertEquals(3, testSystem.updateCount, "System should have updated 3 times");
        assertTrue(testSystem.eventReceived, "System should have received event fired during update");
    }

    public static class TestUpdateSubscriber extends BaseComponentSystem
            implements UpdateSubscriberSystem {

        @In
        public EngineEntityManager entityManager;

        @In
        public EventSystem eventSystem;

        public int updateCount = 0;
        public boolean eventReceived = false;

        @Override
        public void update(float delta) {
            updateCount++;
            // Fire an event on the first tick to test interaction
            if (updateCount == 1) {
                EntityRef entity = entityManager.create();
                entity.send(new TestEvent());
            }
        }

        @ReceiveEvent
        public void onEvent(TestEvent event, EntityRef entity) {
            eventReceived = true;
        }
    }

    public static class TestComponent implements Component<TestComponent> {
        public String name;

        @Override
        public void copyFrom(TestComponent other) {
            this.name = other.name;
        }
    }

    public static class TestEvent implements Event {
    }

    public static class TestConsumableEvent extends AbstractConsumableEvent {
    }

    public static class TestEventHandler extends BaseComponentSystem {
        public boolean received = false;

        @ReceiveEvent
        public void onEvent(TestEvent event, EntityRef entity) {
            received = true;
        }

        @ReceiveEvent
        public void onConsumable(TestConsumableEvent event, EntityRef entity) {
            received = true;
        }
    }

    public static class TestEventHandlerHighPriority
            extends BaseComponentSystem {
        public boolean received = false;

        @Priority(EventPriority.PRIORITY_HIGH)
        @ReceiveEvent
        public void onEvent(TestEvent event, EntityRef entity) {
            received = true;
        }
    }

    public static class ConsumingHandler extends BaseComponentSystem {
        public boolean received = false;

        @Priority(EventPriority.PRIORITY_HIGH)
        @ReceiveEvent
        public void onEvent(TestConsumableEvent event, EntityRef entity) {
            received = true;
            event.consume();
        }
    }
}
