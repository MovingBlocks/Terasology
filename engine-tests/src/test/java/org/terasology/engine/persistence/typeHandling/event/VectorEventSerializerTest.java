// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.event;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.engine.context.internal.ContextImpl;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.entitySystem.metadata.EventMetadata;
import org.terasology.engine.persistence.serializers.EventSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.entitysystem.event.Event;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.ModuleTypeRegistry;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.terasology.joml.test.VectorAssert.assertEquals;

public class VectorEventSerializerTest {
    Map<Class<? extends Event>, Integer> eventMap = new HashMap<>();
    int indexCount = 0;

    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer serializer;

    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);

    @BeforeEach
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class, moduleManager);
        context.put(ReflectFactory.class, reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategies);

        ModuleTypeRegistry typeRegistry = new ModuleTypeRegistry(moduleManager.getEnvironment());
        TypeHandlerLibrary typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);

        entitySystemLibrary = new EntitySystemLibrary(context, typeHandlerLibrary);

        serializer = new EventSerializer(entitySystemLibrary.getEventLibrary(), typeHandlerLibrary);

        registerEvent(Vector3fTestEvent.class);

        serializer.setIdMapping(eventMap);
    }

    //TODO: resolve hidden coupling in NetworkSystemImpl for fieldId's and EventSerializer
    private void registerEvent(Class<? extends Event> clazz) {

        entitySystemLibrary.getEventLibrary().register(new ResourceUrn("unittest", clazz.getName()), clazz);
        EventMetadata<? extends Event> metadata = entitySystemLibrary.getEventLibrary().getMetadata(clazz);
        byte fieldId = 0;
        for (FieldMetadata<?, ?> field : metadata.getFields()) {
            field.setId((byte) fieldId);
            fieldId++;
        }
        eventMap.put(clazz, ++indexCount);
    }


    @Test
    public void testEventSerializationConstant() throws IOException {

        Vector3fTestEvent a = new Vector3fTestEvent();
        a.v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new Vector2f(1.0f, 2.0f);

        a.v1c = new Vector3f(1.0f, 1.0f, 1.0f);
        a.v2c = new Vector4f(1.0f, 1.0f, 2.0f, 2.0f);
        a.v3c = new Vector2f(1.0f, 1.0f);

        EntityData.Event ev = serializer.serialize(a);
        Event dev = serializer.deserialize(ev);
        assumeTrue(dev instanceof Vector3fTestEvent);
        assertEquals(((Vector3fTestEvent) dev).v1, new Vector3f(1.0f, 2.0f, 3.0f), .00001f);
        assertEquals(((Vector3fTestEvent) dev).v2, new Vector4f(1.0f, 2.0f, 3.0f, 5.0f), .00001f);
        assertEquals(((Vector3fTestEvent) dev).v3, new Vector2f(1.0f, 2.0f), .00001f);

        assertEquals(((Vector3fTestEvent) dev).v1c, new Vector3f(1.0f, 1.0f, 1.0f), .00001f);
        assertEquals(((Vector3fTestEvent) dev).v2c, new Vector4f(1.0f, 1.0f, 2.0f, 2.0f), .00001f);
        assertEquals(((Vector3fTestEvent) dev).v3c, new Vector2f(1.0f, 1.0f), .00001f);
    }

    public static class Vector3fTestEvent implements Event {
        public Vector3f v1;
        public Vector4f v2;
        public Vector2f v3;

        public Vector3fc v1c;
        public Vector4fc v2c;
        public Vector2fc v3c;
    }
}
