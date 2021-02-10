// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import gnu.trove.list.TFloatList;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class Vector2fcTypeHandler extends TypeHandler<Vector2fc> {
    @Override
    protected PersistedData serializeNonNull(Vector2fc value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y());
    }

    @Override
    public Optional<Vector2fc> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 1) {
                TFloatList floats = dataArray.getAsFloatArray();
                return Optional.of(new Vector2f(floats.get(0), floats.get(1)));
            }
        }
        return Optional.empty();
    }
}
