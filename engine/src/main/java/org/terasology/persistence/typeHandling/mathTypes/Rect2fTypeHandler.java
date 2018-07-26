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
import org.terasology.math.geom.Rect2f;
import org.terasology.math.geom.Vector2f;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Map;

/**
 */
public class Rect2fTypeHandler implements TypeHandler<Rect2f> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    private TypeHandler<Vector2f> vector2fTypeHandler;

    public Rect2fTypeHandler(TypeHandler<Vector2f> vector2fTypeHandler) {
        this.vector2fTypeHandler = vector2fTypeHandler;
    }

    @Override
    public PersistedData serialize(Rect2f value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        } else {
            Map<String, PersistedData> map = Maps.newLinkedHashMap();

            map.put(MIN_FIELD, vector2fTypeHandler.serialize(value.min(), context));
            map.put(SIZE_FIELD, vector2fTypeHandler.serialize(value.size(), context));

            return context.create(map);
        }
    }

    @Override
    public Rect2f deserialize(PersistedData data) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();

            Vector2f min = vector2fTypeHandler.deserialize(map.get(MIN_FIELD));
            Vector2f size = vector2fTypeHandler.deserialize(map.get(SIZE_FIELD));

            return Rect2f.createFromMinAndSize(min, size);
        }
        return null;
    }

}
