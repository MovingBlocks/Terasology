// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Queues;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.stubbing.Answer;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.arrays.PersistedIntegerArray;
import org.terasology.reflection.reflect.ObjectConstructor;

import java.util.Collection;
import java.util.Collections;

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

        ObjectConstructor<Collection<Integer>> constructor = Queues::newArrayDeque;

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        Collection<Integer> collection = constructor.construct();
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

        ObjectConstructor<Collection<Integer>> constructor = mock(ObjectConstructor.class);
        when(constructor.construct()).then((Answer<Collection<Integer>>) invocation -> Queues.newArrayDeque());

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        TIntList intList = new TIntArrayList();

        for (Integer i : Collections.nCopies(500, -1)) {
            intList.add(i);
        }

        typeHandler.deserialize(new PersistedIntegerArray(intList));

        verify(constructor).construct();

        verify(elementTypeHandler, times(intList.size())).deserialize(any());
    }
}
