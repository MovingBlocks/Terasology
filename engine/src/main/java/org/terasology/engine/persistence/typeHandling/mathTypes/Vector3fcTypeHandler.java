// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import gnu.trove.list.TFloatList;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class Vector3fcTypeHandler extends TypeHandler<Vector3fc> {
    @Override
    protected PersistedData serializeNonNull(Vector3fc value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y(), value.z());
    }

    @Override
    public Optional<Vector3fc> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 2) {
                TFloatList floats = dataArray.getAsFloatArray();
                return Optional.of(new Vector3f(floats.get(0), floats.get(1), floats.get(2)));
            }
        }
        return Optional.empty();
    }
}
