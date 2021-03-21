// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.entitySystem;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityPool;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeRemoveComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnChangedComponent;
import org.terasology.engine.entitySystem.event.internal.EventSystem;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.entitySystem.stubs.EntityRefComponent;
import org.terasology.engine.entitySystem.stubs.IntegerComponent;
import org.terasology.engine.entitySystem.stubs.StringComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.CHUNK;

/**
 */
public class PojoEntityManagerTest {

    private static Context context;
    private PojoEntityManager entityManager;
    private Prefab prefab;

    @BeforeAll
    public static void setupClass() throws Exception {
        context = new ContextImpl();
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        CoreRegistry.setContext(context);
    }

    @BeforeEach
    public void setup() {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = (PojoEntityManager) context.get(EntityManager.class);

        PrefabData protoPrefab = new PrefabData();
        protoPrefab.addComponent(new StringComponent("Test"));
        prefab = Assets.generateAsset(new ResourceUrn("unittest:myprefab"), protoPrefab, Prefab.class);
    }

    @Test
    public void testCreateEntity() {
        EntityRef entity = entityManager.create();
        assertNotNull(entity);
        assertEquals(CHUNK, entity.getScope());
        assertTrue(entityManager.getGlobalPool().contains(entity.getId()));
        assertFalse(entityManager.getSectorManager().contains(entity.getId()));
    }

    @Test
    public void testCreateEntityWithComponent() {
        StringComponent comp = new StringComponent("Test");
        EntityRef entity = entityManager.create(comp);
        assertNotNull(entity);
        assertNotNull(entity.getComponent(StringComponent.class));
        assertEquals(comp, entity.getComponent(StringComponent.class));
        assertEquals(CHUNK, entity.getScope());
        assertTrue(entityManager.getGlobalPool().contains(entity.getId()));
        assertFalse(entityManager.getSectorManager().contains(entity.getId()));
    }

    @Test
    public void testAddAndRetrieveComponent() {
        EntityRef entity = entityManager.create();
        assertNotNull(entity);

        StringComponent comp = new StringComponent();
        entity.addComponent(comp);

        assertEquals(comp, entity.getComponent(StringComponent.class));
    }

    @Test
    public void testRemoveComponent() {
        EntityRef entity = entityManager.create();

        StringComponent comp = new StringComponent();
        entity.addComponent(comp);
        entity.removeComponent(StringComponent.class);

        assertNull(entity.getComponent(StringComponent.class));
    }

    @Test
    public void testReplaceComponent() {
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
    public void testDestroyEntity() {
        EntityRef entity = entityManager.create();

        entity.addComponent(new StringComponent());
        entity.addComponent(new IntegerComponent());
        entity.destroy();

        assertNull(entity.getComponent(StringComponent.class));
        assertNull(entity.getComponent(IntegerComponent.class));
    }

    @Test
    public void testIterateComponents() {
        EntityRef entity = entityManager.create();
        StringComponent comp = new StringComponent();
        entity.addComponent(comp);

        for (Map.Entry<EntityRef, StringComponent> item : entityManager.listComponents(StringComponent.class)) {
            assertEquals(entity, item.getKey());
            assertEquals(comp, item.getValue());
        }
    }

    @Test
    public void testChangeComponentsDuringIterator() {
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
    public void testAddComponentEventSent() {
        EventSystem eventSystem = mock(EventSystem.class);
        entityManager.setEventSystem(eventSystem);
        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());

        verify(eventSystem).send(entity1, OnAddedComponent.newInstance(), comp);
        verify(eventSystem).send(entity1, OnActivatedComponent.newInstance(), comp);
    }

    @Test
    public void testRemoveComponentEventSent() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.removeComponent(StringComponent.class);

        verify(eventSystem).send(entity1, BeforeDeactivateComponent.newInstance(), comp);
        verify(eventSystem).send(entity1, BeforeRemoveComponent.newInstance(), comp);
    }

    @Test
    public void testChangeComponentEventSentWhenSave() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        StringComponent comp = entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.saveComponent(comp);

