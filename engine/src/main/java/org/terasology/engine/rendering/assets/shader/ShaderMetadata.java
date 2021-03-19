// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.shader;

import com.google.common.collect.Lists;

import java.util.List;

/**
 */
public class ShaderMetadata {
    List<ShaderParameterMetadata> parameters = Lists.newArrayList();

    public List<ShaderParameterMetadata> getParameters() {
        return parameters;
    }

    public void setParameters(List<ShaderParameterMetadata> parameters) {
        this.parameters = parameters;
    }
}
