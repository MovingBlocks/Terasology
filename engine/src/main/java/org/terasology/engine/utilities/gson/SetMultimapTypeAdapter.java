// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.gson;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 */
public class SetMultimapTypeAdapter<V> implements JsonDeserializer<SetMultimap<String, V>>, JsonSerializer<SetMultimap<String, V>> {

    private Class<V> valueType;

    public SetMultimapTypeAdapter(Class<V> valueType) {
        this.valueType = valueType;
    }

    @Override
    public SetMultimap<String, V> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SetMultimap<String, V> result = HashMultimap.create();
        JsonObject obj = json.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getValue().isJsonArray()) {
                for (JsonElement item : entry.getValue().getAsJsonArray()) {
                    result.put(entry.getKey(), context.<V>deserialize(item, valueType));
                }
            } else {
                result.put(entry.getKey(), context.<V>deserialize(entry.getValue(), valueType));
            }
        }
        return result;
    }

    @Override
    public JsonElement serialize(SetMultimap<String, V> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        List<String> keys = Lists.newArrayList(src.keys());
        Collections.sort(keys);
        for (String key : keys) {
            Collection<V> values = src.get(key);
            if (values.size() > 1) {
                JsonArray array = new JsonArray();
                for (V value : values) {
                    array.add(context.serialize(value));
                }
                result.add(key, array);
            } else if (values.size() == 1) {
                result.add(key, context.serialize(values.iterator().next()));
            } else {
                result.add(key, context.serialize(""));
            }
        }
        return result;
    }
}
