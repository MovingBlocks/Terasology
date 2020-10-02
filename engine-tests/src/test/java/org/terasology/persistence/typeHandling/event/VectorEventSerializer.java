// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.event;

import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.context.internal.ContextImpl;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.naming.Name;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.serializers.EventSerializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.TeraAssert;

import java.io.IOException;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

public class VectorEventSerializer extends ModuleEnvironmentTest {

    private TypeHandlerLibrary typeHandlerLibrary;
    private EntitySystemLibrary entitySystemLibrary;
    private EventSerializer serializer;

    @Override
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);

        ModuleContext.setContext(moduleManager.getEnvironment().get(new Name("unittest")));

        typeHandlerLibrary = TypeHandlerLibrary.forModuleEnvironment(moduleManager, typeRegistry);

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
