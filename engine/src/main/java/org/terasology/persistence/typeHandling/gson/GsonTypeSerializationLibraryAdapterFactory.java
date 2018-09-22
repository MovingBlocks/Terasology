/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.terasology.persistence.typeHandling.TypeHandler;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;

import java.lang.reflect.Type;

/**
 * A Gson {@link TypeAdapterFactory} that dynamically looks up the {@link TypeHandler} from a
 * {@link TypeSerializationLibrary} for each type encountered, and creates a {@link GsonTypeHandlerAdapter} with
 * the retrieved {@link TypeHandler}.
 */
public class GsonTypeSerializationLibraryAdapterFactory implements TypeAdapterFactory {
    private final TypeSerializationLibrary typeSerializationLibrary;

    public GsonTypeSerializationLibraryAdapterFactory(TypeSerializationLibrary typeSerializationLibrary) {
        this.typeSerializationLibrary = typeSerializationLibrary;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        Type rawType = type.getType();

        TypeHandler<T> typeHandler = (TypeHandler<T>) typeSerializationLibrary.getTypeHandler(rawType);

        if (typeHandler == null) {
            return null;
        }

        return new GsonTypeHandlerAdapter<>(typeHandler, gson, type);
    }
}
