// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.io.IOException;

/**
 * Adapts a {@link TypeHandler} as a Gson {@link TypeAdapter}. Instances of {@link GsonTypeHandlerAdapter},
 * when registered as type adapters in a {@link Gson} object, can be used to (de)serialize objects
 * to JSON (via Gson) with the rules specified by the {@link GsonTypeHandlerAdapter#typeHandler}.
 *
 * Since instances of {@link GsonTypeHandlerAdapter} require a {@link Gson} object and a
 * {@link TypeToken}, it is recommended to register {@link GsonTypeHandlerAdapter} type adapters as a
 * type adapter factory via a {@link com.google.gson.TypeAdapterFactory} like
 * {@link GsonTypeHandlerAdapterFactory}.
 */
public final class GsonTypeHandlerAdapter<T> extends TypeAdapter<T> {

    private final TypeHandler<T> typeHandler;

    GsonTypeHandlerAdapter(TypeHandler<T> typeHandler,
                           Gson gson, TypeToken<T> typeToken) {
        this.typeHandler = typeHandler;
    }

    @Override
    public T read(JsonReader in) throws IOException {
        JsonElement value = Streams.parse(in);
        if (value.isJsonNull()) {
            return null;
        }
        return this.typeHandler.deserializeOrNull(new GsonPersistedData(value));
    }

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        JsonElement tree = ((GsonPersistedData) typeHandler.serialize(value, new GsonPersistedDataSerializer())).getElement();
        Streams.write(tree, out);
    }
}

