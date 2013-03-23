/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.game.CoreRegistry;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.assets.Texture;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Block shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersBlock implements IShaderParameters {

    public ShaderParametersBlock() {
    }

    @Override
    public void applyParameters(ShaderProgram program) {
        Texture terrainTex = Assets.getTexture("engine:terrain");
        if (terrainTex == null) {
            return;
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        program.setInt("carryingTorch", localPlayer.isCarryingTorch() ? 1 : 0);

        program.setFloat3("colorOffset", 1.0f, 1.0f, 1.0f);
        program.setInt("textured", 1);
        program.setFloat("alpha", 1f);
    }

}
