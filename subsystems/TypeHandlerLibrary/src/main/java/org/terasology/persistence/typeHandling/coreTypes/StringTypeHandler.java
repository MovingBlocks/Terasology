// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class StringTypeHandler extends TypeHandler<String> {

    @Override
    public PersistedData serializeNonNull(String value, PersistedDataSerializer serializer) {
        return serializer.serialize(value);
    }

    @Override
    public Optional<String> deserialize(PersistedData data) {
        if (data.isString()) {
            return Optional.ofNullable(data.getAsString());
        }
        return Optional.empty();
    }

}
