// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

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
