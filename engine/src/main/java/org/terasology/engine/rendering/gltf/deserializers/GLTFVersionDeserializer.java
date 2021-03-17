// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

import com.google.common.base.Strings;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.engine.rendering.gltf.model.GLTFVersion;

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
