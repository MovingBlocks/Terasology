// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import java.util.Optional;

class FutureTypeHandler<T> extends TypeHandler<T> {
    public TypeHandler<T> typeHandler;

    private void throwIfTypeHandlerNull() {
        if (typeHandler == null) {
            throw new SerializationException("Future TypeHandler has not been generated yet");
        }
    }

    @Override
    public PersistedData serialize(T value, PersistedDataSerializer serializer) {
        throwIfTypeHandlerNull();
        return typeHandler.serialize(value, serializer);
    }

    @Override
    protected PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        throwIfTypeHandlerNull();
        return typeHandler.serializeNonNull(value, serializer);
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        throwIfTypeHandlerNull();
        return typeHandler.deserialize(data);
    }
}