        verify(eventSystem).send(entity1, OnChangedComponent.newInstance(), comp);
    }

    @Test
    public void testChangeComponentEventSentWhenAddOverExisting() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        StringComponent comp2 = entity1.addComponent(new StringComponent());

        verify(eventSystem).send(entity1, OnChangedComponent.newInstance(), comp2);
    }

    @Test
    public void testMassRemovedComponentEventSentOnDestroy() {
        EventSystem eventSystem = mock(EventSystem.class);

        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entityManager.setEventSystem(eventSystem);
        entity1.destroy();

        verify(eventSystem).send(entity1, BeforeDeactivateComponent.newInstance());
        verify(eventSystem).send(entity1, BeforeRemoveComponent.newInstance());
    }

    @Test
    public void testIterateEntitiesFindsEntityWithSingleComponent() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void testIterateEntitiesDoesNotFindEntityMissingAComponent() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class, IntegerComponent.class));
        assertEquals(Collections.<EntityRef>emptyList(), results);
    }

    @Test
    public void testIterateEntitiesFindsEntityWithTwoComponents() {
        EntityRef entity1 = entityManager.create();
        entity1.addComponent(new StringComponent());
        entity1.addComponent(new IntegerComponent());

        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class, IntegerComponent.class));
        assertEquals(Lists.newArrayList(entity1), results);
    }

    @Test
    public void testIterateWithNoComponents() {
        List<EntityRef> results = Lists.newArrayList(entityManager.getEntitiesWith(StringComponent.class));
        assertEquals(Collections.<EntityRef>emptyList(), results);
    }

    @Test
    public void testGetComponentCountWhenNoComponents() {
        assertEquals(0, entityManager.getCountOfEntitiesWith(StringComponent.class));
    }

    @Test
    public void testGetComponentCount() {
        entityManager.create().addComponent(new StringComponent());
        entityManager.create().addComponent(new StringComponent());
        assertEquals(2, entityManager.getCountOfEntitiesWith(StringComponent.class));
    }

    @Test
    public void testDestroyingEntityInvalidatesEntityRefs() {
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
    public void testPrefabCopiedCorrectly() {
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
    public void testPrefabCopiedCorrectly2() {
        EntityRef test1 = entityManager.create("unittest:myprefab");
        EntityRef test2 = entityManager.create("unittest:myprefab");
        //This returns true because the Objectids are Identical.
        assertFalse(test1.getComponent(StringComponent.class) == (test2.getComponent(StringComponent.class)));
    }

    @Test
    public void testPrefabPersistedRetainedCorrectly() {
        PrefabData protoPrefab = new PrefabData();
        protoPrefab.setPersisted(false);
        prefab = Assets.generateAsset(new ResourceUrn("unittest:nonpersistentPrefab"), protoPrefab, Prefab.class);

        EntityRef entity1 = entityManager.create(prefab);
        assertFalse(entity1.isPersistent());
    }

    @Test
    public void testIsLoadedTrueOnCreate() {
        EntityRef entity = entityManager.create();
        assertTrue(entity.isActive());
    }

    @Test
    public void testIsLoadedFalseAfterDestroyed() {
        EntityRef entity = entityManager.create();
        entity.destroy();
        assertFalse(entity.isActive());
    }

    @Test
    public void testIsLoadedFalseAfterPersist() {
        EntityRef entity = entityManager.create();
        entityManager.deactivateForStorage(entity);
        assertFalse(entity.isActive());
    }

    @Test
    public void testIsLoadedTrueAfterRestore() {
        entityManager.setNextId(3);
        EntityRef entity = entityManager.createEntityWithId(2, Collections.<Component>emptyList());
        assertTrue(entity.isActive());
    }

    @Test
    public void testLoadedStateClearedWhenEntityManagerCleared() {
        EntityRef entity = entityManager.create();
        entityManager.clear();
        assertFalse(entity.isActive());
    }

    @Test
    public void testDestructionOfUnloadedEntitiesPrevented() {
        EntityRef entity = entityManager.create();
        entityManager.deactivateForStorage(entity);
        assertTrue(entity.exists());
        entity.destroy();
        assertTrue(entity.exists());
    }

    @Test
    public void testMoveToPool() {
        EntityRef entity = entityManager.create();
        long id = entity.getId();

        PojoEntityPool pool1 = new PojoEntityPool(entityManager);
        PojoEntityPool pool2 = new PojoEntityPool(entityManager);

        assertFalse(pool1.contains(id));
        assertFalse(pool2.contains(id));

        assertTrue(entityManager.moveToPool(id, pool1));
        assertTrue(pool1.contains(id));
        assertFalse(pool2.contains(id));

        assertTrue(entityManager.moveToPool(id, pool2));
        assertTrue(pool2.contains(id));
        assertFalse(pool1.contains(id));
    }
}
