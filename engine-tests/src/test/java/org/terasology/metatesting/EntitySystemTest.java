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
    }

    @Test
    public void testEntityLifecycle() {
        // 1. Create Entity
        EntityRef entity = entityManager.create();
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

    public static class TestComponent implements Component<TestComponent> {
        public String name;

        @Override
        public void copyFrom(TestComponent other) {
            this.name = other.name;
        }
    }
}
