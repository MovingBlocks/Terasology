// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes.factories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteArrayTypeHandler;
import org.terasology.persistence.typeHandling.coreTypes.ByteTypeHandler;
import org.terasology.persistence.typeHandling.inMemory.InMemoryPersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.PersistedBytes;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;

class BytesTypeHandlerTest {

    @Test
    void byteSerializeDeserialize() {
        byte expectedObj = (byte) 0xFF;

        PersistedBytes data = serialize(expectedObj, new ByteTypeHandler());
        Assertions.assertEquals(expectedObj, data.getAsBytes()[0]);

        byte obj = deserialize(data, new ByteTypeHandler());
        Assertions.assertEquals(expectedObj, obj);
    }

    @Test
    void intDeserializeAsByte() {
        byte expectedObj = (byte) 0xFF;

        byte obj = deserialize(new PersistedInteger(expectedObj), new ByteTypeHandler());
        Assertions.assertEquals(expectedObj, obj);
    }

    @Test
    void byteArraySerializeDeserialize() {
        byte[] expectedObj = new byte[]{(byte) 0xFF};

        PersistedBytes data = serialize(expectedObj, new ByteArrayTypeHandler());
        Assertions.assertArrayEquals(expectedObj, data.getAsBytes());

        byte[] obj = deserialize(data, new ByteArrayTypeHandler());
        Assertions.assertArrayEquals(expectedObj, obj);
    }

    private <R extends PersistedData, T> R serialize(T obj, TypeHandler<T> typeHandler) {
        return (R) typeHandler.serialize(obj,
                new InMemoryPersistedDataSerializer());
    }

    private <R extends PersistedData, T> T deserialize(R data, TypeHandler<T> typeHandler) {
        return typeHandler.deserializeOrNull(data);
    }
}
