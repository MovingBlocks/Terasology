/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.persistence.typeHandling.mathTypes;

import com.google.common.collect.Maps;

import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Map;

/**
 */
public class Rect2iTypeHandler extends TypeHandler<Rect2i> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    private TypeHandler<Vector2i> vector2iTypeHandler;

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
    public Rect2i deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            Vector2i min = vector2iTypeHandler.deserialize(map.get(MIN_FIELD));
            Vector2i size = vector2iTypeHandler.deserialize(map.get(SIZE_FIELD));

            return Rect2i.createFromMinAndSize(min, size);
        }
        return null;
    }

}
