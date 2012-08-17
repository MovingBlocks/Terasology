package org.terasology.entitySystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.ChangedComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.pojo.PojoEntityManager;
import org.terasology.entitySystem.pojo.PojoPrefabManager;
import org.terasology.entitySystem.stubs.EntityRefComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.bootstrap.EntitySystemBuilder;

import com.google.common.collect.Lists;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManagerTest {

    PersistableEntityManager entityManager;
    
    @Before
    public void setup() {
        EntitySystemBuilder builder = new EntitySystemBuilder();

        entityManager = builder.build();
    }
    
    @Test
    public void createEntity() {
        EntityRef entity = entityManager.create();
        assertNotNull(entity);
    }

    @Test
    public void createEntityWithComponent() {
        StringComponent comp = new StringComponent("Test");
        EntityRef entity = entityManager.create(comp);
        assertNotNull(entity);
        assertNotNull(entity.getComponent(StringComponent.class));
        assertEquals(comp, entity.getComponent(StringComponent.class));
    }
    
    @Test
    public void addAndRetrieveComponent() {
        EntityRef entity = entityManager.create();
        assertNotNull(entity);

        StringComponent comp = new StringComponent();
        entity.addComponent(comp);
        
        assertEquals(comp, entity.getComponent(StringComponent.class));
    }
    
    @Test
    public void removeComponent() {
        EntityRef entity = entityManager.create();

        StringComponent comp = new StringComponent();
        entity.addComponent(comp);
        entity.removeComponent(StringComponent.class);

        assertNull(entity.getComponent(StringComponent.class));
    }
    
    @Test
    public void replaceComponent() {
        EntityRef entity = entityManager.create();
        
        StringComponent comp = new StringComponent();
        comp.value = "Hello";
        StringComponent comp2 = new StringComponent();
        comp2.value = "Goodbye";
        
        entity.addComponent(comp);
        entity.addComponent(comp2);

        assertEquals(comp2, entity.getComponent(StringComponent.class));
    }

    @Test
    public void destroyEntity() {
        EntityRef entity = entityManager.create();
        
        entity.addComponent(new StringComponent());
        entity.addComponent(new IntegerComponent());
        entity.destroy();

        assertNull(entity.getComponent(StringComponent.class));
        assertNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void iterateComponents() {
        EntityRef entity = entityManager.create();
        StringComponent comp = new StringComponent();
        entity.addComponent(comp);

        for (Map.Entry<EntityRef, StringComponent> item : entityManager.iterateComponents(StringComponent.class)) {
            assertEquals(entity, item.getKey());
            assertEquals(comp, item.getValue());
        }
    }
    
    @Test
    public void changeComponentsDuringIterator() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        EntityRef entity2 = entityManager.create();
        entity2.addComponent(new StringComponent());
        
        Iterator<Map.Entry<EntityRef, StringComponent>> iterator = entityManager.iterateComponents(StringComponent.class).iterator();
        iterator.next();

        entity2.removeComponent(StringComponent.class);
        iterator.next();
    }

    @Test
    public void addComponentEventSent() {
        EventSystem eventSystem = mock(EventSystem.class);
        entityManager.setEventSystem(eventSystem);
        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());

        verify(eventSystem).send(entity1, AddComponentEvent.newInstance(), comp);
    }

    @Test
    public void removeComponentEventSent() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.removeComponent(StringComponent.class);

        verify(eventSystem).send(entity1, RemovedComponentEvent.newInstance(), comp);
    }

    @Test
    public void changeComponentEventSentWhenSave() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.saveComponent(comp);

        verify(eventSystem).send(entity1, ChangedComponentEvent.newInstance(), comp);
    }

    @Test
    public void changeComponentEventSentWhenAddOverExisting() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp1 = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        StringComponent comp2 = entity1.addComponent(new StringComponent());

        verify(eventSystem).send(entity1, ChangedComponentEvent.newInstance(), comp2);
    }
    
    @Test
    public void massRemovedComponentEventSentOnDestroy() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp1 = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.destroy();
        
        verify(eventSystem).send(entity1, RemovedComponentEvent.newInstance());
    }
    
    @Test
    public void iterateEntitiesFindsEntityWithSingleComponent() {
        EntityRef entity1 = entityManager.create();
        StringComponent comp1 = entity1.addComponent(new StringComponent());
        
        List<EntityRef> results = Lists.newArrayList(entityManager.iteratorEntities(StringComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void iterateEntitiesDoesNotFindEntityMissingAComponent() {
        EntityRef entity1 = entityManager.create();
        StringComponent comp1 = entity1.addComponent(new StringComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.iteratorEntities(StringComponent.class, IntegerComponent.class));
        assertEquals(Lists.newArrayList(), results);
    }

    @Test
    public void iterateEntitiesFindsEntityWithTwoComponents() {
        EntityRef entity1 = entityManager.create();
        StringComponent comp1 = entity1.addComponent(new StringComponent());
        IntegerComponent comp2 = entity1.addComponent(new IntegerComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.iteratorEntities(StringComponent.class, IntegerComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void iterateWithNoComponents() {
        List<EntityRef> results = Lists.newArrayList(entityManager.iteratorEntities(StringComponent.class));
        assertEquals(Lists.newArrayList(), results);
    }
    
    @Test
    public void getComponentCountWhenNoComponents() {
        assertEquals(0, entityManager.getComponentCount(StringComponent.class));
    }
    
    @Test
    public void getComponentCount() {
        entityManager.create().addComponent(new StringComponent());
        entityManager.create().addComponent(new StringComponent());
        assertEquals(2, entityManager.getComponentCount(StringComponent.class));
    }

    @Test
    public void destroyingEntityInvalidatesEntityRefs() {
        EntityRef main = entityManager.create();
        main.addComponent(new StringComponent());

        EntityRef reference = entityManager.create();
        EntityRefComponent refComp = reference.addComponent(new EntityRefComponent());
        refComp.entityRef = entityManager.iteratorEntities(StringComponent.class).iterator().next();

        assertTrue(main.exists());
        entityManager.iteratorEntities(StringComponent.class).iterator().next().destroy();

        assertFalse(main.exists());
        assertFalse(refComp.entityRef.exists());

    }

}
