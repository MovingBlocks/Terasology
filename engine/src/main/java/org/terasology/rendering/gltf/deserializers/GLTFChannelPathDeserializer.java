// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.rendering.gltf.model.GLTFChannelPath;

import java.lang.reflect.Type;

/**
 * Json deserializer for GLTFMode enum, translating the GLTF code to an enum entry
 */
public class GLTFChannelPathDeserializer implements JsonDeserializer<GLTFChannelPath> {
    @Override
    public GLTFChannelPath deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return GLTFChannelPath.getPathFromCode(json.getAsString());
    }
}
