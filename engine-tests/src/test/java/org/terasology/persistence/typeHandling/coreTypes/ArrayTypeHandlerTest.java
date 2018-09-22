/*
 * Copyright 2018 MovingBlocks
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

import com.google.common.collect.Queues;
import com.google.gson.JsonArray;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataArray;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ObjectConstructor;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
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
