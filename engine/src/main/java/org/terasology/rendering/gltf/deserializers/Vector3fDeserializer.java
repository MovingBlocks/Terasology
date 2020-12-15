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
import org.terasology.math.geom.Vector3f;

import java.lang.reflect.Type;

/**
 * Json deserializer for an Vector3f
 */
public class Vector3fDeserializer implements JsonDeserializer<Vector3f> {
    @Override
    public Vector3f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TFloatList result = new TFloatArrayList();
        json.getAsJsonArray().forEach(x -> result.add(x.getAsFloat()));
        if (result.size() != 3) {
            throw new JsonParseException("Incorrect number of values for ImmutableVector3f - expected 3");
        }
        return new Vector3f(result.get(0), result.get(1), result.get(2));
    }
}
