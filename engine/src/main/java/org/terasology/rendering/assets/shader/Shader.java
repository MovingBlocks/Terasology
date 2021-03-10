// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.shader;

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
