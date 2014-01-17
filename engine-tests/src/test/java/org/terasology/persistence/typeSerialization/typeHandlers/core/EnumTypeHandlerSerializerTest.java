/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.persistence.typeSerialization.typeHandlers.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.terasology.protobuf.EntityData.Value;

/**
 * @author mkienenb
 */
public class EnumTypeHandlerSerializerTest {

    enum TestEnum {
        NON_NULL
    };

    @Test
    public void testNullValue() throws Exception {
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<TestEnum>(TestEnum.class);
        Value serializedNull = handler.serialize(null);
        assertNull(serializedNull);
        TestEnum deserializedValue = handler.deserialize(serializedNull);
        assertEquals(null, deserializedValue);
    }

    @Test
    public void testNonNullValue() throws Exception {
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<TestEnum>(TestEnum.class);
        Value serializedNonNull = handler.serialize(TestEnum.NON_NULL);
        assertNotNull(serializedNonNull);
        TestEnum deserializedValue = handler.deserialize(serializedNonNull);
        assertEquals(TestEnum.NON_NULL, deserializedValue);
    }
}
