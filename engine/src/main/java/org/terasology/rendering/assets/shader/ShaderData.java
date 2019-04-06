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
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import org.terasology.assets.AssetData;
import org.terasology.assets.format.AssetDataFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 */
public class ShaderData implements AssetData {

    private String vertexProgram;
    private String fragmentProgram;
    private List<ShaderParameterMetadata> parameterMetadata;
    private AssetDataFile vertexFile;
    private AssetDataFile fragmentFile;

    public ShaderData(String vertexProgram, String fragmentProgram, List<ShaderParameterMetadata> parameterMetadata, AssetDataFile vertexFile, AssetDataFile fragmentFile) {
        this.vertexProgram = vertexProgram;
        this.fragmentProgram = fragmentProgram;
        this.parameterMetadata = ImmutableList.copyOf(parameterMetadata);
        this.vertexFile = vertexFile;
        this.fragmentFile = fragmentFile;
    }

    public String getVertexProgram() {
        return vertexProgram;
    }

    public String getFragmentProgram() {
        return fragmentProgram;
    }

    public List<ShaderParameterMetadata> getParameterMetadata() {
        return parameterMetadata;
    }

    /**
     * Reload the shader data from the source development folder, circumventing the Gestalt
     * filesystem in order to make it easier to develop shaders
     * @throws IOException
     */
    public void reloadFromSource() throws IOException {
        vertexProgram = readSourceFile(vertexFile);
        fragmentProgram = readSourceFile(fragmentFile);
        // metadata was not included here because it is small element and does not
        // give much benefit for reloading from source
    }

    /**
     * Reads shader from the source folder, rather than the shader in the build folder,
     * allowing developers to do hot reloading of shaders during shader development in the source folder,
     * rather than the temporary build folder.
     * A somewhat hacky approach which does not use the Gestalt filesystem to load the asset
     * @param input Gestalt asset data file, used to get filename and path
     * @return shader program string
     * @throws IOException if thrown, this probably means the file was deleted, or there is no src folder
     */
    private String readSourceFile(AssetDataFile input) throws IOException {
        // TODO: consider overwriting existing build file with source file read here

        List<String> inputPath = input.getPath();
        inputPath.remove(0);
        String pathToShaderSource = "engine/src/main/resources/" + String.join("/", inputPath) + "/";
        String fullPath = pathToShaderSource + input.getFilename();

        return readFile(fullPath);
    }

    /**
     * Read file given path
     * @param path path string
     * @return contents of file
     * @throws IOException
     */
    private String readFile(String path) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path), Charsets.UTF_8)) {
            return CharStreams.toString(reader);
        }
    }
}
