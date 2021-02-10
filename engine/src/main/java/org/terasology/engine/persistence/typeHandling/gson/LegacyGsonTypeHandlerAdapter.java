/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.persistence.typeHandling.gson;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.persistence.typeHandling.TypeHandler;

import java.lang.reflect.Type;

/**
 * Adapts a {@link TypeHandler} as a legacy Gson {@link JsonSerializer} and {@link JsonDeserializer}.
 * Instances of {@link LegacyGsonTypeHandlerAdapter}, when registered as type adapters in a {@link Gson}
 * object, can be used to (de)serialize objects to JSON (via Gson) with the rules specified by
 * the {@link #typeHandler}.
 */
@Deprecated
public class LegacyGsonTypeHandlerAdapter<T> implements JsonDeserializer<T>, JsonSerializer<T> {

    private TypeHandler<T> typeHandler;

    public LegacyGsonTypeHandlerAdapter(TypeHandler<T> handler) {
        this.typeHandler = handler;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return typeHandler.deserializeOrNull(new GsonPersistedData(json));
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return ((GsonPersistedData) typeHandler.serialize(src, new GsonPersistedDataSerializer())).getElement();
    }
}
