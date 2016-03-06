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
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.terasology.utilities.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.input.cameraTarget.CameraTargetSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureUtil;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 */
public class ShaderParametersPost extends ShaderParametersBase {

    private Random rand = new FastRandom();

    @Range(min = 0.0f, max = 1.0f)
    private float filmGrainIntensity = 0.05f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        CameraTargetSystem cameraTargetSystem = CoreRegistry.get(CameraTargetSystem.class);
        FrameBuffersManager buffersManager = CoreRegistry.get(FrameBuffersManager.class);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffersManager.bindFboColorTexture("sceneToneMapped");
        program.setInt("texScene", texId++, true);

        if (CoreRegistry.get(Config.class).getRendering().getBlurIntensity() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            buffersManager.getFBO("sceneBlur1").bindTexture();
            program.setInt("texBlur", texId++, true);

            if (cameraTargetSystem != null) {
                program.setFloat("focalDistance", cameraTargetSystem.getFocalDistance(), true); //for use in DOF effect
            }
        }

        Texture colorGradingLut = Assets.getTexture("engine:colorGradingLut1").get();

        if (colorGradingLut != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL12.GL_TEXTURE_3D, colorGradingLut.getId());
            program.setInt("texColorGradingLut", texId++, true);
        }

        FBO sceneCombined = buffersManager.getFBO("sceneOpaque");

        if (sceneCombined != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneCombined.bindDepthTexture();
            program.setInt("texDepth", texId++, true);

            ResourceUrn noiseTextureUri = TextureUtil.getTextureUriForWhiteNoise(1024, 0x1234, 0, 512);
            Texture filmGrainNoiseTexture = Assets.getTexture(noiseTextureUri).get();

            if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, filmGrainNoiseTexture.getId());
                program.setInt("texNoise", texId++, true);
                program.setFloat("grainIntensity", filmGrainIntensity, true);
                program.setFloat("noiseOffset", rand.nextFloat(), true);

                program.setFloat2("noiseSize", filmGrainNoiseTexture.getWidth(), filmGrainNoiseTexture.getHeight(), true);
                program.setFloat2("renderTargetSize", sceneCombined.width(), sceneCombined.height(), true);
            }
        }

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (activeCamera != null && CoreRegistry.get(Config.class).getRendering().isMotionBlur()) {
            program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            program.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix(), true);
        }
    }

}
