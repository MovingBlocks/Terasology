// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.joml.Vector4f;

import java.lang.reflect.Type;

public class Vector4fTypeAdapter implements JsonDeserializer<Vector4f> {

    @Override
    public Vector4f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            if (array.size() == 4) {
                return new Vector4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), array.get(3).getAsFloat());
            } else if (array.size() == 3) {
                return new Vector4f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat(), 1);
            }
        }
        return null;
    }
}
