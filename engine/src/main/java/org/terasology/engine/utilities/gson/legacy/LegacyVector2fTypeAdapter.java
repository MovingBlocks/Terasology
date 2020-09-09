// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.gson.legacy;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.terasology.math.geom.Vector2f;

import java.lang.reflect.Type;

/**
 */
public class LegacyVector2fTypeAdapter implements JsonDeserializer<Vector2f> {
    @Override
    public Vector2f deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = json.getAsJsonArray();
        return new Vector2f(jsonArray.get(0).getAsFloat(), jsonArray.get(1).getAsFloat());
    }
}
