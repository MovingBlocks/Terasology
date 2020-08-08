/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

import java.lang.reflect.Type;

/**
 * Json deserializer for a TIntList
 */
public class TIntListDeserializer implements JsonDeserializer<TIntList> {
    @Override
    public TIntList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TIntList result = new TIntArrayList();
        json.getAsJsonArray().forEach(x -> result.add(x.getAsInt()));
        return result;
    }
}
