// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.persistence.typeHandling.coreTypes;

import com.google.common.collect.Maps;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class GenericMapTypeHandler<K, V> extends TypeHandler<Map<K, V>> {

    private final TypeHandler<K> keyHandler;
    private final TypeHandler<V> valueHandler;

    public GenericMapTypeHandler(TypeHandler<K> keyHandler, TypeHandler<V> valueHandler) {
        this.keyHandler = keyHandler;
        this.valueHandler = valueHandler;
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

        Map<String, PersistedData> jsonEntry = Maps.newLinkedHashMap();
        if (!key.isNull()) {
            jsonEntry.put("_key", key);
            jsonEntry.put("_value", value);
        }
        return serializer.serialize(jsonEntry);
    }

    @Override
    public Optional<Map<K, V>> deserialize(PersistedData data) {
        if (!data.isArray()) {
            return Optional.empty();
        }

        Map<K, V> result = Maps.newLinkedHashMap();

        for (PersistedData entry : data.getAsArray()) {
            final Optional<K> key = keyHandler.deserialize(entry.getAsValueMap().get("_key"));
            final Optional<V> value = valueHandler.deserialize(entry.getAsValueMap().get("_value"));

            key.ifPresent(k -> result.put(k, value.orElse(null)));
        }

        return Optional.of(result);
    }
}
