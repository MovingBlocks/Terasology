// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.mathTypes;

import com.google.common.collect.Maps;
import gnu.trove.list.TIntList;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.util.Map;
import java.util.Optional;

public class BlockAreacTypeHandler extends TypeHandler<BlockAreac> {
    private static final String MIN_FIELD = "min";
    private static final String MAX_FIELD = "max";
    private static final String SIZE_FIELD = "size";

    @Override
    protected PersistedData serializeNonNull(BlockAreac value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();
        map.put(MIN_FIELD, serializer.serialize(value.minX(), value.minY()));
        map.put(MAX_FIELD, serializer.serialize(value.maxX(), value.maxY()));
        return serializer.serialize(map);
    }

    @Override
    public Optional<BlockAreac> deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            PersistedDataArray minDataArr = map.get(MIN_FIELD).getAsArray();
            TIntList minArr = minDataArr.getAsIntegerArray();
            if (map.has(SIZE_FIELD)) {
                PersistedDataArray sizedataArray = map.get(SIZE_FIELD).getAsArray();
                TIntList sizeArr = sizedataArray.getAsIntegerArray();
                return Optional.of(
                    new BlockArea(minArr.get(0), minArr.get(1))
                    		.setSize(sizeArr.get(0), sizeArr.get(1)));
            }

            PersistedDataArray maxDataArr = map.get(MAX_FIELD).getAsArray();
            TIntList maxArr = maxDataArr.getAsIntegerArray();
            return Optional.of(new BlockArea(minArr.get(0), minArr.get(1), maxArr.get(0), maxArr.get(1)));
        }
        return Optional.empty();
    }
}
