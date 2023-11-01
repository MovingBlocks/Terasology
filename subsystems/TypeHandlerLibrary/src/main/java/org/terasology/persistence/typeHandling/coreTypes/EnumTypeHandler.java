// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class EnumTypeHandler<T extends Enum> extends TypeHandler<T> {

    private static final Logger logger = LoggerFactory.getLogger(EnumTypeHandler.class);
    private final Class<T> enumType;
    private Map<String, T> caseInsensitiveLookup = Maps.newHashMap();

    public EnumTypeHandler(Class<T> enumType) {
        this.enumType = enumType;
        for (T value : enumType.getEnumConstants()) {
            caseInsensitiveLookup.put(value.toString().toLowerCase(Locale.ENGLISH), value);
        }
    }

    @Override
    public PersistedData serializeNonNull(T value, PersistedDataSerializer serializer) {
        return serializer.serialize(value.toString());
    }

    @Override
    public Optional<T> deserialize(PersistedData data) {
        if (data.isString()) {
            T result = caseInsensitiveLookup.get(data.getAsString().toLowerCase(Locale.ENGLISH));
            if (result == null) {
                logger.warn("Unknown enum value: '{}' for enum {}", data.getAsString(), enumType.getSimpleName());
            }
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }

}
