// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.serializers.gson.typehandler;

import gnu.trove.list.TFloatList;
import org.terasology.persistence.serializers.gson.models.TestVector4f;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Optional;

/**
 */
public class TestVector4fTypeHandler extends TypeHandler<TestVector4f> {

    @Override
    public PersistedData serializeNonNull(TestVector4f value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.x, value.y, value.z, value.w);
    }

    @Override
    public Optional<TestVector4f> deserialize(PersistedData data) {
        if (data.isArray()) {
            PersistedDataArray dataArray = data.getAsArray();
            if (dataArray.isNumberArray() && dataArray.size() > 3) {
                TFloatList floats = dataArray.getAsFloatArray();
                return Optional.of(new TestVector4f(floats.get(0), floats.get(1), floats.get(2), floats.get(3)));
            }
        }
        return Optional.empty();
    }
}
