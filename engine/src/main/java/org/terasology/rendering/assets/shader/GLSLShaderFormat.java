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
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.format.AssetFileFormat;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.naming.Name;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.file.PathMatcher;
import java.util.List;

/**
 */
@RegisterAssetFileFormat
public class GLSLShaderFormat implements AssetFileFormat<ShaderData> {
    public static final String FRAGMENT_SUFFIX = "_frag.glsl";
    public static final String VERTEX_SUFFIX = "_vert.glsl";
    public static final String METADATA_SUFFIX = ".info";
    private Gson gson;

    public GLSLShaderFormat() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ShaderMetadata.class, new ShaderMetadataHandler())
                .create();
    }

    @Override
    public PathMatcher getFileMatcher() {
        return path -> {
            String name = path.getFileName().toString();
            return name.endsWith(FRAGMENT_SUFFIX) || path.getFileName().toString().endsWith(VERTEX_SUFFIX) || path.getFileName().toString().endsWith(METADATA_SUFFIX);
        };
    }

    @Override
    public Name getAssetName(String filename) throws InvalidAssetFilenameException {
        if (filename.endsWith(METADATA_SUFFIX)) {
            return new Name(filename.substring(0, filename.length() - METADATA_SUFFIX.length()));
        } else {
            return new Name(filename.substring(0, filename.length() - FRAGMENT_SUFFIX.length()));
        }
    }

    @Override
    public ShaderData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {
        String vertProgram = null;
        String fragProgram = null;
        ShaderMetadata metadata = new ShaderMetadata();

        for (AssetDataFile input : inputs) {
            if (input.getFilename().endsWith(VERTEX_SUFFIX)) {
                vertProgram = readInput(input);
            } else if (input.getFilename().endsWith(FRAGMENT_SUFFIX)) {
                fragProgram = readInput(input);
            } else {
                metadata = readMetadata(input);
            }
        }
        if (vertProgram != null && fragProgram != null) {
            return new ShaderData(vertProgram, fragProgram, metadata.getParameters());
        }
        throw new IOException("Failed to load shader '" + urn + "' - missing vertex or fragment program");
    }

    private ShaderMetadata readMetadata(AssetDataFile input) throws IOException {
        try (Reader reader = new InputStreamReader(input.openStream(), Charsets.UTF_8)) {
            return gson.fromJson(reader, ShaderMetadata.class);
        }
    }

    private String readInput(AssetDataFile input) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(input.openStream(), Charsets.UTF_8)) {
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
