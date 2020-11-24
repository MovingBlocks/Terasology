// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.persistence.typeHandling.coreTypes.ObjectFieldMapTypeHandler;

import java.lang.reflect.Type;

/**
 * A Gson {@link TypeAdapterFactory} that dynamically looks up the {@link TypeHandler} from a
 * {@link TypeHandlerLibrary} for each type encountered, and creates a {@link GsonTypeHandlerAdapter} with
 * the retrieved {@link TypeHandler}.
 */
public class GsonTypeSerializationLibraryAdapterFactory implements TypeAdapterFactory {
    private final TypeHandlerLibrary typeHandlerLibrary;

    public GsonTypeSerializationLibraryAdapterFactory(TypeHandlerLibrary typeHandlerLibrary) {
        this.typeHandlerLibrary = typeHandlerLibrary;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Type rawType = type.getType();

        TypeHandler<T> typeHandler = (TypeHandler<T>) typeHandlerLibrary.getTypeHandler(rawType).orElse(null);

        if (typeHandler == null || typeHandler instanceof ObjectFieldMapTypeHandler) {
            return null;
        }

        return new GsonTypeHandlerAdapter<>(typeHandler, gson, type);
    }
}
