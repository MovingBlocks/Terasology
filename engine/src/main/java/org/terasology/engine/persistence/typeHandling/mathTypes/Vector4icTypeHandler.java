// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import gnu.trove.list.TIntList;
import org.joml.Vector4i;
import org.joml.Vector4ic;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class Vector4icTypeHandler extends TypeHandler<Vector4ic> {
    @Override
    protected PersistedData serializeNonNull(Vector4ic value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y(), value.z(), value.w());
    }

    @Override
    public Optional<Vector4ic> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TIntList ints = dataArray.getAsIntegerArray();
                return Optional.of(new Vector4i(ints.get(0), ints.get(1), ints.get(2), ints.get(3)));
            }
        }
        return Optional.empty();
    }
}
