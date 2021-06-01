// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering;

import org.terasology.engine.rendering.assets.material.Material;
import org.terasology.engine.rendering.assets.texture.Texture;

public interface ShaderManager {

    void initShaders();

    void setActiveMaterial(Material material);

    void bindTexture(int slot, Texture texture);

    Material getActiveMaterial();

    void recompileAllShaders();

    Material addShaderProgram(String title, String providingModule);

    void disableShader();

}
