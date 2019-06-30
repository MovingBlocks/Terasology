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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.persistence.typeHandling.*;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.factories.CollectionTypeHandlerFactory;
import org.terasology.persistence.typeHandling.inMemory.PersistedMap;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

public class RuntimeDelegatingTypeHandlerTest {
    private final ConstructorLibrary constructorLibrary =
            new ConstructorLibrary(Maps.newHashMap());

    private final CollectionTypeHandlerFactory collectionHandlerFactory =
            new CollectionTypeHandlerFactory(constructorLibrary);

    private final TypeHandlerLibrary typeHandlerLibrary = mock(TypeHandlerLibrary.class);

    private final TypeHandlerContext context =
            new TypeHandlerContext(typeHandlerLibrary, new Reflections(getClass().getClassLoader()));

    private static class Base {
        int x;
    }

    private static class Sub extends Base {
        float y;
    }

    @Test
    public void testSerialize() {
        PersistedDataSerializer serializer = mock(PersistedDataSerializer.class);
        when(serializer.serialize(any(String.class)))
                .then(invocation -> new PersistedString((String) invocation.getArguments()[0]));

        Class<Sub> subType = Sub.class;
        Type baseType = TypeInfo.of(Base.class).getType();

        abstract class SubHandler extends TypeHandler<Sub> {}

        TypeHandler baseTypeHandler = mock(TypeHandler.class);
        TypeHandler<Sub> subTypeHandler = mock(SubHandler.class);

        when(typeHandlerLibrary.getTypeHandler(eq(baseType)))
                .thenReturn(Optional.of(baseTypeHandler));

        when(typeHandlerLibrary.getTypeHandler(eq((Type) subType)))
                .thenReturn(Optional.of(subTypeHandler));

        TypeHandler<List<Base>> listTypeHandler =
                collectionHandlerFactory.create(new TypeInfo<List<Base>>() {}, context).get();

        ArrayList<Base> bases = Lists.newArrayList(new Sub(), new Base(), new Sub(), new Base(), new Sub());
        listTypeHandler.serialize(bases, serializer);

        verify(typeHandlerLibrary).getTypeHandler(eq(baseType));
        verify(typeHandlerLibrary, times(3)).getTypeHandler(eq((Type) subType));

        verify(baseTypeHandler, times(2)).serialize(any(), any());
        verify(subTypeHandler, times(3)).serialize(any(), any());

        verify(serializer, times(3)).serialize(
                argThat((ArgumentMatcher<Map<String, PersistedData>>) argument ->
                        argument.get(RuntimeDelegatingTypeHandler.TYPE_FIELD)
                                .getAsString()
                                .equals(subType.getName()) &&
                                argument.containsKey(RuntimeDelegatingTypeHandler.VALUE_FIELD))
        );
    }

    @Test
    public void testDeserialize() {
        Type subType = Sub.class;
        Type baseType = TypeInfo.of(Base.class).getType();

        abstract class SubHandler extends TypeHandler<Sub> {}

        TypeHandler baseTypeHandler = mock(TypeHandler.class);
        TypeHandler<Sub> subTypeHandler = mock(SubHandler.class);

        when(typeHandlerLibrary.getTypeHandler(eq(baseType)))
                .thenReturn(Optional.of(baseTypeHandler));

        when(typeHandlerLibrary.getTypeHandler(eq(subType)))
                .thenReturn(Optional.of(subTypeHandler));

        TypeHandler<List<Base>> listTypeHandler = collectionHandlerFactory.create(
                new TypeInfo<List<Base>>() {}, context
        ).get();

        PersistedData persistedBase = new PersistedMap(ImmutableMap.of());

        PersistedData persistedSub = new PersistedMap(
                ImmutableMap.of(
                        RuntimeDelegatingTypeHandler.TYPE_FIELD,
                        new PersistedString(((Class<?>) subType).getName()),
                        RuntimeDelegatingTypeHandler.VALUE_FIELD,
                        new PersistedMap(ImmutableMap.of())
                )
        );

        PersistedDataArray persistedBases = mock(PersistedDataArray.class);
        when(persistedBases.isArray()).thenReturn(true);
        when(persistedBases.getAsArray()).thenReturn(persistedBases);
        when(persistedBases.iterator()).thenReturn(
                Lists.newArrayList(persistedSub, persistedBase, persistedSub, persistedBase, persistedSub).iterator()
        );

        listTypeHandler.deserialize(persistedBases);

        verify(typeHandlerLibrary).getTypeHandler(eq(baseType));
        verify(typeHandlerLibrary, times(3)).getTypeHandler(eq(subType));

        verify(baseTypeHandler, times(2)).deserialize(any());
        verify(subTypeHandler, times(3)).deserialize(any());
    }
}
