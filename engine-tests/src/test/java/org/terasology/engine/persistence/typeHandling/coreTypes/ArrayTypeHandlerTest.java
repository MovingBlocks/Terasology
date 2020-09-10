// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.coreTypes;

import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.gson.GsonPersistedDataArray;
import org.terasology.nui.reflection.TypeInfo;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ArrayTypeHandlerTest {
    private static final int ARRAY_SIZE = 500;

    @Test
    public void testSerialize() {
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
    public void testDeserialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        ArrayTypeHandler<Integer> typeHandler = new ArrayTypeHandler<>(
                elementTypeHandler,
                TypeInfo.of(Integer.class)
        );

        JsonArray jsonArray = new JsonArray();

        for (Integer i : Collections.nCopies(ARRAY_SIZE, -1)) {
            jsonArray.add(i);
        }

        typeHandler.deserialize(new GsonPersistedDataArray(jsonArray));

        verify(elementTypeHandler, times(jsonArray.size())).deserialize(any());
    }
}
