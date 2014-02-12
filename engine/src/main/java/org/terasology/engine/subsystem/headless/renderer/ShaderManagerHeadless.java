/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.subsystem.headless.renderer;

import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

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
    public void enableDefault() {
        // Do nothing
    }

    @Override
    public void enableDefaultTextured() {
        // Do nothing
    }

    @Override
    public void disableShader() {
        // Do nothing
    }

}
