// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.terasology.engine.ModuleEnvironmentTest;
import org.terasology.engine.core.module.ModuleContext;
import org.terasology.engine.persistence.typeHandling.TypeHandlerLibraryImpl;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataReader;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataWriter;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.serializers.Serializer;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.reflection.TypeInfo;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockAreaTypeHandlerTest extends ModuleEnvironmentTest {
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
    public void testGsonSerialization() throws IOException {
        TestObject a = new TestObject();
        a.b1 = new BlockArea(-1, -1, 0, 0);
        a.b2 = new BlockArea(0, 0, 1, 1);

        byte[] data = gsonSerializer.serialize(a, new TypeInfo<TestObject>() {
        }).get();

        TestObject o = gsonSerializer.deserialize(new TypeInfo<TestObject>() {
        }, data).get();
        assertEquals(new BlockArea(-1, -1, 0, 0), o.b1);
        assertEquals(new BlockArea(0, 0, 1, 1), o.b2);
    }

    public static class TestObject {
        public BlockArea b1;
        public BlockAreac b2;
    }
}
