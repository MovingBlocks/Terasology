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
import org.terasology.engine.testUtil.Assertions;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;

import static org.terasology.joml.test.VectorAssert.assertEquals;

class VectorTypeSerializerTest extends ModuleEnvironmentTest {
    static class TestObject {
        public Vector3f v1;
        public Vector2f v2;
        public Vector4f v3;
    }

    static class TestObject2 {
        public Vector3fc v1;
        public Vector4fc v2;
        public Vector2fc v3;
    }

    private TypeHandlerLibrary typeHandlerLibrary;
    private ProtobufSerializer protobufSerializer;
    private Serializer<?> gsonSerializer;
    private Gson gson = new Gson();

    @Override
    public void setup() {
        ModuleContext.setContext(moduleManager.getEnvironment().get(new Name("unittest")));

        typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);

        protobufSerializer = new ProtobufSerializer(typeHandlerLibrary);
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
        },data).get();

        Assertions.assertNotEmpty(typeHandlerLibrary.getTypeHandler(Vector3fc.class));
        Assertions.assertNotEmpty(typeHandlerLibrary.getTypeHandler(Vector3f.class));

        Assertions.assertNotEmpty(typeRegistry.load("org.joml.Vector3fc"));
        Assertions.assertNotEmpty(typeRegistry.load("org.joml.Vector3f"));

        ModuleEnvironment env = moduleManager.getEnvironment();
        Assertions.assertNotEmpty(env.getSubtypesOf(Vector3fc.class));

        Assertions.assertNotEmpty(typeRegistry.getSubtypesOf(Vector3fc.class));

        assertEquals(new Vector3f(1.0f, 2.0f, 3.0f), o.v1, .00001f);
        assertEquals(new Vector4f(1.0f, 2.0f, 3.0f, 5.0f), o.v2, .00001f);
        assertEquals(new Vector2f(1.0f, 2.0f), o.v3, .00001f);
    }

    @Test
    void testJsonSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] data = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(data, new TypeInfo<TestObject>() {
        });

        assertEquals(new Vector3f(11.5f, 13.15f, 3), o.v1, .00001f);
        assertEquals(new Vector2f(12f, 13f), o.v2, .00001f);
        assertEquals(new Vector4f(12, 12.2f, 3f, 15.5f), o.v3, .00001f);
    }

    @Test
    void testProtobufSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] bytes = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(bytes, new TypeInfo<TestObject>() {
        });

        assertEquals(new Vector3f(11.5f, 13.15f, 3), o.v1, .00001f);
        assertEquals(new Vector2f(12f, 13f), o.v2, .00001f);
        assertEquals(new Vector4f(12, 12.2f, 3f, 15.5f), o.v3, .00001f);
    }
}
