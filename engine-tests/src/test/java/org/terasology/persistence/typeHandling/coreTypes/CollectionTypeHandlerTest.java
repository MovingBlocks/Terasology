// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Queues;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataArray;
import org.terasology.reflection.reflect.CollectionCopyConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class CollectionTypeHandlerTest {
    @Test
    public void testSerialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        CollectionCopyConstructor<Collection<Integer>, Integer> ctor = Queues::newArrayDeque;
        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                ctor
        );

        Collection<Integer> collection = ctor.construct(Collections.nCopies(500, -1));

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
    public void testDeserialize() {

        IntTypeHandler elementTypeHandler = spy(new IntTypeHandler());

        CollectionCopyConstructor<Collection<Integer>, Integer> constructor =
                spy(new CollectionCopyConstructor<Collection<Integer>, Integer>() {
                    @Override
                    public Collection<Integer> construct(Collection<Integer> elements) {
                        return Queues.newArrayDeque(elements);
                    }
                });

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        List<Integer> integers = Collections.nCopies(500, -1);
        JsonArray jsonArray = new JsonArray();

        for (Integer i : integers) {
            jsonArray.add(i);
        }

        typeHandler.deserialize(new GsonPersistedDataArray(jsonArray));

        verify(constructor).construct(eq(integers));

        verify(elementTypeHandler, times(jsonArray.size())).deserialize(any());
    }
}
