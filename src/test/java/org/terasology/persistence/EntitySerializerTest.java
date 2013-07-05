package org.terasology.persistence;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.internal.EntityInfoComponent;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.persistence.serializers.EntitySerializer;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabData;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.MappedTypeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.logic.mod.ModManager;
import org.terasology.network.NetworkSystem;
import org.terasology.protobuf.EntityData;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializerTest {

    private ComponentLibrary componentLibrary;
    private EngineEntityManager entityManager;
    private EntitySerializer entitySerializer;
    private static ModManager modManager;
    private Prefab prefab;

    @BeforeClass
    public static void setupClass() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {

        EntitySystemBuilder builder = new EntitySystemBuilder();
        entityManager = builder.build(modManager, mock(NetworkSystem.class));
        entityManager.getComponentLibrary().register(GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(StringComponent.class);
        entityManager.getComponentLibrary().register(IntegerComponent.class);
        entitySerializer = new EntitySerializer(entityManager);
        componentLibrary = entityManager.getComponentLibrary();
        PrefabManager prefabManager = entityManager.getPrefabManager();

        PrefabData prefabData = new PrefabData();
        prefabData.addComponent(new StringComponent("Value"));
        prefab = Assets.generateAsset(new AssetUri(AssetType.PREFAB, "test:Test"), prefabData, Prefab.class);
        prefabManager.registerPrefab(prefab);
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
        assertEquals("Integer", componentData.getType());
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
        assertEquals(Lists.newArrayList("String"), entityData.getRemovedComponentList());
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
        assertEquals("String", componentData.getType());
        assertEquals(Lists.newArrayList(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("Delta").build()).build()), componentData.getFieldList());
    }

    @Test
    public void testDeltaLoadNoChange() throws Exception {
        EntityRef entity = entityManager.create("test:Test");
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        entityManager.clear();
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
        entityManager.clear();
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
        entityManager.clear();
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
        entityManager.clear();
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Delta", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testPrefabMaintainedOverSerialization() throws Exception {
        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entitySerializer.serialize(entity);
        entityManager.clear();
        EntityRef newEntity = entitySerializer.deserialize(entityData);
        assertTrue(newEntity.hasComponent(EntityInfoComponent.class));
        EntityInfoComponent comp = newEntity.getComponent(EntityInfoComponent.class);
        assertEquals(prefab.getName(), comp.parentPrefab);
    }

    @Test
    public void testMappedTypeHandling() throws Exception {
        componentLibrary.register(MappedTypeComponent.class);

        EntityRef entity = entityManager.create();
        entity.addComponent(new MappedTypeComponent());
        EntityData.Entity entityData = entitySerializer.serialize(entity);
        entityManager.clear();
        EntityRef loadedEntity = entitySerializer.deserialize(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(MappedTypeComponent.class));
    }

}
