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

import com.google.common.collect.ImmutableList;
import org.terasology.assets.AssetData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class ShaderData implements AssetData {

    private static final String DEFAULT_VERSION = "120";

    private final String vertexProgramVersion;
    private final String fragmentProgramVersion;
    private final String geometryProgramVersion;
    private final String vertexProgram;
    private final String fragmentProgram;
    private final String geometryProgram;
    private List<ShaderParameterMetadata> parameterMetadata;

    public ShaderData(String vertexProgram, String fragmentProgram, List<ShaderParameterMetadata> parameterMetadata) {
        this(vertexProgram, fragmentProgram, null, parameterMetadata);
    }

    public ShaderData(String vertexProgram, String fragmentProgram, String geometryProgram, List<ShaderParameterMetadata> parameterMetadata) {
        this.vertexProgramVersion = getShaderVersion(vertexProgram);
        this.vertexProgram = stripShaderVersion(vertexProgram);
        this.fragmentProgramVersion = getShaderVersion(fragmentProgram);
        this.fragmentProgram = stripShaderVersion(fragmentProgram);

        if (geometryProgram != null) {
            this.geometryProgramVersion = getShaderVersion(geometryProgram);
            this.geometryProgram = stripShaderVersion(geometryProgram);
        } else {
            this.geometryProgramVersion = null;
            this.geometryProgram = null;
        }
        this.parameterMetadata = ImmutableList.copyOf(parameterMetadata);
    }

    public String getVertexProgram() {
        return vertexProgram;
    }

    public String getFragmentProgram() {
        return fragmentProgram;
    }

    public String getGeometryProgram() {
        return geometryProgram;
    }

    public String getVertexProgramVersion() {
        return vertexProgramVersion;
    }

    public String getFragmentProgramVersion() {
        return fragmentProgramVersion;
    }

    public String getGeometryProgramVersion() {
        return geometryProgramVersion;
    }

    public List<ShaderParameterMetadata> getParameterMetadata() {
        return parameterMetadata;
    }

    private String getShaderVersion(String shaderSource) {
        Pattern versionPattern = Pattern.compile("^#version (\\d+.*)");
        Matcher versionMatcher = versionPattern.matcher(shaderSource);

        if (versionMatcher.find()) {
            return versionMatcher.group(1);
        } else {
            return DEFAULT_VERSION;
        }
    }

    private String stripShaderVersion(String source) {
        if (source.startsWith("#version ")) {
            return source.substring(source.indexOf("\n") + 1);
        } else {
            return source;
        }
    }
}
