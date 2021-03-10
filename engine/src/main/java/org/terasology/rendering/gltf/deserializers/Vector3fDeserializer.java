// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.joml.Vector3f;

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
