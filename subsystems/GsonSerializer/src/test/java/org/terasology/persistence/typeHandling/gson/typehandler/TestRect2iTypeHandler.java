// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.gson.typehandler;

import com.google.common.collect.Maps;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.gson.models.TestRect2i;

import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class TestRect2iTypeHandler extends TypeHandler<TestRect2i> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    @Override
    public PersistedData serializeNonNull(TestRect2i value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();

        map.put(MIN_FIELD, serializer.serialize(value.getMinX(), value.getMinY()));
        map.put(SIZE_FIELD, serializer.serialize(value.getSizeX(), value.getSizeY()));

        return serializer.serialize(map);
    }

    @Override
    public Optional<TestRect2i> deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            int[] min = map.get(MIN_FIELD).getAsArray().getAsIntegerArray().toArray();

            int[] size = map.get(SIZE_FIELD).getAsArray().getAsIntegerArray().toArray();

            return Optional.of(new TestRect2i(min[0], min[1], size[0], size[1]));
        }
        return Optional.empty();
    }

}
