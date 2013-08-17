/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.entitySystem;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.utilities.collection.NullIterator;
import org.terasology.entitySystem.event.EventSystem;
import org.terasology.entitySystem.internal.PojoEntityManager;
import org.terasology.entitySystem.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.entitySystem.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.lifecycleEvents.OnChangedComponent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.stubs.EntityRefComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.engine.module.ModuleManager;
import org.terasology.network.NetworkSystem;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PojoEntityManagerTest {
    private static ModuleManager moduleManager;

    private PojoEntityManager entityManager;
    private Prefab prefab;

    @BeforeClass
    public static void setupClass() {
        moduleManager = new ModuleManager();
    }

    @Before
    public void setup() {
        EntitySystemBuilder builder = new EntitySystemBuilder();

        entityManager = (PojoEntityManager) builder.build(moduleManager, mock(NetworkSystem.class));

        PrefabManager prefabManager = entityManager.getPrefabManager();
        PrefabData protoPrefab = new PrefabData();
        protoPrefab.addComponent(new StringComponent("Test"));
        prefab = Assets.generateAsset(new AssetUri(AssetType.PREFAB, "unittest:myprefab"), protoPrefab, Prefab.class);
        prefabManager.registerPrefab(prefab);
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

        for (Map.Entry<EntityRef, StringComponent> item : entityManager.listComponents(StringComponent.class)) {
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

        Iterator<Map.Entry<EntityRef, StringComponent>> iterator = entityManager.listComponents(StringComponent.class).iterator();
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

        verify(eventSystem).send(entity1, OnAddedComponent.newInstance(), comp);
        verify(eventSystem).send(entity1, OnActivatedComponent.newInstance(), comp);
    }

    @Test
    public void removeComponentEventSent() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.removeComponent(StringComponent.class);

        verify(eventSystem).send(entity1, BeforeDeactivateComponent.newInstance(), comp);
        verify(eventSystem).send(entity1, BeforeRemoveComponent.newInstance(), comp);
    }

    @Test
    public void changeComponentEventSentWhenSave() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.saveComponent(comp);

        verify(eventSystem).send(entity1, OnChangedComponent.newInstance(), comp);
    }

    @Test
    public void changeComponentEventSentWhenAddOverExisting() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        StringComponent comp2 = entity1.addComponent(new StringComponent());

        verify(eventSystem).send(entity1, OnChangedComponent.newInstance(), comp2);
    }

    @Test
    public void massRemovedComponentEventSentOnDestroy() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.destroy();

        verify(eventSystem).send(entity1, BeforeDeactivateComponent.newInstance());
        verify(eventSystem).send(entity1, BeforeRemoveComponent.newInstance());
    }

    @Test
    public void iterateEntitiesFindsEntityWithSingleComponent() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void iterateEntitiesDoesNotFindEntityMissingAComponent() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class, IntegerComponent.class));
        assertEquals(Collections.<EntityRef>emptyList(), results);
    }

    @Test
    public void iterateEntitiesFindsEntityWithTwoComponents() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entity1.addComponent(new IntegerComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class, IntegerComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void iterateWithNoComponents() {
        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class));
        assertEquals(Collections.<EntityRef>emptyList(), results);
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
        refComp.entityRef = entityManager.getEntitiesWith(StringComponent.class).iterator().next();

        assertTrue(main.exists());
        entityManager.getEntitiesWith(StringComponent.class).iterator().next().destroy();

        assertFalse(main.exists());
        assertFalse(refComp.entityRef.exists());

    }

    @Test
    public void prefabCopiedCorrectly() {
        EntityRef entity1 = entityManager.create(prefab);
        StringComponent comp = entity1.getComponent(StringComponent.class);
        assertEquals("Test", comp.value);
        comp.value = "One";
        entity1.saveComponent(comp);
        assertEquals("Test", prefab.getComponent(StringComponent.class).value);
        EntityRef entity2 = entityManager.create(prefab);
        assertEquals("Test", prefab.getComponent(StringComponent.class).value);
        assertEquals("One", entity1.getComponent(StringComponent.class).value);
        assertEquals("Test", entity2.getComponent(StringComponent.class).value);
    }

    @Test
    public void prefabCopiedCorrectly2() {
        EntityRef test1 = entityManager.create("unittest:myprefab");
        EntityRef test2 = entityManager.create("unittest:myprefab");
        //This returns true because the Objectids are Identical.
        assertFalse(test1.getComponent(StringComponent.class) == (test2.getComponent(StringComponent.class)));
    }

    @Test
    public void prefabPersistedRetainedCorrectly() {
        PrefabData protoPrefab = new PrefabData();
        protoPrefab.setPersisted(false);
        prefab = Assets.generateAsset(new AssetUri(AssetType.PREFAB, "unittest:nonpersistentPrefab"), protoPrefab, Prefab.class);
        entityManager.getPrefabManager().registerPrefab(prefab);

        EntityRef entity1 = entityManager.create(prefab);
        assertFalse(entity1.isPersistent());
    }

    @Test
    public void isLoadedTrueOnCreate() {
        EntityRef entity = entityManager.create();
        assertTrue(entity.isActive());
    }

    @Test
    public void isLoadedFalseAfterDestroyed() {
        EntityRef entity = entityManager.create();
        entity.destroy();
        assertFalse(entity.isActive());
    }

    @Test
    public void isLoadedFalseAfterPersist() {
        EntityRef entity = entityManager.create();
        entityManager.deactivateForStorage(entity);
        assertFalse(entity.isActive());
    }

    @Test
    public void isLoadedTrueAfterRestore() {
        EntityRef entity = entityManager.createEntityWithId(2, NullIterator.<Component>newInstance());
        assertTrue(entity.isActive());
    }

    @Test
    public void loadedStateClearedWhenEntityManagerCleared() {
        EntityRef entity = entityManager.create();
        entityManager.clear();
        assertFalse(entity.isActive());
    }

    @Test
    public void destructionOfUnloadedEntitiesPrevented() {
        EntityRef entity = entityManager.create();
        int id = entity.getId();
        entityManager.deactivateForStorage(entity);
        assertTrue(entity.exists());
        entity.destroy();
        assertTrue(entity.exists());
        assertFalse(entityManager.getFreedIds().contains(id));
    }
}
