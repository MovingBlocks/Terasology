// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.event;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.TeraAssert;

import java.io.IOException;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class VectorEventSerializer {

    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer serializer;

    public static class Vector3fConstant implements Event {
        public Vector3fc v1;
        public Vector4fc v2;
        public Vector2fc v3;
    }

    @BeforeEach
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);

        Reflections reflections = new Reflections(getClass().getClassLoader());
        TypeHandlerLibrary typeHandlerLibrary = new TypeHandlerLibrary(reflections);

        entitySystemLibrary = new EntitySystemLibrary(context, typeHandlerLibrary);
        serializer = new EventSerializer(entitySystemLibrary.getEventLibrary(), typeHandlerLibrary);
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
