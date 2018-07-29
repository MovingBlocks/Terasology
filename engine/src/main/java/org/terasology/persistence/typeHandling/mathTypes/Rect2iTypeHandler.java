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
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.SerializationContext;

import java.util.Map;

/**
 */
public class Rect2iTypeHandler implements org.terasology.persistence.typeHandling.TypeHandler<Rect2i> {

    private static final String MIN_FIELD = "min";
    private static final String SIZE_FIELD = "size";

    @Override
    public PersistedData serialize(Rect2i value, SerializationContext context) {
        if (value == null) {
            return context.createNull();
        } else {
            Map<String, PersistedData> map = Maps.newLinkedHashMap();
            map.put(MIN_FIELD, context.create(value.min(), Vector2i.class));
            map.put(SIZE_FIELD, context.create(value.size(), Vector2i.class));
            return context.create(map);
        }
    }

    @Override
    public Rect2i deserialize(PersistedData data, DeserializationContext context) {
        if (!data.isNull() && data.isValueMap()) {
            PersistedDataMap map = data.getAsValueMap();
            Vector2i min = context.deserializeAs(map.get(MIN_FIELD), Vector2i.class);
            Vector2i size = context.deserializeAs(map.get(SIZE_FIELD), Vector2i.class);
            return Rect2i.createFromMinAndSize(min, size);
        }
        return null;
    }

}
