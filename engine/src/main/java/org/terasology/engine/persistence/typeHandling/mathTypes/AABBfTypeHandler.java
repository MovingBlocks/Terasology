// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.common.collect.Maps;
import gnu.trove.list.TFloatList;
import org.terasology.joml.geom.AABBf;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Map;
import java.util.Optional;

public class AABBfTypeHandler extends TypeHandler<AABBf> {
    private static final String MIN_FIELD = "min";
    private static final String MAX_FIELD = "max";

    @Override
    protected PersistedData serializeNonNull(AABBf value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();
        map.put(MIN_FIELD, serializer.serialize(value.minX, value.minY, value.minZ));
        map.put(MAX_FIELD, serializer.serialize(value.maxX, value.maxY, value.maxZ));
        return serializer.serialize(map);
    }

    @Override
    public Optional<AABBf> deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            PersistedDataArray minDataArr = map.get(MIN_FIELD).getAsArray();
            PersistedDataArray maxDataArr = map.get(MAX_FIELD).getAsArray();

            TFloatList minArr = minDataArr.getAsFloatArray();
            TFloatList maxArr = maxDataArr.getAsFloatArray();

            return Optional.of(new AABBf(minArr.get(0), minArr.get(1), minArr.get(2), maxArr.get(0), maxArr.get(1),
                maxArr.get(2)));
        }
        return Optional.empty();
    }
}
