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

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;

/**
 */
public abstract class Shader extends Asset<ShaderData> {

    protected Shader(ResourceUrn urn, AssetType<?, ShaderData> assetType) {
        super(urn, assetType);
    }

    /**
     * Recompiles the shader
     */
    public abstract void recompile();

    /**
     * @param desc
     * @return The desired shader param, or null if there isn't one with that name
     */
    public abstract ShaderParameterMetadata getParameter(String desc);

    /**
     * @return The list of parameters this shader has
     */
    public abstract Iterable<ShaderParameterMetadata> listParameters();

}
