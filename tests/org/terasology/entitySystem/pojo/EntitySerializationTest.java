package org.terasology.entitySystem.pojo;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.terasology.entitySystem.EntityInfoComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.pojo.persistence.EntityPersister;
import org.terasology.entitySystem.pojo.persistence.EntityPersisterImpl;
import org.terasology.entitySystem.pojo.persistence.FieldInfo;
import org.terasology.entitySystem.pojo.persistence.SerializationInfo;
import org.terasology.entitySystem.pojo.persistence.extension.Vector3fTypeHandler;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntitySerializationTest {

    EntityManager entityManager;
    EntityPersister entityPersister;

    @Before
    public void setup() {
        entityManager = new PojoEntityManager();
        entityPersister = new EntityPersisterImpl();
        entityPersister.registerTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        entityPersister.registerComponentClass(EntityInfoComponent.class);
        entityPersister.registerComponentClass(IntegerComponent.class);
        entityPersister.registerComponentClass(StringComponent.class);
    }

    @Test
    public void testGetterSetterUtilization() throws Exception {
        SerializationInfo info = new SerializationInfo(GetterSetterComponent.class);
        info.addField(new FieldInfo(GetterSetterComponent.class.getDeclaredField("value"), GetterSetterComponent.class, new Vector3fTypeHandler()));

        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) info.deserialize(info.serialize(comp));
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }

    @Test
    public void testDeltaNoUnchangedComponents() throws Exception {
        Prefab prefab = new PojoPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create();
        entity.addComponent(new StringComponent("Value"));

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity, prefab);

        assertEquals(1, entityData.getId());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
    }

    @Test
    public void testDeltaAddNewComponent() throws Exception {
        Prefab prefab = new PojoPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create();
        entity.addComponent(new StringComponent("Value"));
        entity.addComponent(new IntegerComponent(1));

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity, prefab);

        assertEquals(1, entityData.getId());
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
        Prefab prefab = new PojoPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create();

        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity, prefab);

        assertEquals(1, entityData.getId());
        assertEquals(0, entityData.getComponentCount());
        assertEquals(Lists.newArrayList("String"), entityData.getRemovedComponentList());
    }

    @Test
    public void testDeltaChangedComponent() throws Exception {
        Prefab prefab = new PojoPrefab("Test");
        prefab.setComponent(new StringComponent("Value"));

        EntityRef entity = entityManager.create();
        entity.addComponent(new StringComponent("Delta"));
        EntityData.Entity entityData = entityPersister.serializeEntity(1, entity, prefab);

        assertEquals(1, entityData.getId());
        assertEquals(1, entityData.getComponentCount());
        assertEquals(0, entityData.getRemovedComponentCount());
        EntityData.Component componentData = entityData.getComponent(0);
        assertEquals("String", componentData.getType());
        assertEquals(Lists.newArrayList(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("Delta").build()).build()), componentData.getFieldList());
    }
}
