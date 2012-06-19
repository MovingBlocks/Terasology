package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.metadata.ComponentLibraryImpl;
import org.terasology.entitySystem.metadata.ComponentMetadata;
import org.terasology.entitySystem.metadata.FieldMetadata;
import org.terasology.entitySystem.metadata.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.metadata.ComponentUtil;
import org.terasology.entitySystem.persistence.EntityPersisterHelper;
import org.terasology.entitySystem.persistence.EntityPersisterHelperImpl;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.MappedTypeComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {

	ComponentLibrary componentLibrary;
    PojoEntityManager entityManager;
    EntityPersisterHelper entityPersisterHelper;
    PrefabManager prefabManager;

    @Before
    public void setup() {

        componentLibrary = new ComponentLibraryImpl();
        componentLibrary.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        componentLibrary.registerComponentClass(IntegerComponent.class);
        componentLibrary.registerComponentClass(StringComponent.class);
        componentLibrary.registerComponentClass(GetterSetterComponent.class);
        prefabManager = new PojoPrefabManager(componentLibrary);
        entityManager = new PojoEntityManager(componentLibrary, prefabManager);
        entityPersisterHelper = new EntityPersisterHelperImpl(entityManager);
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
        entity.getComponent(StringComponent.class).value = "Delta";
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
        entity.getComponent(StringComponent.class).value = "Delta";
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
        entity.getComponent(StringComponent.class).value = "New";

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
        entity.getComponent(StringComponent.class).value = "New";

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
