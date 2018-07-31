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

/**
 * Serializes arrays of type {@code E[]}.
 *
 * @param <E> The type of an element in the array to serialize.
 */
public class ArrayTypeHandler<E> extends TypeHandler<E[]> {
    private TypeHandler<E> elementTypeHandler;
    private TypeInfo<E> elementType;

    public ArrayTypeHandler(TypeHandler<E> elementTypeHandler, TypeInfo<E> elementType) {
        this.elementTypeHandler = elementTypeHandler;
        this.elementType = elementType;
    }

    @Override
    protected PersistedData serializeNonNull(E[] value, PersistedDataSerializer serializer) {
        List<PersistedData> items = Lists.newArrayList();

        for (E element : value) {
            items.add(elementTypeHandler.serialize(element, serializer));
        }

        return serializer.serialize(items);
    }

    @Override
    public Optional<E[]> deserialize(PersistedData data) {
        if (!data.isArray()) {
            return Optional.empty();
        }

        @SuppressWarnings({"unchecked"})
        E[] array = data.getAsArray().getAsValueArray().stream()
                .map(itemData -> elementTypeHandler.deserialize(itemData))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toArray(size -> (E[]) Array.newInstance(elementType.getRawType(), size));

        return Optional.of(array);
    }
}
