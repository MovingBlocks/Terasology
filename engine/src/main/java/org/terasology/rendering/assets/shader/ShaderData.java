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

/**
 */
public class ShaderData implements AssetData {

    private String vertexProgram;
    private String fragmentProgram;
    private List<ShaderParameterMetadata> parameterMetadata;

    public ShaderData(String vertexProgram, String fragmentProgram, List<ShaderParameterMetadata> parameterMetadata) {
        this.vertexProgram = vertexProgram;
        this.fragmentProgram = fragmentProgram;
        this.parameterMetadata = ImmutableList.copyOf(parameterMetadata);
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
}
