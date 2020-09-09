// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.common.collect.Maps;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataMap;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandler;

import java.util.Map;
import java.util.Optional;

/**
 */
public class Rect2iTypeHandler extends TypeHandler<Rect2i> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    private final TypeHandler<Vector2i> vector2iTypeHandler;

    public Rect2iTypeHandler(TypeHandler<Vector2i> vector2iTypeHandler) {
        this.vector2iTypeHandler = vector2iTypeHandler;
    }

    @Override
    public PersistedData serializeNonNull(Rect2i value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();

        map.put(MIN_FIELD, vector2iTypeHandler.serialize(value.min(), serializer));
        map.put(SIZE_FIELD, vector2iTypeHandler.serialize(value.size(), serializer));

        return serializer.serialize(map);
    }

    @Override
    public Optional<Rect2i> deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            Vector2i min = vector2iTypeHandler.deserializeOrThrow(map.get(MIN_FIELD),
                    "Could not deserialize Rect2i." + MIN_FIELD);

            Vector2i size = vector2iTypeHandler.deserializeOrThrow(map.get(SIZE_FIELD),
                    "Could not deserialize Rect2i." + SIZE_FIELD);

            return Optional.ofNullable(Rect2i.createFromMinAndSize(min, size));
        }
        return Optional.empty();
    }

}
