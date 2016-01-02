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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.DeserializationContext;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataArray;
import org.terasology.persistence.typeHandling.SerializationContext;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
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
    public PersistedData serialize(T value, SerializationContext context) {
        if (value != null) {
            return context.create(value.toString());
        }
        return context.createNull();
    }

    @Override
    public T deserialize(PersistedData data, DeserializationContext context) {
        if (data.isString()) {
            T result = caseInsensitiveLookup.get(data.getAsString().toLowerCase(Locale.ENGLISH));
            if (result == null) {
                logger.warn("Unknown enum value: '{}' for enum {}", data.getAsString(), enumType.getSimpleName());
            }
            return result;
        }
        return null;
    }

    @Override
    public PersistedData serializeCollection(Collection<T> value, SerializationContext context) {
        List<String> values = value.stream().map(T::toString).collect(Collectors.toCollection(ArrayList::new));
        return context.createStrings(values);
    }

    @Override
    public List<T> deserializeCollection(PersistedData data, DeserializationContext context) {
        if (data.isArray()) {
            PersistedDataArray array = data.getAsArray();
            List<T> result = Lists.newArrayListWithCapacity(array.size());
            for (PersistedData item : array) {
                result.add(deserialize(item, context));
            }
        }
        return Lists.newArrayList();
    }
}
