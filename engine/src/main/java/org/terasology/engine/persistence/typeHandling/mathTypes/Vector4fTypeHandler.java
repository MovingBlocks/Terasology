// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.mathTypes;


import gnu.trove.list.TFloatList;
import org.joml.Vector4f;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataArray;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;

import java.util.Optional;

/**
 */
public class Vector4fTypeHandler extends org.terasology.engine.persistence.typeHandling.TypeHandler<Vector4f> {

    @Override
    public PersistedData serializeNonNull(Vector4f value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x, value.y, value.z, value.w);
    }

    @Override
    public Optional<Vector4f> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TFloatList floats = dataArray.getAsFloatArray();
                return Optional.of(new Vector4f(floats.get(0), floats.get(1), floats.get(2), floats.get(3)));
            }
        }
        return Optional.empty();
    }
}
