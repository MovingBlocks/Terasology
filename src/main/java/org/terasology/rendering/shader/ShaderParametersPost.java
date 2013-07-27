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
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.input.CameraTargetSystem;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.procedural.FastRandom;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost extends ShaderParametersBase {

    FastRandom rand = new FastRandom();

    float filmGrainIntensity = 0.025f;

    float blurStart = 0.0f;
    float blurLength = 0.15f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        CameraTargetSystem cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneToneMapped");
        program.setInt("texScene", texId++, true);

        if (CoreRegistry.get(Config.class).getRendering().getBlurIntensity() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().getFBO("sceneBlur1").bindTexture();
            program.setInt("texBlur", texId++, true);

            if (cameraTargetSystem != null) {
                program.setFloat("blurFocusDistance", cameraTargetSystem.getEyeFocusDistance(), true);
            }

            program.setFloat("blurStart", blurStart, true);
            program.setFloat("blurLength", blurLength, true);
        }

        Texture colorGradingLut = Assets.getTexture("engine:colorGradingLut1");

        if (colorGradingLut != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL12.GL_TEXTURE_3D, colorGradingLut.getId());
            program.setInt("texColorGradingLut", texId++, true);
        }

        DefaultRenderingProcess.FBO sceneCombined = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (sceneCombined != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneCombined.bindDepthTexture();
            program.setInt("texDepth", texId++, true);

            Texture filmGrainNoiseTexture = Assets.getTexture("engine:noise");

            if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, filmGrainNoiseTexture.getId());
                program.setInt("texNoise", texId++, true);
                program.setFloat("grainIntensity", filmGrainIntensity, true);
                program.setFloat("noiseOffset", rand.randomPosFloat(), true);

                program.setFloat2("noiseSize", filmGrainNoiseTexture.getWidth(), filmGrainNoiseTexture.getHeight(), true);
                program.setFloat2("renderTargetSize", sceneCombined.width, sceneCombined.height, true);
            }
        }

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (activeCamera != null && CoreRegistry.get(Config.class).getRendering().isMotionBlur()) {
            program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            program.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix(), true);
        }
    }

}
