// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Map;

/**
 * A {@link TypeAdapterFactory} that generates type adapters which read and write type information for
 * sub-types of the given base type. The type information allows the generated type adapters to dynamically
 * identify which sub-type instance is being read or written when it is given a base type reference.
 *
 * @param <T> The base type.
 */
public class PolymorphicTypeAdapterFactory<T> implements TypeAdapterFactory {
    private static final String TYPE_FIELD_NAME = "@type";

    private final Class<T> baseClass;

    private PolymorphicTypeAdapterFactory(Class<T> baseClass) {
        this.baseClass = baseClass;
    }

    public static <T> PolymorphicTypeAdapterFactory<T> of(Class<T> baseClass) {
        return new PolymorphicTypeAdapterFactory<>(baseClass);
    }

    @Override
    public <R> TypeAdapter<R> create(Gson gson, TypeToken<R> type) {
        if (!baseClass.isAssignableFrom(type.getRawType())) {
            return null;
        }

        return new TypeAdapter<R>() {
            @SuppressWarnings("unchecked")
            @Override
            public void write(JsonWriter out, R value) throws IOException {
                Class<?> valueClass = value.getClass();
                String valueTypeName = valueClass.getName();

                TypeToken<?> valueType = TypeToken.get(valueClass);
                TypeAdapter<R> delegate = (TypeAdapter<R>)
                        gson.getDelegateAdapter(PolymorphicTypeAdapterFactory.this, valueType);

                if (delegate == null) {
                    throw new JsonParseException("Could not serialize " + valueClass.getName());
                }

                JsonElement jsonElement = delegate.toJsonTree(value);

                if (valueClass != baseClass) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();

                    JsonObject clone = new JsonObject();
                    clone.add(TYPE_FIELD_NAME, new JsonPrimitive(valueTypeName));

                    for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                        clone.add(e.getKey(), e.getValue());
                    }

                    Streams.write(clone, out);
                } else {
                    Streams.write(jsonElement, out);
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public R read(JsonReader in) throws IOException {
                JsonElement jsonElement = Streams.parse(in);
                Class<?> valueClass;

                if (jsonElement.isJsonObject()) {
                    JsonElement typeNameJsonElement = jsonElement.getAsJsonObject().remove(TYPE_FIELD_NAME);

                    if (typeNameJsonElement != null) {
                        String typeName = typeNameJsonElement.getAsString();

                        try {
                            valueClass = Class.forName(typeName);
                        } catch (ClassNotFoundException e) {
                            throw new JsonParseException("Could not find class " + typeName);
                        }
                    } else {
                        valueClass = baseClass;
                    }
                } else {
                    valueClass = baseClass;
                }

                if (!baseClass.isAssignableFrom(valueClass)) {
                    throw new JsonParseException(valueClass.getName() + " does not derive from " + baseClass.getName());
                }

                TypeToken<?> valueType = TypeToken.get(valueClass);
                TypeAdapter<R> delegate = (TypeAdapter<R>)
                        gson.getDelegateAdapter(PolymorphicTypeAdapterFactory.this, valueType);

                if (delegate == null) {
                    throw new JsonParseException("Could not deserialize " + valueClass.getName());
                }

                return delegate.fromJsonTree(jsonElement);
            }
        };
    }
}
