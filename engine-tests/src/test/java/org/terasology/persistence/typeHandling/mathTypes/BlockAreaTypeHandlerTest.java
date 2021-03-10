// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import org.junit.jupiter.api.Test;
import org.terasology.ModuleEnvironmentTest;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.persistence.serializers.GsonSerializer;
import org.terasology.engine.persistence.serializers.ProtobufSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.naming.Name;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAreaTypeHandlerTest extends ModuleEnvironmentTest {

    static class TestObject {
        public BlockArea b1;
        public BlockAreac b2;
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
    public void testGsonSerialization() throws IOException {
        TestObject a = new TestObject();
        a.b1 = new BlockArea(-1, -1, 0, 0);
        a.b2 = new BlockArea(0, 0, 1, 1);

        String data = gsonSerializer.toJson(a, new TypeInfo<TestObject>() {
        });

        TestObject o = gsonSerializer.fromJson(data, new TypeInfo<TestObject>() {
        });
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
