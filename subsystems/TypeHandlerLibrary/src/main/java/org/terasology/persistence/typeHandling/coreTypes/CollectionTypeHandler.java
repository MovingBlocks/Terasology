// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Lists;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.reflection.reflect.CollectionCopyConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class CollectionTypeHandler<E> extends TypeHandler<Collection<E>> {
    private TypeHandler<E> elementTypeHandler;
    private CollectionCopyConstructor<? extends Collection<E>, E> constructor;

    public CollectionTypeHandler(TypeHandler<E> elementTypeHandler,
                                 CollectionCopyConstructor<? extends Collection<E>, E> constructor) {
        this.elementTypeHandler = elementTypeHandler;
        this.constructor = constructor;
    }

    @Override
    public PersistedData serializeNonNull(Collection<E> value, PersistedDataSerializer serializer) {
        List<PersistedData> items = Lists.newArrayList();

        for (E element : value) {
            items.add(elementTypeHandler.serialize(element, serializer));
        }

        return serializer.serialize(items);
    }

    @Override
    public Optional<Collection<E>> deserialize(PersistedData data) {
        if (!data.isArray()) {
            return Optional.empty();
        }

        Collection<E> items = Lists.newArrayList();

        for (PersistedData item : data.getAsArray()) {
            Optional<E> element = elementTypeHandler.deserialize(item);
            element.ifPresent(items::add);
        }

        return Optional.of(constructor.construct(items));
    }
}
