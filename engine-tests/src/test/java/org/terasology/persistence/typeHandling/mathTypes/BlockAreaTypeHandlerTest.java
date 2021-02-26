// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.engine.module.ModuleContext;
import org.terasology.naming.Name;
import org.terasology.persistence.serializers.ProtobufSerializer;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.reflection.TypeInfo;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAreaTypeHandlerTest extends ModuleEnvironmentTest {

    static class TestObject {
        public BlockArea b1;
        public BlockAreac b2;
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
    public void testGsonSerialization() throws IOException {
        TestObject a = new TestObject();
        a.b1 = new BlockArea(-1, -1, 0, 0);
        a.b2 = new BlockArea(0, 0, 1, 1);

        byte[] data = gsonSerializer.serialize(a, new TypeInfo<TestObject>() {
        }).get();

        TestObject o = gsonSerializer.deserialize(new TypeInfo<TestObject>() {
        },data).get();
        assertEquals(o.b1, new BlockArea(-1, -1, 0, 0));
        assertEquals(o.b2, new BlockArea(0, 0, 1, 1));
    }

    @Test
    public void testProtobufSerialize() throws IOException {
        TestObject a = new TestObject();
        a.b1 = new BlockArea(-1, -1, 0, 0);
        a.b2 = new BlockArea(0, 0, 1, 1);

        byte[] data = protobufSerializer.toBytes(a, new TypeInfo<TestObject>() {
        });

        TestObject o = protobufSerializer.fromBytes(data, new TypeInfo<TestObject>() {
        });
        assertEquals(o.b1, new BlockArea(-1, -1, 0, 0));
        assertEquals(o.b2, new BlockArea(0, 0, 1, 1));
    }

}
