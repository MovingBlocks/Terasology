// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.shader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.exceptions.InvalidAssetFilenameException;
import org.terasology.gestalt.assets.format.AssetDataFile;
import org.terasology.gestalt.assets.format.AssetFileFormat;
import org.terasology.gestalt.assets.module.annotations.RegisterAssetFileFormat;
import org.terasology.gestalt.module.resources.FileReference;
import org.terasology.gestalt.naming.Name;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Predicate;

@RegisterAssetFileFormat
public class GLSLShaderFormat implements AssetFileFormat<ShaderData> {
    public static final String FRAGMENT_SUFFIX = "_frag.glsl";
    public static final String VERTEX_SUFFIX = "_vert.glsl";
    public static final String GEOMETRY_SUFFIX = "_geom.glsl";
    public static final String METADATA_SUFFIX = ".info";
    private Gson gson;

    public GLSLShaderFormat() {
        gson = new GsonBuilder()
                .registerTypeAdapter(ShaderMetadata.class, new ShaderMetadataHandler())
                .create();
    }

    @Override
    public Predicate<FileReference> getFileMatcher() {
        return path -> {
            String name = path.getName();
            return name.endsWith(FRAGMENT_SUFFIX)
                    || name.endsWith(VERTEX_SUFFIX)
                    || name.endsWith(GEOMETRY_SUFFIX)
                    || name.endsWith(METADATA_SUFFIX);
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
        String geomProgram = null;
        ShaderMetadata metadata = new ShaderMetadata();

        for (AssetDataFile input : inputs) {
            String fileName = input.getFilename();

            if (fileName.endsWith(VERTEX_SUFFIX)) {
                vertProgram = readInput(input);
            } else if (fileName.endsWith(FRAGMENT_SUFFIX)) {
                fragProgram = readInput(input);
            } else if (fileName.endsWith(GEOMETRY_SUFFIX)) {
                geomProgram = readInput(input);
            } else {
                metadata = readMetadata(input);
            }
        }
        if (vertProgram != null && fragProgram != null) {
            return new ShaderData(vertProgram, fragProgram, geomProgram, metadata.getParameters());
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
