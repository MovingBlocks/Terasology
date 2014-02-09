/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.assets.shader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.asset.AssetLoader;
import org.terasology.engine.module.Module;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

/**
 * @author Immortius
 */
public class GLSLShaderLoader implements AssetLoader<ShaderData> {
    private Gson gson;

    public GLSLShaderLoader() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ShaderMetadata.class, new ShaderMetadataHandler())
                .create();
    }

    @Override
    public ShaderData load(Module module, InputStream stream, List<URL> urls) throws IOException {
        String vertProgram = null;
        String fragProgram = null;
        ShaderMetadata metadata = new ShaderMetadata();

        for (URL url : urls) {
            if (url.toString().endsWith("_vert.glsl")) {
                vertProgram = readUrl(url);
            } else if (url.toString().endsWith("_frag.glsl")) {
                fragProgram = readUrl(url);
            } else if (url.toString().endsWith(".info")) {
                metadata = readMetadata(url);
            }
        }
        if (vertProgram != null && fragProgram != null) {
            return new ShaderData(vertProgram, fragProgram, metadata.getParameters());
        }
        return null;
    }

    private ShaderMetadata readMetadata(URL url) throws IOException {
        try (Reader reader = new InputStreamReader(url.openStream(), Charsets.UTF_8)) {
            return gson.fromJson(reader, ShaderMetadata.class);
        }
    }

    private String readUrl(URL url) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(url.openStream(), Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }

    private static class ShaderMetadataHandler implements JsonDeserializer<ShaderMetadata> {

        @Override
        public ShaderMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ShaderMetadata result = new ShaderMetadata();
            ShaderParameterMetadata[] params = context.deserialize(json.getAsJsonObject().get("params"), ShaderParameterMetadata[].class);
            for (ShaderParameterMetadata param : params) {
                result.getParameters().add(param);
            }
            return result;
        }
    }
}
