// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.identity.storageServiceClient;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BigIntegerBase64SerializerTest {

    @Test
    public void testSerializer() {
        BigIntegerBase64Serializer serializer = new BigIntegerBase64Serializer();
        BigInteger data = new BigInteger("123456789123456789123456789123456789");
        JsonElement serialized = serializer.serialize(data, null, null);
        assertTrue(serialized.isJsonPrimitive());
        assertTrue(((JsonPrimitive) serialized).isString());
        assertEquals(data, serializer.deserialize(serialized, null, null));
    }
}
