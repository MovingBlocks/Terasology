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
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.TypeInfo;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serializes arrays of type {@code E[]}.
 *
 * {@link ArrayTypeHandler} extends {@link TypeHandler<Object>} because the type parameter {@link E}
 * supports only wrapper types, and primitive array to wrapper type array (and vice versa) casts are
 * unsupported. The array is accessed via the {@link Array} utility class as an {@link Object} so that
 * the cast can be avoided.
 *
 * @param <E> The type of an element in the array to serialize.
 */
public class ArrayTypeHandler<E> extends TypeHandler<Object> {
    private TypeHandler<E> elementTypeHandler;
    private TypeInfo<E> elementType;

    public ArrayTypeHandler(TypeHandler<E> elementTypeHandler, TypeInfo<E> elementType) {
        this.elementTypeHandler = elementTypeHandler;
        this.elementType = elementType;
    }

    @Override
    protected PersistedData serializeNonNull(Object value, PersistedDataSerializer serializer) {
        List<PersistedData> items = Lists.newArrayList();

        for (int i = 0; i < Array.getLength(value); i++) {
            E element = (E) Array.get(value, i);
            items.add(elementTypeHandler.serialize(element, serializer));
        }

        return serializer.serialize(items);
    }

    @Override
    public Optional<Object> deserialize(PersistedData data) {
        if (!data.isArray()) {
            return Optional.empty();
        }

        @SuppressWarnings({"unchecked"})
        List<E> items = data.getAsArray().getAsValueArray().stream()
                .map(itemData -> elementTypeHandler.deserialize(itemData))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        Object array = Array.newInstance(elementType.getRawType(), items.size());

        for (int i = 0; i < items.size(); i++) {
            Array.set(array, i, items.get(i));
        }

        return Optional.of(array);
    }
}
