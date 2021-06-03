// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.subsystem.headless.renderer;

import org.terasology.engine.rendering.ShaderManager;
import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.texture.Texture;

public class ShaderManagerHeadless implements ShaderManager {

    private Material activeMaterial;

    @Override
    public void initShaders() {
    }

    @Override
    public void setActiveMaterial(Material material) {
        activeMaterial = material;
    }

    @Override
    public void bindTexture(int slot, Texture texture) {
        // Do nothing
    }

    @Override
    public Material getActiveMaterial() {
        return activeMaterial;
    }

    @Override
    public void recompileAllShaders() {
        // Do nothing
    }

    @Override
    public Material addShaderProgram(String title, String providingModule) {
        return null;
    }
  
    @Override
    public void disableShader() {
        // Do nothing
    }

}
