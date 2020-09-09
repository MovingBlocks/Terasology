// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.coreTypes;

import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class NumberTypeHandler extends TypeHandler<Number> {

    @Override
    public PersistedData serializeNonNull(Number value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.doubleValue());
    }

    @Override
    public Optional<Number> deserialize(PersistedData data) {
        if (data.isNumber()) {
            return Optional.of(data.getAsDouble());
        }
        return Optional.empty();
    }

}
