// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class BooleanTypeHandler extends TypeHandler<Boolean> {

    @Override
    public PersistedData serializeNonNull(Boolean value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<Boolean> deserialize(PersistedData data) {
        if (data.isBoolean()) {
            return Optional.of(data.getAsBoolean());
        }
        return Optional.empty();
    }

}
