// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class CharacterTypeHandler extends TypeHandler<Character> {

    @Override
    protected PersistedData serializeNonNull(Character value, PersistedDataSerializer serializer) {
        // converts value to string since char is not a JSON primitive type
        return serializer.serialize(Character.toString(value));
    }

    @Override
    public Optional<Character> deserialize(PersistedData data) {
        if (data.isString()) {
            // returns the character that was serialized as string
            return Optional.of(data.getAsString().charAt(0));
        }
        return Optional.empty();
    }
}
