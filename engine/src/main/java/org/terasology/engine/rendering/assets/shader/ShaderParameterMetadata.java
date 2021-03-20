// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.shader;

/**
 */
public class ShaderParameterMetadata {
    String name;
    ParamType type;

    public ShaderParameterMetadata() {
    }

    public ShaderParameterMetadata(String name, ParamType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ParamType getType() {
        return type;
    }

    public void setType(ParamType type) {
        this.type = type;
    }
}
