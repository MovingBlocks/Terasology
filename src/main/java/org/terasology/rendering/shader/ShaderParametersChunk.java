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
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.Time;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.block.management.BlockManager;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk implements IShaderParameters {
    private Texture lava = Assets.getTexture("engine:custom_lava_still");
    private Texture water = Assets.getTexture("engine:water_normal");
    private Texture effects = Assets.getTexture("engine:effects");

    public void applyParameters(ShaderProgram program) {
        Texture terrain = Assets.getTexture("engine:terrain");
        if (terrain == null) {
            return;
        }

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        Time time = CoreRegistry.get(Time.class);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        glBindTexture(GL11.GL_TEXTURE_2D, lava.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, water.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, effects.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        PostProcessingRenderer.getInstance().getFBO("sceneReflected").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        glBindTexture(GL11.GL_TEXTURE_2D, terrain.getId());

        program.setInt("textureLava", 1);
        program.setInt("textureWaterNormal", 2);
        program.setInt("textureEffects", 3);
        program.setInt("textureWaterReflection", 4);
        program.setInt("textureAtlas", 0);

        program.setFloat("blockScale", 1.0f);

        if (worldRenderer != null) {
            program.setFloat("daylight", (float) worldRenderer.getDaylight());
            program.setFloat("swimming", worldRenderer.isUnderWater() ? 1.0f : 0.0f);
        }

        if (localPlayer != null) {
            program.setInt("carryingTorch", localPlayer.isCarryingTorch() ? 1 : 0);
        }

        if (time != null) {
            program.setFloat("time", worldRenderer.getWorldProvider().getTime().getDays());
        }
    }
}
