/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.utilities.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.Uri;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A Gson Adapter factory for supporting enums in a case-insensitive manner
 *
 */
public class UriTypeAdapterFactory implements TypeAdapterFactory {
    private static final Logger logger = LoggerFactory.getLogger(UriTypeAdapterFactory.class);

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final Class<T> rawType = (Class<T>) type.getRawType();
        if (!Uri.class.isAssignableFrom(rawType)) {
            return null;
        }

        final Constructor<T> constructor;
        try {
            constructor = rawType.getConstructor(String.class);
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            logger.error("URI type {} lacks String constructor", rawType);
            return null;
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                out.value(value.toString());
            }

            @Override
            public T read(JsonReader reader) throws IOException {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    return null;
                } else {
                    String nextString = reader.nextString();
                    try {
                        return constructor.newInstance(nextString);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        logger.error("Failed to instantiate uri of type {} from value {}", rawType, nextString, e);
                        return null;
                    }
                }
            }
        };
    }
}
