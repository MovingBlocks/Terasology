// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class DoubleTypeHandler extends TypeHandler<Double> {

    @Override
    public PersistedData serializeNonNull(Double value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<Double> deserialize(PersistedData data) {
        if (data.isNumber()) {
            return Optional.of(data.getAsDouble());
        }
        return Optional.empty();
    }

}
