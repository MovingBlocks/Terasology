// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.mathTypes;

import com.google.common.collect.Maps;
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.engine.persistence.typeHandling.PersistedData;
import org.terasology.engine.persistence.typeHandling.PersistedDataMap;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.TypeHandler;

import java.util.Map;
import java.util.Optional;

/**
 */
public class Rect2fTypeHandler extends TypeHandler<Rect2f> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    private final TypeHandler<Vector2f> vector2fTypeHandler;

    public Rect2fTypeHandler(TypeHandler<Vector2f> vector2fTypeHandler) {
        this.vector2fTypeHandler = vector2fTypeHandler;
    }

    @Override
    public PersistedData serializeNonNull(Rect2f value, PersistedDataSerializer serializer) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();

        map.put(MIN_FIELD, vector2fTypeHandler.serialize(value.min(), serializer));
        map.put(SIZE_FIELD, vector2fTypeHandler.serialize(value.size(), serializer));

        return serializer.serialize(map);
    }

    @Override
    public Optional<Rect2f> deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            Vector2f min = vector2fTypeHandler.deserializeOrThrow(map.get(MIN_FIELD),
                    "Could not deserialize Rect2f." + MIN_FIELD);

            Vector2f size = vector2fTypeHandler.deserializeOrThrow(map.get(SIZE_FIELD),
                    "Could not deserialize Rect2f." + SIZE_FIELD);

            return Optional.ofNullable(Rect2f.createFromMinAndSize(min, size));
        }
        return Optional.empty();
    }

}
