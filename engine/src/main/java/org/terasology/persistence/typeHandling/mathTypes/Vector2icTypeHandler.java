// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import gnu.trove.list.TIntList;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class Vector2icTypeHandler extends TypeHandler<Vector2ic> {
    @Override
    protected PersistedData serializeNonNull(Vector2ic value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y());
    }

    @Override
    public Optional<Vector2ic> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 1) {
                TIntList ints = dataArray.getAsIntegerArray();
                return Optional.of(new Vector2i(ints.get(0), ints.get(1)));
            }
        }
        return Optional.empty();
    }
}
