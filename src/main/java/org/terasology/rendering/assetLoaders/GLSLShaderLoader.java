/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.rendering.assetLoaders;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.MaterialShader;
import org.terasology.rendering.assets.metadata.ParamMetadata;
import org.terasology.rendering.assets.metadata.ShaderMetadata;

import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * @author Immortius
 */
public class GLSLShaderLoader implements AssetLoader<MaterialShader> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GLSLShaderLoader.class);

    private Gson gson;

    public GLSLShaderLoader() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ShaderMetadata.class, new ShaderMetadataHandler())
                .create();
    }

    @Override
    public MaterialShader load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
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
            return new MaterialShader(uri, vertProgram, fragProgram, metadata);
        }
        return null;
    }

    private ShaderMetadata readMetadata(URL url) throws IOException {
        Reader reader = new InputStreamReader(url.openStream());
        try {
            return gson.fromJson(reader, ShaderMetadata.class);
        } finally {
            // JAVA7: clean up
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Failed to close stream", e);
            }
        }
    }

    private String readUrl(URL url) throws IOException {
        InputStream stream = url.openStream();
        InputStreamReader reader = new InputStreamReader(stream);
        try {
            return CharStreams.toString(reader);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.error("Failed to close stream", e);
            }
        }
    }

    private static class ShaderMetadataHandler implements JsonDeserializer<ShaderMetadata> {

        @Override
        public ShaderMetadata deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ShaderMetadata result = new ShaderMetadata();
            ParamMetadata[] params = context.deserialize(json.getAsJsonObject().get("params"), ParamMetadata[].class);
            for (ParamMetadata param : params) {
                result.getParameters().add(param);
            }
            return result;
        }
    }
}
