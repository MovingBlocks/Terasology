// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.DoNotPersist;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityInfoComponent;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabData;
import org.terasology.engine.entitySystem.prefab.internal.PojoPrefab;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.serializers.EntitySerializer;
import org.terasology.engine.persistence.serializers.PersistenceComponentSerializeCheck;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.utilities.Assets;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManager;
import org.terasology.gestalt.assets.module.ModuleAwareAssetTypeManagerImpl;
import org.terasology.gestalt.entitysystem.component.EmptyComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.unittest.stubs.GetterSetterComponent;
import org.terasology.unittest.stubs.IntegerComponent;
import org.terasology.unittest.stubs.MappedTypeComponent;
import org.terasology.unittest.stubs.StringComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.CHUNK;
import static org.terasology.engine.entitySystem.entity.internal.EntityScope.GLOBAL;

public class EntitySerializerTest {

    private static Context context;
    private static ModuleManager moduleManager;
    private ComponentLibrary componentLibrary;
    private EngineEntityManager entityManager;
    private EntitySerializer entitySerializer;
    private Prefab prefab;


    @BeforeAll
    public static void setupClass() throws Exception {
        context = new ContextImpl();
        CoreRegistry.setContext(context);
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);

        ModuleAwareAssetTypeManager assetTypeManager = new ModuleAwareAssetTypeManagerImpl();
        assetTypeManager.createAssetType(Prefab.class, PojoPrefab::new, "prefabs");
        assetTypeManager.switchEnvironment(moduleManager.getEnvironment());
        context.put(AssetManager.class, assetTypeManager.getAssetManager());
    }

    @BeforeEach
    public void setup() {
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        entityManager = context.get(EngineEntityManager.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "gettersetter"), GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "string"), StringComponent.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "integer"), IntegerComponent.class);
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
        assertEquals(Lists.newArrayList(EntityData.NameValue.newBuilder()
                        .setName("value")
                        .setValue(EntityData.Value.newBuilder().addString("Delta").build())
                        .build()),
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
        EntityScope newScope = defaultSetting ? CHUNK : GLOBAL;
        entity.setScope(newScope);

        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef newEntity = entitySerializer.deserialize(entityData);
        assertEquals(newScope, newEntity.getScope());
        assertEquals(!defaultSetting, newEntity.isAlwaysRelevant());
    }

    @Test
    public void testScopePersisted() {
        EntityRef entity = entityManager.create(prefab);
        for (EntityScope scope : EntityScope.values()) {
            entity.setScope(scope);

            entity = serializeDeserializeEntity(entity);

            assertEquals(scope, entity.getScope());
        }
    }

    @Test
    public void testMappedTypeHandling() throws Exception {
        componentLibrary.register(new ResourceUrn("test", "mappedtype"), MappedTypeComponent.class);

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

    private EntityRef serializeDeserializeEntity(EntityRef entity) {
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        return entitySerializer.deserialize(entityData);
    }

    @Test
    public void testComponentWithDoNotPersistIsNotPersisted() {
        componentLibrary.register(new ResourceUrn("test", "nonpersisted"), NonpersistedComponent.class);

        EntityRef entity = entityManager.create();
        entity.addComponent(new NonpersistedComponent());
        entitySerializer.setComponentSerializeCheck(new PersistenceComponentSerializeCheck());
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertFalse(loadedEntity.hasComponent(NonpersistedComponent.class));
    }

    @Test
    public void testComponentWithDoNotPersistIsIgnoredUponDeserialization() {
        componentLibrary.register(new ResourceUrn("test", "nonpersisted"), NonpersistedComponent.class);

        EntityRef entity = entityManager.create();
        entity.addComponent(new NonpersistedComponent());
        entitySerializer.setComponentSerializeCheck(metadata -> true);
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        long nextId = entityManager.getNextId();
        entityManager.clear();
        entityManager.setNextId(nextId);
        entitySerializer.setComponentSerializeCheck(new PersistenceComponentSerializeCheck());
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertFalse(loadedEntity.hasComponent(NonpersistedComponent.class));
    }

    @DoNotPersist
    public static class NonpersistedComponent extends EmptyComponent<NonpersistedComponent> {
    }
}
