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
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.SimpleTypeHandler;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Map;

/**
 */
public class StringMapTypeHandler<T> extends SimpleTypeHandler<Map<String, T>> {

    private TypeHandler<T> contentsHandler;

    public StringMapTypeHandler(TypeHandler contentsHandler) {
        this.contentsHandler = contentsHandler;
    }

    @Override
    public PersistedData serialize(Map<String, T> value, SerializationContext context) {
        Map<String, PersistedData> map = Maps.newLinkedHashMap();
        for (Map.Entry<String, T> entry : value.entrySet()) {
            PersistedData item = contentsHandler.serialize(entry.getValue(), context);
            if (!item.isNull()) {
                map.put(entry.getKey(), item);
            }
        }
        return context.create(map);
    }

    @Override
    public Map<String, T> deserialize(PersistedData data, DeserializationContext context) {
        Map<String, T> result = Maps.newLinkedHashMap();
        if (data.isValueMap()) {
            for (Map.Entry<String, PersistedData> item : data.getAsValueMap().entrySet()) {
                result.put(item.getKey(), contentsHandler.deserialize(item.getValue(), context));
            }
        }
        return result;
    }
}
