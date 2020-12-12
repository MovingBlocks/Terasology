// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import java.util.Optional;

/**
 */
public abstract class StringRepresentationTypeHandler<T> extends TypeHandler<T> {

    public abstract String getAsString(T item);

    public abstract T getFromString(String representation);

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        String stringValue = getAsString(value);
        return serializer.serialize(stringValue);
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (data.isString()) {
            return Optional.ofNullable(getFromString(data.getAsString()));
        }
        return Optional.empty();
    }

}
