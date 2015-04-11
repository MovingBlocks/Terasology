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
package org.terasology.rendering.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * Shader parameters for the Block shader program.
 *
 * @author Benjamin Glatzel
 */
// TODO: Put these values in a material and use that.
public class ShaderParametersBlock extends ShaderParametersBase {

    public ShaderParametersBlock() {
    }

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        Texture terrainTex = Assets.getTexture("engine:terrain");

        if (terrainTex == null) {
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

        program.setFloat3(UNIFORM, "colorOffset", 1.0f, 1.0f, 1.0f, true);
        program.setBoolean(UNIFORM, "textured", true, true);
        program.setFloat(UNIFORM, "alpha", 1.0f, true);
    }

}
