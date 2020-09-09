// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.coreTypes;

import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;

import java.util.Optional;

public class ByteArrayTypeHandler extends org.terasology.engine.persistence.typeHandling.TypeHandler<byte[]> {
    @Override
    public PersistedData serializeNonNull(byte[] value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<byte[]> deserialize(PersistedData data) {
        if (data.isBytes()) {
            return Optional.of(data.getAsBytes());
        } else {
            return Optional.empty();
        }
    }
}
