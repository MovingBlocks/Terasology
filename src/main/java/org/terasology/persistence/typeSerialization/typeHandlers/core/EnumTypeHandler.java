/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeSerialization.typeHandlers.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeSerialization.typeHandlers.TypeHandler;
import org.terasology.protobuf.EntityData;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class EnumTypeHandler<T extends Enum> implements TypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(EnumTypeHandler.class);
    private Class<T> enumType;
    private Map<String, T> caseInsensitiveLookup = Maps.newHashMap();

    public EnumTypeHandler(Class<T> enumType) {
        this.enumType = enumType;
        for (T value : enumType.getEnumConstants()) {
            caseInsensitiveLookup.put(value.toString().toLowerCase(Locale.ENGLISH), value);
        }
    }

    @Override
    public EntityData.Value serialize(T value) {
        return EntityData.Value.newBuilder().addString(value.toString()).build();
    }

    @Override
    public T deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            T result = caseInsensitiveLookup.get(value.getString(0).toLowerCase(Locale.ENGLISH));
            if (result == null) {
                logger.warn("Unknown enum value: '{}' for enum {}", value.getString(0), enumType.getSimpleName());
            }
            return result;
        }
        return null;
    }

    @Override
    public EntityData.Value serializeCollection(Iterable<T> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (T item : value) {
            result.addString(item.toString());
        }
        return result.build();
    }

    @Override
    public List<T> deserializeCollection(EntityData.Value value) {
        List<T> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            T enumItem = caseInsensitiveLookup.get(item.toLowerCase(Locale.ENGLISH));
            if (enumItem == null) {
                logger.warn("Unknown enum value: '{}' for enum {}", item, enumType.getSimpleName());
            } else {
                result.add(enumItem);
            }
        }
        return result;
    }
}
