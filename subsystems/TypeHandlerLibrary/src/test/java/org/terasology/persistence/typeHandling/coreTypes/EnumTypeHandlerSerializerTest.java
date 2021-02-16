// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnumTypeHandlerSerializerTest {

    enum TestEnum {
        NON_NULL
    }

    @Test
    void testNullValue() throws Exception {
        PersistedData nullData = mock(PersistedData.class);
        when(nullData.isNull()).thenReturn(true);

        PersistedDataSerializer persistedDataSerializer = mock(PersistedDataSerializer.class);
        when(persistedDataSerializer.serializeNull()).thenReturn(nullData);
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<>(TestEnum.class);
        PersistedData serializedNull = handler.serialize(null, persistedDataSerializer);
        assertEquals(nullData, serializedNull);

        assertFalse(handler.deserialize(nullData).isPresent());
    }

    @Test
    void testNonNullValue() throws Exception {
        PersistedData data = mock(PersistedData.class);
        when(data.getAsString()).thenReturn(TestEnum.NON_NULL.toString());
        when(data.isString()).thenReturn(true);

        PersistedDataSerializer persistedDataSerializer = mock(PersistedDataSerializer.class);
        when(persistedDataSerializer.serialize(TestEnum.NON_NULL.toString())).thenReturn(data);
        EnumTypeHandler<TestEnum> handler = new EnumTypeHandler<>(TestEnum.class);
        PersistedData serializedNonNull = handler.serialize(TestEnum.NON_NULL, persistedDataSerializer);
        assertEquals(data, serializedNonNull);

        TestEnum deserializedValue = handler.deserialize(data).get();
        assertEquals(TestEnum.NON_NULL, deserializedValue);
    }
}
