/*
 * Copyright 2018 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.utilities.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.lwjgl.glfw.GLFWVidMode;
import org.terasology.engine.subsystem.Resolution;
import org.terasology.engine.subsystem.headless.device.HeadlessResolution;
import org.terasology.engine.subsystem.lwjgl.LwjglResolution;

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
