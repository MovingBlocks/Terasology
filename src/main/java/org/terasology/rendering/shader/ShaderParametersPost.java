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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.nio.FloatBuffer;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost extends ShaderParametersBase {

    FastRandom rand = new FastRandom();

    Texture vignetteTexture = Assets.getTexture("engine:vignette");
    Texture noiseTexture = Assets.getTexture("engine:noise");

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        PostProcessingRenderer.getInstance().getFBO("sceneBloom1").bindTexture();
        if (Config.getInstance().getBlurIntensity() != 0 || Config.getInstance().isMotionBlur()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            PostProcessingRenderer.getInstance().getFBO("sceneBlur1").bindTexture();
        }
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        scene.bindDepthTexture();

        if (Config.getInstance().isFilmGrain()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE5);
            glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture.getId());
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();

        program.setInt("texScene", 0);
        program.setInt("texBloom", 1);
        if (Config.getInstance().getBlurIntensity() != 0) {
            program.setInt("texBlur", 2);
        }
        program.setInt("texVignette", 3);
        program.setInt("texDepth", 4);

        if (Config.getInstance().isFilmGrain()) {
            program.setInt("texNoise", 5);
            program.setFloat("grainIntensity", 0.075f);
            program.setFloat("noiseOffset", rand.randomPosFloat());

            FloatBuffer rtSize = BufferUtils.createFloatBuffer(2);
            rtSize.put((float) scene._width).put((float) scene._height);
            rtSize.flip();

            program.setFloat2("renderTargetSize", rtSize);
        }

        if (CoreRegistry.get(LocalPlayer.class).isValid()) {
            Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            Block block = CoreRegistry.get(WorldProvider.class).getBlock(cameraPos);
            program.setInt("swimming", block.isLiquid() ? 1 : 0);
        }

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        if (activeCamera != null && Config.getInstance().isMotionBlur()) {
            program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix());
            program.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix());
        }
    }

}
