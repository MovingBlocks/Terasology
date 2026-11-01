// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedIntegerArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedValueArray;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ArrayTypeHandlerTest {
    private static final int ARRAY_SIZE = 500;

    @Test
    void testSerialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        ArrayTypeHandler<Integer> typeHandler = new ArrayTypeHandler<>(
                elementTypeHandler,
                TypeInfo.of(Integer.class)
        );

        Integer[] array = new Integer[ARRAY_SIZE];
        final int[] c = {0};
        Collections.nCopies(array.length, -1).forEach(i -> array[c[0]++] = i);

        PersistedDataSerializer context = mock(PersistedDataSerializer.class);

        typeHandler.serialize(array, context);

        verify(elementTypeHandler, times(array.length)).serialize(any(), any());

        verify(context).serialize(argThat((ArgumentMatcher<Iterable<PersistedData>>) argument ->
                argument instanceof Collection && ((Collection) argument).size() == array.length));
    }

    @Test
    void testDeserialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        ArrayTypeHandler<Integer> typeHandler = new ArrayTypeHandler<>(
                elementTypeHandler,
                TypeInfo.of(Integer.class)
        );

        TIntList intList = new TIntArrayList(ARRAY_SIZE);

        for (Integer i : Collections.nCopies(ARRAY_SIZE, -1)) {
            intList.add(i);
        }

        typeHandler.deserialize(new PersistedIntegerArray(intList));

        verify(elementTypeHandler, times(intList.size())).deserialize(any());
    }

    @Test
    @DisplayName("An empty array encoded as '[]' can be deserialized successfully.")
    void testDeserializeEmpty() {
        ArrayTypeHandler<String> typeHandler = new ArrayTypeHandler<>(
                new StringTypeHandler(),
                TypeInfo.of(String.class)
        );

        var testData = new PersistedValueArray(List.of());

        var result = typeHandler.deserialize(testData);

        assertThat(result).isPresent();
        assertThat(Array.getLength(result.get())).isEqualTo(0);
    }
}
