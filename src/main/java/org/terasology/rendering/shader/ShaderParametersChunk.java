/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import static org.lwjgl.opengl.GL11.glBindTexture;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.management.BlockManager;

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
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

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

        if (worldRenderer != null)
            program.setFloat("daylight", (float) worldRenderer.getDaylight());

        if (localPlayer != null) {
            // TODO: This should be whether the camera is underwater I think?
            program.setInt("underWater", worldRenderer.isUnderWater() ? 1 : 0);
            // TODO: Should be a camera setting?
            program.setInt("carryingTorch", localPlayer.isCarryingTorch() ? 1 : 0);
        }

        if (worldProvider != null) {
            program.setFloat("time", (float) worldProvider.getTimeInDays());
        }

        program.setFloat1("wavingCoordinates", BlockManager.getInstance().calcCoordinatesForWavingBlocks());
        program.setFloat2("grassCoordinate", BlockManager.getInstance().calcCoordinate("engine:grass"));
        program.setFloat2("waterCoordinate", BlockManager.getInstance().calcCoordinate("engine:water"));
        program.setFloat2("lavaCoordinate", BlockManager.getInstance().calcCoordinate("engine:lava"));
    }

}
