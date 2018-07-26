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
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.gson.GsonPersistedDataArray;
import org.terasology.reflection.reflect.ObjectConstructor;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class CollectionTypeHandlerTest {
    @Test
    public void testSerialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        ObjectConstructor<Collection<Integer>> constructor = Queues::newArrayDeque;

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        Collection<Integer> collection = constructor.construct();
        collection.addAll(Collections.nCopies(500, -1));

        SerializationContext context = mock(SerializationContext.class);

        typeHandler.serialize(collection, context);

        verify(elementTypeHandler, times(collection.size())).serialize(any(), any());

        verify(context).create(argThat(new ArgumentMatcher<Iterable<PersistedData>>() {
            @Override
            public boolean matches(Iterable<PersistedData> argument) {
                return argument instanceof Collection && ((Collection) argument).size() == collection.size();
            }
        }));
    }

    @Test
    public void testDeserialize() {
        IntTypeHandler elementTypeHandler = mock(IntTypeHandler.class);

        ObjectConstructor<Collection<Integer>> constructor = mock(ObjectConstructor.class);
        when(constructor.construct()).then((Answer<Collection<Integer>>) invocation -> Queues.newArrayDeque());

        CollectionTypeHandler<Integer> typeHandler = new CollectionTypeHandler<>(
                elementTypeHandler,
                constructor
        );

        JsonArray jsonArray = new JsonArray();

        for (Integer i : Collections.nCopies(500, -1)) {
            jsonArray.add(i);
        }

        typeHandler.deserialize(new GsonPersistedDataArray(jsonArray));

        verify(constructor).construct();

        verify(elementTypeHandler, times(jsonArray.size())).deserialize(any());
    }
}
