/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.identity.storageServiceClient;

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
