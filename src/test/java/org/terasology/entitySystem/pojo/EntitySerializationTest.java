package org.terasology.entitySystem.pojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import javax.vecmath.Vector3f;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.PersistableEntityManager;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.ComponentUtil;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.persistence.EntityPersisterHelper;
import org.terasology.entitySystem.persistence.EntityPersisterHelperImpl;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.MappedTypeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.logic.mod.ModManager;
import org.terasology.protobuf.EntityData;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {

	private ComponentLibrary componentLibrary;
    private PersistableEntityManager entityManager;
    private EntityPersisterHelper entityPersisterHelper;
    private PrefabManager prefabManager;
    private static ModManager modManager;

    @BeforeClass
    public static void setupClass() {
        modManager = new ModManager();
    }

    @Before
    public void setup() {

        EntitySystemBuilder builder = new EntitySystemBuilder();
        entityManager = builder.build(modManager);
        entityManager.getComponentLibrary().registerComponentClass(GetterSetterComponent.class);
        entityManager.getComponentLibrary().registerComponentClass(StringComponent.class);
        entityManager.getComponentLibrary().registerComponentClass(IntegerComponent.class);
        entityPersisterHelper = new EntityPersisterHelperImpl(entityManager);
        componentLibrary = entityManager.getComponentLibrary();
        prefabManager = entityManager.getPrefabManager();
    }

    @Test
    public void testGetterSetterUtilization() throws Exception {
        ComponentMetadata info = new ComponentMetadata(GetterSetterComponent.class);
        info.addField(new FieldMetadata(GetterSetterComponent.class.getDeclaredField("value"), GetterSetterComponent.class, new Vector3fTypeHandler()));

        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) entityPersisterHelper.deserializeComponent(entityPersisterHelper.serializeComponent(comp));
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }

    @Test
    public void testDeltaNoUnchangedComponents() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
    }

    @Test
    public void testDeltaAddNewComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        entity.addComponent(new IntegerComponent(1));

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);

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
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        entity.removeComponent(StringComponent.class);

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);

        assertEquals(entity.getId(), entityData.getId());
        assertEquals(prefab.getName(), entityData.getParentPrefab());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(Lists.newArrayList("String"), entityData.getRemovedComponentList());
    }

    @Test
    public void testDeltaChangedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Delta";
        entity.saveComponent(comp);
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);

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
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef loadedEntity = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testDeltaLoadAddedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        entity.addComponent(new IntegerComponent(2));
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef loadedEntity = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Value", loadedEntity.getComponent(StringComponent.class).value);
        assertTrue(loadedEntity.hasComponent(IntegerComponent.class));
        assertEquals(2, loadedEntity.getComponent(IntegerComponent.class).value);
    }

    @Test
    public void testDeltaLoadRemovedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        entity.removeComponent(StringComponent.class);
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef loadedEntity = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertFalse(loadedEntity.hasComponent(StringComponent.class));
    }

    @Test
    public void testDeltaLoadChangedComponent() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create("Test");
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "Delta";
        entity.saveComponent(comp);
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef loadedEntity = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(StringComponent.class));
        assertEquals("Delta", loadedEntity.getComponent(StringComponent.class).value);
    }

    @Test
    public void testComponentTypeIdUsedWhenLookupTableEnabled() throws Exception {
        entityPersisterHelper.setUsingLookupTables(true);
        EntityRef entity = entityManager.create();
        entity.addComponent(new StringComponent("Test"));
        EntityData.World world = entityPersisterHelper.serializeWorld();
        int typeId = world.getComponentClassList().indexOf(ComponentUtil.getComponentClassName(StringComponent.class));
        assertFalse(typeId == -1);
        boolean found = false;
        for (EntityData.Component component : world.getEntity(0).getComponentList()) {
            assertTrue(component.hasTypeIndex());
            assertFalse(component.hasType());
            if (component.getTypeIndex() == typeId) {
                found = true;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testComponentTypeIdUsedWhenLookupTableEnabledForComponentDeltas() throws Exception {
        entityPersisterHelper.setUsingLookupTables(true);
        Map<Integer, Class<? extends Component>> componentIdTable = Maps.newHashMap();
        componentIdTable.put(413, StringComponent.class);
        entityPersisterHelper.setComponentTypeIdTable(componentIdTable);

        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "New";
        entity.saveComponent(comp);

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);

        assertEquals(413, entityData.getComponent(0).getTypeIndex());
    }

    @Test
    public void testComponentTypeIdDeserializes() throws Exception {
        entityPersisterHelper.setUsingLookupTables(true);
        EntityRef entity = entityManager.create();
        entity.addComponent(new StringComponent("Test"));
        EntityData.World world = entityPersisterHelper.serializeWorld();

        entityManager.clear();
        entityPersisterHelper.deserializeWorld(world);
        assertNotNull(entity.getComponent(StringComponent.class));
    }

    @Test
    public void testDeltaComponentTypeIdDeserializes() throws Exception {
        entityPersisterHelper.setUsingLookupTables(true);
        Map<Integer, Class<? extends Component>> componentIdTable = Maps.newHashMap();
        componentIdTable.put(413, StringComponent.class);
        entityPersisterHelper.setComponentTypeIdTable(componentIdTable);

        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);
        StringComponent comp = entity.getComponent(StringComponent.class);
        comp.value = "New";
        entity.saveComponent(comp);

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef result = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(result.hasComponent(StringComponent.class));
        assertEquals("New", result.getComponent(StringComponent.class).value);
    }

    @Test
    public void testPrefabMaintainedOverSerialization() throws Exception {
        Prefab prefab = prefabManager.createPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create(prefab);

        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef newEntity = entityPersisterHelper.deserializeEntity(entityData);
        assertTrue(newEntity.hasComponent(EntityInfoComponent.class));
        EntityInfoComponent comp = newEntity.getComponent(EntityInfoComponent.class);
        assertEquals(prefab.getName(), comp.parentPrefab);
    }

    @Test
    public void testNotPersistedIfFlagedOtherwise() throws Exception {
        EntityRef entity = entityManager.create();
        entity.setPersisted(false);
        int id = entity.getId();

        EntityData.World worldData = entityPersisterHelper.serializeWorld();
        assertEquals(0, worldData.getEntityCount());
        assertEquals(1, worldData.getFreedEntityIdCount());
        assertEquals(id, worldData.getFreedEntityId(0));
    }

    @Test
    public void testMappedTypeHandling() throws Exception {
        componentLibrary.registerComponentClass(MappedTypeComponent.class);

        EntityRef entity = entityManager.create();
        entity.addComponent(new MappedTypeComponent());
        EntityData.Entity entityData = entityPersisterHelper.serializeEntity(entity);
        entityManager.clear();
        EntityRef loadedEntity = entityPersisterHelper.deserializeEntity(entityData);

        assertTrue(loadedEntity.exists());
        assertTrue(loadedEntity.hasComponent(MappedTypeComponent.class));
    }

}
