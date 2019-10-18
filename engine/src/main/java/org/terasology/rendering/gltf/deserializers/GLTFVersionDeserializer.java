/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.rendering.gltf.deserializers;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.rendering.gltf.model.GLTFVersion;

import java.lang.reflect.Type;

/**
 * Json deserializer for GLTFVersion, translating from a string of the form major.minor to a GLTFVersion object with major and minor values
 */
public class GLTFVersionDeserializer implements JsonDeserializer<GLTFVersion> {

    @Override
    public GLTFVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String versionString = json.getAsString();
        if (Strings.isNullOrEmpty(versionString)) {
            return null;
        }
        int splitPoint = versionString.indexOf('.');
        if (splitPoint == -1) {
            return null;
        }
        int major = Integer.parseInt(versionString.substring(0, splitPoint));
        int minor = Integer.parseInt(versionString.substring(splitPoint + 1));
        return new GLTFVersion(major, minor);
    }
}
