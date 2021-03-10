// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.terasology.engine.core.subsystem.Resolution;
import org.terasology.engine.core.subsystem.headless.device.HeadlessResolution;
import org.terasology.engine.core.subsystem.lwjgl.LwjglResolution;

import java.lang.reflect.Type;

public final class ResolutionHandler implements JsonSerializer<Resolution>, JsonDeserializer<Resolution> {

    @Override
    public Resolution deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        if (json.isJsonNull()) {
            return HeadlessResolution.getInstance();
        }
        return context.deserialize(json, LwjglResolution.class);
    }

    @Override
    public JsonElement serialize(Resolution src, Type typeOfSrc, JsonSerializationContext context) {
        if (src instanceof LwjglResolution) {
            return context.serialize(src);
        } else {
            return null;
        }
    }
}
