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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Locale;
import java.util.Map;

/**
 */
public class EnumTypeHandler<T extends Enum> extends TypeHandler<T> {

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
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        if (value != null) {
            return serializer.serialize(value.toString());
        }
        return serializer.serializeNull();
    }

    @Override
    public T deserialize(PersistedData data) {
        if (data.isString()) {
            T result = caseInsensitiveLookup.get(data.getAsString().toLowerCase(Locale.ENGLISH));
            if (result == null) {
                logger.warn("Unknown enum value: '{}' for enum {}", data.getAsString(), enumType.getSimpleName());
            }
            return result;
        }
        return null;
    }

}
