// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.input.Input;
import org.terasology.input.InputType;

import java.lang.reflect.Type;

/**
 */
public class InputHandler implements JsonSerializer<Input>, JsonDeserializer<Input> {

    @Override
    public Input deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return InputType.parse(json.getAsString());
    }

    @Override
    public JsonElement serialize(Input src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }
}
