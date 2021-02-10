// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import gnu.trove.list.TFloatList;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

public class QuaternionfcTypeHandler extends TypeHandler<Quaternionfc> {

    @Override
    public PersistedData serializeNonNull(Quaternionfc value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x(), value.y(), value.z(), value.w());
    }

    @Override
    public Optional<Quaternionfc> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TFloatList floats = dataArray.getAsFloatArray();
                return Optional.of(new Quaternionf(floats.get(0), floats.get(1), floats.get(2), floats.get(3)));
            }
        }
        return Optional.empty();
    }
}
