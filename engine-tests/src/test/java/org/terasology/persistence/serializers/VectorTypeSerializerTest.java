// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers;

import com.google.gson.Gson;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.naming.Name;
import org.terasology.persistence.ModuleContext;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.reflection.TypeInfo;
import org.terasology.testUtil.TeraAssert;

import java.io.IOException;

class VectorTypeSerializerTest extends ModuleEnvironmentTest {

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
    void testSerializationConstant() throws IOException {
        TestObject2 a = new TestObject2();
        a.v1 = new org.joml.Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new org.joml.Vector2f(1.0f, 2.0f);
        byte[] data = gsonSerializer.serialize(a, new TypeInfo<TestObject2>() {
        }).get();

        TestObject2 o = gsonSerializer.deserialize(new TypeInfo<TestObject2>() {
        },data).get();
        TeraAssert.assertEquals(o.v1, new org.joml.Vector3f(1.0f, 2.0f, 3.0f), .00001f);
        TeraAssert.assertEquals(o.v2, new org.joml.Vector4f(1.0f, 2.0f, 3.0f, 5.0f), .00001f);
        TeraAssert.assertEquals(o.v3, new org.joml.Vector2f(1.0f, 2.0f), .00001f);
    }

    @Test
    void testJsonSerializeRemapped() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);
        a.v11 = new org.joml.Vector3f(11.5f, 13.15f, 3);
        a.v22 = new org.joml.Vector2f(12, 13f);
        a.v33 = new org.joml.Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] data = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject1 o = protobufSerializer.fromBytes(data, new TypeInfo<TestObject1>() {
        });

        TeraAssert.assertEquals(o.v1, new org.joml.Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v2, new org.joml.Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v3, new org.joml.Vector4f(12, 12.2f, 3f, 15.5f), .00001f);

    }

    @Test
    void testProtobufSerializeRemapped() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);
        a.v11 = new org.joml.Vector3f(11.5f, 13.15f, 3);
        a.v22 = new org.joml.Vector2f(12, 13f);
        a.v33 = new org.joml.Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] data = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject1 o = protobufSerializer.fromBytes(data, new TypeInfo<TestObject1>() {
        });

        TeraAssert.assertEquals(o.v1, new org.joml.Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v2, new org.joml.Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v3, new org.joml.Vector4f(12, 12.2f, 3f, 15.5f), .00001f);

    }

    @Test
    void testJsonSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);
        a.v11 = new org.joml.Vector3f(11.5f, 13.15f, 3);
        a.v22 = new org.joml.Vector2f(12, 13f);
        a.v33 = new org.joml.Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] data = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(data, new TypeInfo<TestObject>() {
        });

        TeraAssert.assertEquals(o.v1, new Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v2, new Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v3, new Vector4f(12, 12.2f, 3f, 15.5f), .00001f);

        TeraAssert.assertEquals(o.v11, new org.joml.Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v22, new org.joml.Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v33, new org.joml.Vector4f(12, 12.2f, 3f, 15.5f), .00001f);
    }

    @Test
    void testProtobufSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);
        a.v11 = new org.joml.Vector3f(11.5f, 13.15f, 3);
        a.v22 = new org.joml.Vector2f(12, 13f);
        a.v33 = new org.joml.Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] bytes = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(bytes, new TypeInfo<TestObject>() {
        });

        TeraAssert.assertEquals(o.v1, new Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v2, new Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v3, new Vector4f(12, 12.2f, 3f, 15.5f), .00001f);

        TeraAssert.assertEquals(o.v11, new org.joml.Vector3f(11.5f, 13.15f, 3), .00001f);
        TeraAssert.assertEquals(o.v22, new org.joml.Vector2f(12f, 13f), .00001f);
        TeraAssert.assertEquals(o.v33, new org.joml.Vector4f(12, 12.2f, 3f, 15.5f), .00001f);
    }


    static class TestObject{
        public Vector3f v1;
        public Vector2f v2;
        public Vector4f v3;
        public org.joml.Vector3f v11;
        public org.joml.Vector2f v22;
        public org.joml.Vector4f v33;
    }
    static class TestObject1 {
        public org.joml.Vector3f v1;
        public org.joml.Vector2f v2;
        public org.joml.Vector4f v3;
    }

    static class TestObject2 {
        public Vector3fc v1;
        public Vector4fc v2;
        public Vector2fc v3;
    }


}
