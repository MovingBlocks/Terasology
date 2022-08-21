// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataMap;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A type handler for generic maps delegating (de-)serialization to the respective type handler for the key type {@code
 * K} and the value type {@code V}.
 *
 * @param <K> the type of keys; requires a {@code TypeHandler<K>}
 * @param <V> the type of mapped values; requires a {@code TypeHandler<V>}
 */
public class GenericMapTypeHandler<K, V> extends TypeHandler<Map<K, V>> {

    static final String KEY = "key";
    static final String VALUE = "value";

    private static final Logger logger = LoggerFactory.getLogger(GenericMapTypeHandler.class);

    private final TypeHandler<K> keyHandler;
    private final TypeHandler<V> valueHandler;

    public GenericMapTypeHandler(TypeHandler<K> keyHandler, TypeHandler<V> valueHandler) {
        this.keyHandler = Preconditions.checkNotNull(keyHandler);
        this.valueHandler = Preconditions.checkNotNull(valueHandler);
    }

    @Override
    protected PersistedData serializeNonNull(Map<K, V> data, PersistedDataSerializer serializer) {
        final List<PersistedData> entries = data.entrySet().stream()
                .map(entry -> serializeEntry(entry, serializer))
                .collect(Collectors.toList());
        return serializer.serialize(entries);
    }

    private PersistedData serializeEntry(Map.Entry<K, V> entry, PersistedDataSerializer serializer) {
        PersistedData key = keyHandler.serialize(entry.getKey(), serializer);
        PersistedData value = valueHandler.serialize(entry.getValue(), serializer);

        Map<String, PersistedData> result = Maps.newLinkedHashMap();
        if (!key.isNull()) {
            result.put(KEY, key);
            result.put(VALUE, value);
        }
        return serializer.serialize(result);
    }

    @Override
    public Optional<Map<K, V>> deserialize(PersistedData data) {
        if (!data.isArray()) {
            return Optional.empty();
        }

        Map<K, V> result = Maps.newLinkedHashMap();

        for (PersistedData entry : data.getAsArray()) {
            PersistedDataMap kvEntry = entry.getAsValueMap();
            final Optional<K> key = keyHandler.deserialize(kvEntry.get(KEY));

            if (key.isPresent()) {
                final Optional<V> value = valueHandler.deserialize(kvEntry.get(VALUE));
                if (value.isPresent()) {
                    result.put(key.get(), value.get());
                } else {
                    logger.warn("Missing value for key '{}' with {} given entry '{}'", key.get(), valueHandler, kvEntry.get(VALUE));
                }
            } else {
                logger.warn("Missing field '{}' for entry '{}'", KEY, kvEntry);
            }
        }

        return Optional.of(result);
    }
}
