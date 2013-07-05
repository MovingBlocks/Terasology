package org.terasology.persistence;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.junit.Before;
import org.junit.Test;
import org.terasology.persistence.serializers.EntityDataJSONFormat;
import org.terasology.protobuf.EntityData;

import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EntityDataJSONFormatTest {

    public static final String VALUE_NAME = "Name";
    private EntityData.World.Builder worldBuilder;
    private EntityData.Entity.Builder entityBuilder;
    private EntityData.Prefab.Builder prefabBuilder;
    private EntityData.Component.Builder componentBuilder;
    private EntityData.NameValue.Builder nameValueBuilder;

    @Before
    public void setup() {
        worldBuilder = EntityData.World.newBuilder();
        entityBuilder = EntityData.Entity.newBuilder();
        prefabBuilder = EntityData.Prefab.newBuilder();
        componentBuilder = EntityData.Component.newBuilder();
        componentBuilder.setType("Test");
        nameValueBuilder = EntityData.NameValue.newBuilder();
    }

    @Test
    public void testPersistWorldSimple() throws IOException {
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistNextId() throws Exception {
        worldBuilder.setNextEntityId(413);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistFreedIds() throws Exception {
        worldBuilder.addFreedEntityId(1);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistMultipleFreedIds() throws Exception {
        worldBuilder.addFreedEntityId(1);
        worldBuilder.addFreedEntityId(2);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEmptyEntity() throws Exception {
        EntityData.Entity entity = entityBuilder.build();
        worldBuilder.addEntity(entity);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEntityParent() throws Exception {
        entityBuilder.setParentPrefab("Test");
        EntityData.Entity entity = entityBuilder.build();
        worldBuilder.addEntity(entity);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEntityId() throws Exception {
        entityBuilder.setId(413);
        EntityData.Entity entity = entityBuilder.build();
        worldBuilder.addEntity(entity);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEntityRemovedComponent() throws Exception {
        entityBuilder.addRemovedComponent("String");
        EntityData.Entity entity = entityBuilder.build();
        worldBuilder.addEntity(entity);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEntityRemovedComponents() throws Exception {
        entityBuilder.addRemovedComponent("String");
        entityBuilder.addRemovedComponent("Integer");
        EntityData.Entity entity = entityBuilder.build();
        worldBuilder.addEntity(entity);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistEmptyComponent() throws Exception {
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());
        assertPersist(worldBuilder);
    }

    @Test
     public void testPersistComponentWithDouble() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addDouble(1));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());
        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1.0), actual.getEntity(0).getComponent(0).getField(0).getValue().getDoubleList());
    }

    @Test
    public void testPersistComponentWithFloat() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addFloat(1));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1f), actual.getEntity(0).getComponent(0).getField(0).getValue().getFloatList());
    }

    @Test
    public void testPersistComponentWithInteger() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addInteger(1));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1), actual.getEntity(0).getComponent(0).getField(0).getValue().getIntegerList());
    }

    @Test
    public void testPersistComponentWithIntegerList() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addInteger(1).addInteger(2));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1,2), actual.getEntity(0).getComponent(0).getField(0).getValue().getIntegerList());
    }

    @Test
    public void testPersistComponentWithLong() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addLong(1));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1L), actual.getEntity(0).getComponent(0).getField(0).getValue().getLongList());
    }

    @Test
    public void testPersistComponentWithBoolean() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addBoolean(true));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(true), actual.getEntity(0).getComponent(0).getField(0).getValue().getBooleanList());
    }

    @Test
    public void testPersistComponentWithString() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addString("Test"));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList("Test"), actual.getEntity(0).getComponent(0).getField(0).getValue().getStringList());
    }

    @Test
    public void testPersistComponentWithValueInValue() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addValue(EntityData.Value.newBuilder().addInteger(1).addInteger(2)));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals(Lists.newArrayList(1,2), actual.getEntity(0).getComponent(0).getField(0).getValue().getValue(0).getIntegerList());
    }

    @Test
    public void testPersistComponentWithValueMap() throws Exception {
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().addNameValue(EntityData.NameValue.newBuilder().setName("Fred").setValue(EntityData.Value.newBuilder().addInteger(1))));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());

        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertEquals("Fred", actual.getEntity(0).getComponent(0).getField(0).getValue().getNameValue(0).getName());
        assertEquals(1, actual.getEntity(0).getComponent(0).getField(0).getValue().getNameValue(0).getValue().getInteger(0));
    }

    @Test
    public void testPersistBytes() throws Exception {
        ByteString bytes = ByteString.copyFrom(new byte[]{1, 2, 3, 4});
        nameValueBuilder.setName(VALUE_NAME);
        nameValueBuilder.setValue(EntityData.Value.newBuilder().setBytes(bytes));
        componentBuilder.addField(nameValueBuilder);
        entityBuilder.addComponent(componentBuilder.build());
        worldBuilder.addEntity(entityBuilder.build());
        EntityData.World actual = persistAndRetrieve(worldBuilder.build());
        assertEquals(VALUE_NAME, actual.getEntity(0).getComponent(0).getField(0).getName());
        assertArrayEquals(bytes.toByteArray(), actual.getEntity(0).getComponent(0).getField(0).getValue().getBytes().toByteArray());
    }

    @Test
    public void testPersistEmptyPrefab() throws Exception {
        worldBuilder.addPrefab(prefabBuilder);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistPrefabName() throws Exception {
        prefabBuilder.setName("test:PrefabName");
        worldBuilder.addPrefab(prefabBuilder);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistPrefabParent() throws Exception {
        prefabBuilder.setParentName("test:PrefabName");
        worldBuilder.addPrefab(prefabBuilder);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistPrefabComponent() throws Exception {
        prefabBuilder.addComponent(componentBuilder);
        worldBuilder.addPrefab(prefabBuilder);
        assertPersist(worldBuilder);
    }

    @Test
    public void testPersistPersistableFlag() throws Exception {
        prefabBuilder.setPersisted(false);
        worldBuilder.addPrefab(prefabBuilder);
        assertPersist(worldBuilder);
    }

    private void assertPersist(EntityData.World.Builder worldBuilder) throws IOException{
        EntityData.World world = worldBuilder.build();
        EntityData.World newWorld = persistAndRetrieve(world);
        assertEquals(world, newWorld);
    }

    private EntityData.World persistAndRetrieve(EntityData.World world) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));
        EntityDataJSONFormat.write(world, writer);
        writer.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return EntityDataJSONFormat.readWorld(new BufferedReader(new InputStreamReader(bais)));
    }

}
