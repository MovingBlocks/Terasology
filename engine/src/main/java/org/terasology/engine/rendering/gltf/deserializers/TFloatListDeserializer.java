// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;

import java.lang.reflect.Type;

/**
 * Json deserializer for a TFloatList
 */
public class TFloatListDeserializer implements JsonDeserializer<TFloatList> {
    @Override
    public TFloatList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TFloatList result = new TFloatArrayList();
        json.getAsJsonArray().forEach(x -> result.add(x.getAsFloat()));
        return result;
    }
}
