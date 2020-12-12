// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class LongTypeHandler extends TypeHandler<Long> {

    @Override
    public PersistedData serializeNonNull(Long value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<Long> deserialize(PersistedData data) {
        if (data.isNumber()) {
            return Optional.of(data.getAsLong());
        }
        return Optional.empty();
    }

}
