/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.lwjgl.opengl.GL13;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.TextureManager;
import org.terasology.model.blocks.management.BlockManager;

/**
 * Shader parameters for the Chunk shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersChunk implements IShaderParameters {

    public void applyParameters(ShaderProgram program) {
        Terasology tera = Terasology.getInstance();

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        TextureManager.getInstance().bindTexture("custom_lava_still");
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        TextureManager.getInstance().bindTexture("water_normal");
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        TextureManager.getInstance().bindTexture("effects");
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        TextureManager.getInstance().bindTexture("terrain");

        program.setInt("textureLava", 1);
        program.setInt("textureWaterNormal", 2);
        program.setInt("textureEffects", 3);
        program.setInt("textureAtlas", 0);

        if (tera.getActiveWorldRenderer() != null)
            program.setFloat("daylight", (float) tera.getActiveWorldRenderer().getDaylight());

        if (tera.getActivePlayer() != null) {
            // TODO: This should be whether the camera is underwater I think?
            //program.setInt("swimming", tera.getActivePlayer().isSwimming() ? 1 : 0);
            program.setInt("carryingTorch", tera.getActivePlayer().isCarryingTorch() ? 1 : 0);
        }

        if (tera.getActiveWorldProvider() != null) {
            program.setFloat("time", (float) tera.getActiveWorldProvider().getTime());
        }

        program.setFloat1("wavingCoordinates", BlockManager.getInstance().calcCoordinatesForWavingBlocks());
        program.setFloat2("grassCoordinate", BlockManager.getInstance().calcCoordinate("Grass"));
        program.setFloat2("waterCoordinate", BlockManager.getInstance().calcCoordinate("Water"));
        program.setFloat2("lavaCoordinate", BlockManager.getInstance().calcCoordinate("Lava"));
    }

}
