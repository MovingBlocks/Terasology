// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import gnu.trove.list.TIntList;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class Vector3icTypeHandler extends TypeHandler<Vector3ic> {
    @Override
    protected PersistedData serializeNonNull(Vector3ic value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y(), value.z());
    }

    @Override
    public Optional<Vector3ic> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 2) {
                TIntList ints = dataArray.getAsIntegerArray();
                return Optional.of(new Vector3i(ints.get(0), ints.get(1), ints.get(2)));
            }
        }
        return Optional.empty();
    }
}
