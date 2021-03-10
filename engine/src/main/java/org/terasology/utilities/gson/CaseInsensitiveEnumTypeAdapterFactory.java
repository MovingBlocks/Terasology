// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.gson;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * A Gson Adapter factory for supporting enums in a case-insensitive manner
 *
 */
public class CaseInsensitiveEnumTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<T> rawType = (Class<T>) type.getRawType();
        if (!rawType.isEnum()) {
            return null;
        }

        final Map<String, T> lowercaseToConstant = Maps.newHashMap();
        for (T constant : rawType.getEnumConstants()) {
            String norm = normalize(constant.toString());
            lowercaseToConstant.put(norm, constant);
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                } else {
                    out.value(normalize(value.toString()));
                }
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    String value = reader.nextString();
                    return lowercaseToConstant.get(normalize(value));
                }
            }
        };
    }

    private String normalize(String name) {
        return name.toLowerCase(Locale.ENGLISH);
    }
}
