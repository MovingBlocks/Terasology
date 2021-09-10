// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.assets;

import org.terasology.engine.rendering.assets.shader.Shader;
import org.terasology.engine.rendering.assets.shader.ShaderData;
import org.terasology.engine.rendering.assets.shader.ShaderParameterMetadata;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;

import java.util.Collections;

public class HeadlessShader extends Shader {

    private ShaderData shaderProgramBase;

    public HeadlessShader(ResourceUrn urn, AssetType<?, ShaderData> assetType, ShaderData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    public void recompile() {
        // do nothing
    }

    @Override
    public ShaderParameterMetadata getParameter(String desc) {
        return null;
    }

    @Override
    public Iterable<ShaderParameterMetadata> listParameters() {
        return Collections.emptyList();
    }

    @Override
    protected void doReload(ShaderData data) {
        shaderProgramBase = data;
    }

}
