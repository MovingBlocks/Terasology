/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.persistence;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.utilities.Assets;
import org.terasology.assets.AssetFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.context.Context;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.MappedTypeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 */
public class EntitySerializerTest {

    private static Context context;
    private static ModuleManager moduleManager;
    private ComponentLibrary componentLibrary;
    private EngineEntityManager entityManager;
    private EntitySerializer entitySerializer;
    private Prefab prefab;


    @BeforeClass
    public static void setupClass() throws Exception {
        context = new ContextImpl();
        CoreRegistry.setContext(context);
        moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManager();
        assetTypeManager.registerCoreAssetType(Prefab.class,
                (AssetFactory<Prefab, PrefabData>) PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
    }

    @Before
    public void setup() {
        context.put(NetworkSystem.class, mock(NetworkSystem.class));

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);
        entityManager.getComponentLibrary().register(new SimpleUri("test", "gettersetter"), GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(new SimpleUri("test", "string"), StringComponent.class);
        entityManager.getComponentLibrary().register(new SimpleUri("test", "integer"), IntegerComponent.class);
        entitySerializer = new EntitySerializer(entityManager);
        componentLibrary = entityManager.getComponentLibrary();

        PrefabData prefabData = new PrefabData();
        prefabData.addComponent(new StringComponent("Value"));
        prefab = Assets.generateAsset(new ResourceUrn("test:Test"), prefabData, Prefab.class);
    }

    @Test
    public void testDeltaNoUnchangedComponents() throws Exception {


        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entitySerializer.serialize(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
    }

    @Test
    public void testDeltaAddNewComponent() throws Exception {
        EntityRef entity = entityManager.create(prefab);
        entity.addComponent(new IntegerComponent(1));

        EntityData.Entity entityData = entitySerializer.serialize(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(1, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
        EntityData.Component componentData = entityData.getComponent(0);
        assertEquals("test:integer", componentData.getType());
        assertEquals(1, componentData.getFieldCount());
        EntityData.NameValue field = componentData.getField(0);
        assertEquals("value", field.getName());
        assertEquals(1, field.getValue().getInteger(0));
    }

    @Test
    public void testDeltaRemoveComponent() throws Exception {
        EntityRef entity = entityManager.create(prefab);
        entity.removeComponent(StringComponent.class);

        EntityData.Entity entityData = entitySerializer.serialize(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(Lists.newArrayList("test:string"), entityData.getRemovedComponentList());
    }

    @Test
    public void testDeltaChangedComponent() throws Exception {
        EntityRef entity = entityManager.create(prefab);
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Delta";
        entity.saveComponent(comp);
        EntityData.Entity entityData = entitySerializer.serialize(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(1, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
        EntityData.Component componentData = entityData.getComponent(0);
        assertEquals("test:string", componentData.getType());
        assertEquals(Lists.newArrayList(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("Delta").build()).build()),
                componentData.getFieldList());
    }

    @Test
    public void testDeltaLoadNoChange() throws Exception {
        EntityRef entity = entityManager.create("test:Test");
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testDeltaLoadAddedComponent() throws Exception {
        EntityRef entity = entityManager.create("test:Test");
        entity.addComponent(new IntegerComponent(2));
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
        assertTrue(loadedEntity.hasComponent(IntegerComponent.class));
        assertEquals(2, loadedEntity.getComponent(IntegerComponent.class).value);
    }

    @Test
    public void testDeltaLoadRemovedComponent() throws Exception {
        EntityRef entity = entityManager.create("test:Test");
        entity.removeComponent(StringComponent.class);
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertFalse(loadedEntity.hasComponent(StringComponent.class));
    }

    @Test
    public void testDeltaLoadChangedComponent() throws Exception {
        EntityRef entity = entityManager.create("test:Test");
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Delta";
        entity.saveComponent(comp);
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Delta", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testPrefabMaintainedOverSerialization() throws Exception {
        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef newEntity = entitySerializer.deserialize(entityData);
        assertTrue(newEntity.hasComponent(EntityInfoComponent.class));
        EntityInfoComponent comp = newEntity.getComponent(EntityInfoComponent.class);
        assertEquals(prefab, comp.parentPrefab);
    }

    @Test
    public void testAlwaysRelevantPersisted() throws Exception {
        EntityRef entity = entityManager.create(prefab);
        boolean defaultSetting = entity.isAlwaysRelevant();
        entity.setAlwaysRelevant(!defaultSetting);

        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef newEntity = entitySerializer.deserialize(entityData);
        assertEquals(!defaultSetting, newEntity.isAlwaysRelevant());
    }

    @Test
    public void testMappedTypeHandling() throws Exception {
        componentLibrary.register(new SimpleUri("test", "mappedtype"), MappedTypeComponent.class);

        EntityRef entity = entityManager.create();
        entity.addComponent(new MappedTypeComponent());
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(MappedTypeComponent.class));
    }
}
