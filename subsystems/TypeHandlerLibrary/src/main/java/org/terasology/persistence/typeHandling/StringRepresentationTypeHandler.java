// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import java.util.Optional;

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
            String value = data.getAsString();
            if (value == null) {
                // A PersistedData can report isString() true while still holding no actual
                // content (e.g. a stale/renamed reference in older save data) - treat that the
                // same as "absent" instead of forwarding null into getFromString(), which for
                // handlers like ComponentClassTypeHandler ends up constructing a SimpleUri(null).
                return Optional.empty();
            }
            return Optional.ofNullable(getFromString(value));
        }
        return Optional.empty();
    }

}
