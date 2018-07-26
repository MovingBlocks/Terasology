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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
import org.terasology.persistence.typeHandling.coreTypes.factories.CollectionTypeHandlerFactory;
import org.terasology.reflection.TypeInfo;
import org.terasology.reflection.reflect.ConstructorLibrary;
import org.terasology.reflection.reflect.ReflectionReflectFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class RuntimeDelegatingTypeHandlerTest {
    private final ConstructorLibrary constructorLibrary =
            new ConstructorLibrary(Maps.newHashMap());

    private final CollectionTypeHandlerFactory collectionHandlerFactory =
            new CollectionTypeHandlerFactory(constructorLibrary);

    private final TypeSerializationLibrary typeSerializationLibrary = mock(TypeSerializationLibrary.class);

    private static class Base {
        int x;
    }

    private static class Sub extends Base {
        float y;
    }

    @Test
    public void testSerialize() {
        PersistedDataSerializer context = mock(PersistedDataSerializer.class);

        Class<Sub> subType = Sub.class;
        Type baseType = TypeInfo.of(Base.class).getType();

        TypeHandler baseTypeHandler = mock(TypeHandler.class);
        TypeHandler<Sub> subTypeHandler = mock(TypeHandler.class);

        when(typeSerializationLibrary.getTypeHandler(eq(baseType))).thenReturn(baseTypeHandler);
        when(typeSerializationLibrary.getTypeHandler(eq(subType))).thenReturn(subTypeHandler);

        TypeHandler<List<Base>> listTypeHandler = collectionHandlerFactory.create(new TypeInfo<List<Base>>() {
        }, typeSerializationLibrary).get();

        ArrayList<Base> bases = Lists.newArrayList(new Sub(), new Base(), new Sub(), new Base(), new Sub());
        listTypeHandler.serialize(bases, context);

        verify(typeSerializationLibrary).getTypeHandler(baseType);
        verify(typeSerializationLibrary, times(3)).getTypeHandler(subType);

        verify(baseTypeHandler, times(2)).serialize(any(), any());
        verify(subTypeHandler, times(3)).serialize(any(), any());
    }

    @Test
    public void testDeserialize() {
    }
}
