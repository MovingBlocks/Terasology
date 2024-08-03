// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.serializers;

import com.google.gson.Gson;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.jupiter.api.Test;
import org.terasology.engine.ModuleEnvironmentTest;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import static org.terasology.joml.test.VectorAssert.assertEquals;

@SuppressWarnings("FieldCanBeLocal")
class VectorTypeSerializerTest extends ModuleEnvironmentTest {
    private TypeHandlerLibrary typeHandlerLibrary;
    private Serializer<?> gsonSerializer;
    private Gson gson = new Gson();

    @Override
    public void setup() {
        ModuleContext.setContext(moduleManager.getEnvironment().get(new Name("unittest")));

        typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);

        gsonSerializer = new Serializer<>(typeHandlerLibrary,
                new GsonPersistedDataSerializer(),
                new GsonPersistedDataWriter(gson),
                new GsonPersistedDataReader(gson)
        );
    }

    @Test
    void testSerializationConstant() {
        TestObject2 a = new TestObject2();
        a.v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new Vector2f(1.0f, 2.0f);
        byte[] data = gsonSerializer.serialize(a, new TypeInfo<TestObject2>() {
        }).get();

        TestObject2 o = gsonSerializer.deserialize(new TypeInfo<TestObject2>() {
        }, data).get();

        assertEquals(new Vector3f(1.0f, 2.0f, 3.0f), o.v1, .00001f);
        assertEquals(new Vector4f(1.0f, 2.0f, 3.0f, 5.0f), o.v2, .00001f);
        assertEquals(new Vector2f(1.0f, 2.0f), o.v3, .00001f);
    }

    public static class TestObject {
        public Vector3f v1;
        public Vector2f v2;
        public Vector4f v3;
    }

    public static class TestObject2 {
        public Vector3fc v1;
        public Vector4fc v2;
        public Vector2fc v3;
    }
}
