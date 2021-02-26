// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers;

import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.engine.module.ModuleContext;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;

import static org.terasology.joml.test.VectorAssert.assertEquals;

public class VectorTypeSerializerTest extends ModuleEnvironmentTest {
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
    private GsonSerializer gsonSerializer;

    @Override
    public void setup() {
        ModuleContext.setContext(moduleManager.getEnvironment().get(new Name("unittest")));

        typeHandlerLibrary = TypeHandlerLibraryImpl.forModuleEnvironment(moduleManager, typeRegistry);

        protobufSerializer = new ProtobufSerializer(typeHandlerLibrary);
        gsonSerializer = new GsonSerializer(typeHandlerLibrary);
    }

    @Test
    public void testSerializationConstant() throws IOException {
        TestObject2 a = new TestObject2();
        a.v1 = new Vector3f(1.0f, 2.0f, 3.0f);
        a.v2 = new Vector4f(1.0f, 2.0f, 3.0f, 5.0f);
        a.v3 = new Vector2f(1.0f, 2.0f);
        String data = gsonSerializer.toJson(a, new TypeInfo<TestObject2>() {
        });

        TestObject2 o = gsonSerializer.fromJson(data, new TypeInfo<TestObject2>() {
        });
        assertEquals(o.v1, new Vector3f(1.0f, 2.0f, 3.0f), .00001f);
        assertEquals(o.v2, new Vector4f(1.0f, 2.0f, 3.0f, 5.0f), .00001f);
        assertEquals(o.v3, new Vector2f(1.0f, 2.0f), .00001f);
    }

    @Test
    public void testJsonSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);

        String data = gsonSerializer.toJson(a, new TypeInfo<TestObject>() {
        });

        TestObject o = gsonSerializer.fromJson(data, new TypeInfo<TestObject>() {
        });

        assertEquals(o.v1, new Vector3f(11.5f, 13.15f, 3), .00001f);
        assertEquals(o.v2, new Vector2f(12f, 13f), .00001f);
        assertEquals(o.v3, new Vector4f(12, 12.2f, 3f, 15.5f), .00001f);
    }

    @Test
    public void testProtobufSerialize() throws IOException {
        TestObject a = new TestObject();
        a.v1 = new Vector3f(11.5f, 13.15f, 3);
        a.v2 = new Vector2f(12, 13f);
        a.v3 = new Vector4f(12, 12.2f, 3f, 15.5f);

        byte[] bytes = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(bytes, new TypeInfo<TestObject>() {
        });

        assertEquals(o.v1, new Vector3f(11.5f, 13.15f, 3), .00001f);
        assertEquals(o.v2, new Vector2f(12f, 13f), .00001f);
        assertEquals(o.v3, new Vector4f(12, 12.2f, 3f, 15.5f), .00001f);
    }
}
