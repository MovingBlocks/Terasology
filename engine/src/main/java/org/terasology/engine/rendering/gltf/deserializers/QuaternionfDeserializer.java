// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.joml.Quaternionf;

import java.lang.reflect.Type;

/**
 * Json deserializer for an Quat4f.
 */
public class QuaternionfDeserializer implements JsonDeserializer<Quaternionf> {
    @Override
    public Quaternionf deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TFloatList result = new TFloatArrayList();
        json.getAsJsonArray().forEach(x -> result.add(x.getAsFloat()));
        if (result.size() != 4) {
            throw new JsonParseException("Incorrect number of values for ImmutableQuat4f - expected 4");
        }
        return new Quaternionf(result.get(0), result.get(1), result.get(2), result.get(3));
    }
}
