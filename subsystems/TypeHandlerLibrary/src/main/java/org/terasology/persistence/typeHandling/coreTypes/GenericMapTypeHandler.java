// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.base.MoreObjects;
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

        Map<K, V> result = Maps.newLinkedHashMap();

        if (data.isNull()) {
            return Optional.of(result);
        }

        if (!data.isArray() || data.isValueMap()) {
            logger.warn("Incorrect map format detected: object instead of array.\n{}", getUsageInfo(data));
            return Optional.empty();
        }

        for (PersistedData entry : data.getAsArray()) {
            PersistedDataMap kvEntry = entry.getAsValueMap();
            PersistedData rawKey = kvEntry.get(KEY);
            PersistedData rawValue = kvEntry.get(VALUE);
            if (rawKey == null || rawValue == null) {
                logger.warn("Incorrect map format detected: missing map entry with \"key\" or \"value\" key.\n{}", getUsageInfo(data));
                return Optional.empty();
            }

            final Optional<K> key = keyHandler.deserialize(rawKey);
            if (key.isEmpty()) {
                logger.warn("Could not deserialize key '{}' as '{}'", rawKey, keyHandler.getClass().getSimpleName());
                return Optional.empty();
            }

            final Optional<V> value = valueHandler.deserialize(kvEntry.get(VALUE));
            if (value.isEmpty()) {
                logger.warn("Could not deserialize value '{}' as '{}'", rawValue, valueHandler.getClass().getSimpleName());
                return Optional.empty();
            }

            result.put(key.get(), value.get());
        }

        return Optional.of(result);
    }

    private String getUsageInfo(PersistedData data) {
        return "Expected\n" +
                "  \"mapName\": [\n" +
                "    { \"key\": \"...\", \"value\": \"...\" }\n" +
                "  ]\n" +
                "but found \n'" + data + "'";
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key", keyHandler)
                .add("value", valueHandler)
                .toString();
    }
}
