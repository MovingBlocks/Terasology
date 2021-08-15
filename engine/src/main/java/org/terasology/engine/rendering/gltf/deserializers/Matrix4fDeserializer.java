// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

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
