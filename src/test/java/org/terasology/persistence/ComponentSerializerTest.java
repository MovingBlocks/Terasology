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

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.classMetadata.copying.CopyStrategyLibrary;
import org.terasology.classMetadata.reflect.ReflectFactory;
import org.terasology.classMetadata.reflect.ReflectionReflectFactory;
import org.terasology.entitySystem.stubs.GetterSetterComponent;
import org.terasology.entitySystem.stubs.IntegerComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.serializers.ComponentSerializer;
import org.terasology.persistence.typeSerialization.TypeSerializationLibrary;
import org.terasology.persistence.typeSerialization.typeHandlers.extension.Quat4fTypeHandler;
import org.terasology.persistence.typeSerialization.typeHandlers.extension.Vector3fTypeHandler;
import org.terasology.protobuf.EntityData;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Immortius
 */
public class ComponentSerializerTest {
    private static ModuleManager moduleManager;
    private ComponentSerializer componentSerializer;
    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategyLibrary = CopyStrategyLibrary.create(reflectFactory);

    @BeforeClass
    public static void setupClass() {
        moduleManager = new ModuleManager();
    }

    @Before
    public void setup() {
        TypeSerializationLibrary serializationLibrary = new TypeSerializationLibrary(reflectFactory, copyStrategyLibrary);
        serializationLibrary.add(Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.add(Quat4f.class, new Quat4fTypeHandler());

        NetworkSystem networkSystem = mock(NetworkSystem.class);
        EntitySystemBuilder builder = new EntitySystemBuilder();
        EngineEntityManager entityManager = builder.build(moduleManager, networkSystem, new ReflectionReflectFactory());
        entityManager.getComponentLibrary().register(GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(StringComponent.class);
        entityManager.getComponentLibrary().register(IntegerComponent.class);
        ComponentLibrary componentLibrary = entityManager.getComponentLibrary();
        componentSerializer = new ComponentSerializer(componentLibrary, serializationLibrary);

    }

    @Test
    public void testGetterSetterUtilization() throws Exception {
        GetterSetterComponent comp = new GetterSetterComponent();
        GetterSetterComponent newComp = (GetterSetterComponent) componentSerializer.deserialize(componentSerializer.serialize(comp));
        assertTrue(comp.getterUsed);
        assertTrue(newComp.setterUsed);
    }

    @Test
    public void testSerializeComponentDeltas() throws Exception {
        EntityData.Component componentData = componentSerializer.serialize(new StringComponent("Original"), new StringComponent("Delta"));

        assertEquals("value", componentData.getField(0).getName());
        assertEquals("Delta", componentData.getField(0).getValue().getString(0));
    }

    @Test
    public void testComponentTypeIdUsedWhenLookupTableEnabled() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        Component stringComponent = new StringComponent("Test");
        EntityData.Component compData = componentSerializer.serialize(stringComponent);
        assertEquals(1, compData.getTypeIndex());
        assertFalse(compData.hasType());
    }

    @Test
    public void testComponentTypeIdUsedWhenLookupTableEnabledForComponentDeltas() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 413).build());

        EntityData.Component componentData = componentSerializer.serialize(new StringComponent("Original"), new StringComponent("Value"));

        assertEquals(413, componentData.getTypeIndex());
    }

    @Test
    public void testComponentTypeIdDeserializes() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        EntityData.Component compData = EntityData.Component.newBuilder().setTypeIndex(1)
                .addField(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("item"))).build();
        Component comp = componentSerializer.deserialize(compData);
        assertTrue(comp instanceof StringComponent);
        assertEquals("item", ((StringComponent) comp).value);
    }

    @Test
    public void testDeltaComponentTypeIdDeserializesWithValue() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        EntityData.Component compData = EntityData.Component.newBuilder().setTypeIndex(1)
                .addField(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("item"))).build();
        StringComponent original = new StringComponent("test");
        componentSerializer.deserializeOnto(original, compData);
        assertEquals("item", original.value);
    }

    @Test
    public void testDeltaComponentTypeIdDeserializesWithoutValue() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        EntityData.Component compData = EntityData.Component.newBuilder().setTypeIndex(1).addField(EntityData.NameValue.newBuilder().setName("value")).build();
        StringComponent original = new StringComponent("test");
        componentSerializer.deserializeOnto(original, compData);
        assertEquals("test", original.value);
    }
}
