// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * A Gson {@link TypeAdapterFactory} that creates a {@link GsonTypeHandlerAdapter} for each
 * {@link TypeHandler} registered in the {@link #typeHandlerMap}.
 */
public class GsonTypeHandlerAdapterFactory implements TypeAdapterFactory {
    private Map<Class<?>, TypeHandler<?>> typeHandlerMap;

    public GsonTypeHandlerAdapterFactory() {
        typeHandlerMap = new HashMap<>();
    }

    /**
     * Adds a {@link TypeHandler} to the {@link #typeHandlerMap} for the given type.
     *
     * @param typeHandlerEntry The {@link TypeHandlerEntry} encapsulating the {@link TypeHandler} for
     *                         the given type.
     */
    public <T> void addTypeHandler(TypeHandlerEntry<T> typeHandlerEntry) {
        addTypeHandler(typeHandlerEntry.type, typeHandlerEntry.typeHandler);
    }

    /**
     * Adds a {@link TypeHandler} to the {@link #typeHandlerMap} for the given type.
     * @param type The {@link Class} of the type.
     * @param typeHandler The {@link TypeHandler} for the type.
     */
    public <T> void addTypeHandler(Class<T> type, TypeHandler<T> typeHandler) {
        typeHandlerMap.put(type, typeHandler);
    }

    /**
     * Returns a boolean stating whether the {@link #typeHandlerMap} contains a type handler for the given type.
     * @param type The {@link Class} of the given type.
     */
    public boolean containsTypeHandlerFor(Class<?> type) {
        return typeHandlerMap.containsKey(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Class<? super T> rawType = type.getRawType();

        if (!containsTypeHandlerFor(rawType)) {
            return null;
        }

        return new GsonTypeHandlerAdapter<>((TypeHandler<T>) typeHandlerMap.get(rawType), gson, type);
    }
}
