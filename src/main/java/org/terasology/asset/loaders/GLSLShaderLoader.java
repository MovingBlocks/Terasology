/*
 * Copyright 2012
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

package org.terasology.asset.loaders;

import com.google.common.io.CharStreams;
import com.google.gson.*;
import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.rendering.assets.Shader;
import org.terasology.rendering.assets.metadata.ParamMetadata;
import org.terasology.rendering.assets.metadata.ShaderMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Immortius
 */
public class GLSLShaderLoader implements AssetLoader<Shader> {

    private Gson gson;

    public GLSLShaderLoader() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ShaderMetadata.class, new ShaderMetadataHandler())
                .create();
    }

    @Override
    public Shader load(InputStream stream, AssetUri uri, List<URL> urls) throws IOException {
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
            return new Shader(uri, vertProgram, fragProgram, metadata);
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
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to close stream", e);
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
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Failed to close stream", e);
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
