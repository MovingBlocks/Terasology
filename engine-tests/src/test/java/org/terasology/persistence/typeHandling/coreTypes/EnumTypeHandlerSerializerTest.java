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
package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.Test;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 */
public class EnumTypeHandlerSerializerTest {

    enum TestEnum {
        NON_NULL
    }

    @Test
    public void testNullValue() throws Exception {
        PersistedData nullData = mock(PersistedData.class);
        when(nullData.isNull()).thenReturn(true);

        PersistedDataSerializer persistedDataSerializer = mock(PersistedDataSerializer.class);
        when(persistedDataSerializer.serializeNull()).thenReturn(nullData);
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<>(TestEnum.class);
        PersistedData serializedNull = handler.serialize(null, persistedDataSerializer);
        assertEquals(nullData, serializedNull);

        TestEnum deserializedValue = handler.deserialize(nullData);
        assertEquals(null, deserializedValue);
    }

    @Test
    public void testNonNullValue() throws Exception {
        PersistedData data = mock(PersistedData.class);
        when(data.getAsString()).thenReturn(TestEnum.NON_NULL.toString());
        when(data.isString()).thenReturn(true);

        PersistedDataSerializer persistedDataSerializer = mock(PersistedDataSerializer.class);
        when(persistedDataSerializer.serialize(TestEnum.NON_NULL.toString())).thenReturn(data);
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<>(TestEnum.class);
        PersistedData serializedNonNull = handler.serialize(TestEnum.NON_NULL, persistedDataSerializer);
        assertEquals(data, serializedNonNull);

        TestEnum deserializedValue = handler.deserialize(data);
        assertEquals(TestEnum.NON_NULL, deserializedValue);
    }
}
