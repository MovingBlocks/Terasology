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

import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.AssetManager;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost implements IShaderParameters {

    Texture texture = AssetManager.loadTexture("engine:vignette");

    @Override
    public void applyParameters(ShaderProgram program) {
        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        PostProcessingRenderer.getInstance().getFBO("sceneBloom1").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        PostProcessingRenderer.getInstance().getFBO("sceneBlur1").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        scene.bindDepthTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();

        program.setInt("texScene", 0);
        program.setInt("texBloom", 1);
        program.setInt("texBlur", 2);
        program.setInt("texVignette", 3);
        program.setInt("texDepth", 4);

        program.setFloat("viewingDistance", Config.getInstance().getActiveViewingDistance() * 8.0f);

        // Calculate the fog value based on the daylight value
        float fogLinearIntensity = 0.01f;
        float daylight = (float) CoreRegistry.get(WorldRenderer.class).getDaylight();

        if (daylight < 1.0 && daylight > 0.25) {
            float daylightFactor = (1.0f - daylight) / 0.75f;
            fogLinearIntensity += 0.1f * daylightFactor;
        } else if (daylight <= 0.25f) {
            float daylightFactor = (0.25f - daylight) / 0.25f;
            fogLinearIntensity += TeraMath.lerpf(0.1f, 0.0f, daylightFactor);
        }

        WorldRenderer renderer = CoreRegistry.get(WorldRenderer.class);
        float fogIntensity = renderer.getWorldProvider().getBiomeProvider().getFog(renderer.getWorldProvider().getTimeInDays()) * daylight;

        program.setFloat("fogIntensity", fogIntensity);
        program.setFloat("fogLinearIntensity", fogLinearIntensity);

        if (CoreRegistry.get(LocalPlayer.class).isValid()) {
            Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            Block block = CoreRegistry.get(WorldProvider.class).getBlock(cameraPos);
            program.setInt("swimming", block.isLiquid() ? 1 : 0);
        }
    }

}
