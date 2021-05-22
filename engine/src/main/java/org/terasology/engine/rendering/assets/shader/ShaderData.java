// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.assets.shader;

import com.google.common.collect.ImmutableList;
import org.terasology.gestalt.assets.AssetData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
