// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedIntegerArray;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedValueArray;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.CollectionCopyConstructor;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CollectionTypeHandlerTest {
    @Test
    void testSerialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        CollectionCopyConstructor<Queue<Integer>, Integer> constructor = Queues::newArrayDeque;

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        Collection<Integer> collection = constructor.construct(Lists.newArrayList());
        collection.addAll(Collections.nCopies(500, -1));

        PersistedDataSerializer context = mock(PersistedDataSerializer.class);

        typeHandler.serialize(collection, context);

        verify(elementTypeHandler, times(collection.size())).serialize(any(), any());

        verify(context).serialize(argThat(new ArgumentMatcher<Iterable<PersistedData>>() {
            @Override
            public boolean matches(Iterable<PersistedData> argument) {
                return argument instanceof Collection && ((Collection) argument).size() == collection.size();
            }
        }));
    }

    @Test
    void testDeserialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        CollectionCopyConstructor<Collection<Integer>, Integer> constructor = mock(CollectionCopyConstructor.class);
        when(constructor.construct(Lists.newArrayList())).then((Answer<Collection<Integer>>) invocation -> Queues.newArrayDeque());

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        TIntList intList = new TIntArrayList();

        for (Integer i : Collections.nCopies(500, -1)) {
            intList.add(i);
        }

        typeHandler.deserialize(new PersistedIntegerArray(intList));

        verify(constructor).construct(Lists.newArrayList());

        verify(elementTypeHandler, times(intList.size())).deserialize(any());
    }

    @Test
    @DisplayName("An empty collection encoded as '[]' can be deserialized successfully.")
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
