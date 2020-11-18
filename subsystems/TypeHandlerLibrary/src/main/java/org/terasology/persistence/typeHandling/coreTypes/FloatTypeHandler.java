// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class FloatTypeHandler extends TypeHandler<Float> {

    @Override
    public PersistedData serializeNonNull(Float value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<Float> deserialize(PersistedData data) {
        if (data.isNumber()) {
            return Optional.of(data.getAsFloat());
        }
        return Optional.empty();
    }

}
