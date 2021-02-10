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
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.joml.Matrix4f;

import java.lang.reflect.Type;

/**
 * Json deserializer for an Matrix4f.
 */
public class Matrix4fDeserializer implements JsonDeserializer<Matrix4f> {
    @Override
    public Matrix4f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TFloatList result = new TFloatArrayList();
        json.getAsJsonArray().forEach(x -> result.add(x.getAsFloat()));
        if (result.size() != 16) {
            throw new JsonParseException("Incorrect number of values for ImmutableMatrix4f - expected 16");
        }
        return new Matrix4f(
                result.get(0), result.get(1), result.get(2), result.get(3),
                result.get(4), result.get(5), result.get(6), result.get(7),
                result.get(8), result.get(9), result.get(10), result.get(11),
                result.get(12), result.get(13), result.get(14), result.get(15));
    }
}
