// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.event;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.assets.ResourceUrn;
import org.terasology.context.internal.ContextImpl;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.entitySystem.metadata.EventMetadata;
import org.terasology.naming.Name;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.testUtil.TeraAssert;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class VectorEventSerializer {

    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer serializer;

    private ReflectFactory reflectFactory = new ReflectionReflectFactory();
    private CopyStrategyLibrary copyStrategies = new CopyStrategyLibrary(reflectFactory);
    HashMap<Class<? extends Event>, Integer> eventMap = new HashMap<>();
    int indexCount = 0;
    public static class Vector3fConstant implements Event {
        public Vector3f v1;
        public Vector4f v2;
        public Vector2f v3;
    }

    @BeforeEach
    public void setup() throws Exception {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);
        ModuleManager moduleManager = ModuleManagerFactory.create();
        context.put(ModuleManager.class,moduleManager);
        context.put(ReflectFactory.class, reflectFactory);
        context.put(CopyStrategyLibrary.class, copyStrategies);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary typeHandlerLibrary = new TypeHandlerLibrary(reflections);

        entitySystemLibrary = new EntitySystemLibrary(context, typeHandlerLibrary);

        serializer = new EventSerializer(entitySystemLibrary.getEventLibrary(), typeHandlerLibrary);

        registerEvent(Vector3fConstant.class);

        serializer.setIdMapping(eventMap);
    }

    private void registerEvent(Class<? extends Event> clazz) {

        entitySystemLibrary.getEventLibrary().register(new ResourceUrn("unittest", clazz.getName()), clazz);
        EventMetadata<? extends Event> metadata = entitySystemLibrary.getEventLibrary().getMetadata(clazz);
        byte fieldId = 0;
        for (FieldMetadata<?, ?> field : metadata.getFields()) {
            field.setId((byte) fieldId);
            fieldId++;
        }
        eventMap.put(clazz,++indexCount);
    }


    @Test
    public void testEventSerializationConstant() throws IOException {

        Vector3fConstant a = new Vector3fConstant();
        a.v1 = new org.joml.Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new org.joml.Vector2f(1.0f, 2.0f);

        EntityData.Event ev = serializer.serialize(a);
        Event dev = serializer.deserialize(ev);
        assumeTrue(dev instanceof Vector3fConstant);
        TeraAssert.assertEquals(((Vector3fConstant) dev).v1, new org.joml.Vector3f(1.0f, 2.0f, 3.0f), .00001f);
        TeraAssert.assertEquals(((Vector3fConstant) dev).v2, new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f), .00001f);
        TeraAssert.assertEquals(((Vector3fConstant) dev).v3, new org.joml.Vector2f(1.0f, 2.0f), .00001f);
    }
}
