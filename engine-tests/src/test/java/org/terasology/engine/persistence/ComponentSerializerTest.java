// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence;

import com.google.common.collect.ImmutableMap;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.engine.context.Context;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.serializers.ComponentSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.mathTypes.QuaternionfTypeHandler;
import org.terasology.engine.persistence.typeHandling.mathTypes.Vector3fTypeHandler;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.unittest.stubs.GetterSetterComponent;
import org.terasology.unittest.stubs.IntegerComponent;
import org.terasology.unittest.stubs.StringComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ComponentSerializerTest {
    private static ModuleManager moduleManager;
    private ComponentSerializer componentSerializer;
    private Context context;

    @BeforeAll
    public static void setupClass() throws Exception {
        moduleManager = ModuleManagerFactory.create();
    }

    @BeforeEach
    public void setup() {
        context = new ContextImpl();
        context.put(RecordAndReplayCurrentStatus.class, new RecordAndReplayCurrentStatus());
        context.put(ModuleManager.class, moduleManager);
        CoreRegistry.setContext(context);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary serializationLibrary = new TypeHandlerLibraryImpl(reflections);

        serializationLibrary.addTypeHandler(Vector3f.class, new Vector3fTypeHandler());
        serializationLibrary.addTypeHandler(Quaternionf.class, new QuaternionfTypeHandler());

        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        context.put(NetworkSystem.class, networkSystem);
        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager entityManager = context.get(EngineEntityManager.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "gettersetter"), GetterSetterComponent.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "string"), StringComponent.class);
        entityManager.getComponentLibrary().register(new ResourceUrn("test", "integer"), IntegerComponent.class);
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
        EntityData.Component compData = EntityData.Component.newBuilder()
                .setTypeIndex(1)
                .addField(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("item")))
                .build();
        Component comp = componentSerializer.deserialize(compData);
        assertTrue(comp instanceof StringComponent);
        assertEquals("item", ((StringComponent) comp).value);
    }

    @Test
    public void testDeltaComponentTypeIdDeserializesWithValue() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        EntityData.Component compData = EntityData.Component.newBuilder()
                .setTypeIndex(1)
                .addField(EntityData.NameValue.newBuilder().setName("value").setValue(EntityData.Value.newBuilder().addString("item")))
                .build();
        StringComponent original = new StringComponent("test");
        componentSerializer.deserializeOnto(original, compData);
        assertEquals("item", original.value);
    }

    @Test
    public void testDeltaComponentTypeIdDeserializesWithoutValue() throws Exception {
        componentSerializer.setIdMapping(ImmutableMap.<Class<? extends Component>, Integer>builder().put(StringComponent.class, 1).build());
        EntityData.Component compData = EntityData.Component.newBuilder()
                .setTypeIndex(1)
                .addField(EntityData.NameValue.newBuilder().setName("value"))
                .build();
        StringComponent original = new StringComponent("test");
        componentSerializer.deserializeOnto(original, compData);
        assertEquals(null, original.value);
    }
}
