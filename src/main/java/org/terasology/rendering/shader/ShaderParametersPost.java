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

import org.lwjgl.opengl.*;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.rendering.assets.GLSLShaderProgram;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost extends ShaderParametersBase {

    FastRandom rand = new FastRandom();

    Texture filmGrainNoiseTexture = Assets.getTexture("engine:noise");
    Property filmGrainIntensity = new Property("filmGrainIntensity", 0.025f, 0.0f, 1.0f);

    Property blurStart = new Property("blurStart", 0.0f, 0.0f, 1.0f);
    Property blurLength = new Property("blurLength", 0.025f, 0.0f, 1.0f);

    @Override
    public void applyParameters(GLSLShaderProgram program) {
        super.applyParameters(program);

        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        LocalPlayerSystem localPlayerSystem = CoreRegistry.get(LocalPlayerSystem.class);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneToneMapped");
        program.setInt("texScene", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneBloom1");
        program.setInt("texBloom", texId++);

        if (CoreRegistry.get(Config.class).getRendering().getBlurIntensity() != 0) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().getFBO("sceneBlur1").bindTexture();
            program.setInt("texBlur", texId++);

            if (localPlayerSystem != null) {
                program.setFloat("blurFocusDistance", localPlayerSystem.getEyeFocusDistance());
            }

            program.setFloat("blurStart", (Float) blurStart.getValue());
            program.setFloat("blurLength", (Float) blurLength.getValue());
        }

        Texture vignetteTexture = Assets.getTexture("engine:vignette");

        if (vignetteTexture != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
            program.setInt("texVignette", texId++);
        }

        Texture colorGradingLut = Assets.getTexture("engine:colorGradingLut1");

        if (colorGradingLut != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL12.GL_TEXTURE_3D, colorGradingLut.getId());
            program.setInt("texColorGradingLut", texId++);
        }

        Vector3f tint = worldRenderer.getTint();
        program.setFloat3("inLiquidTint", tint.x, tint.y, tint.z);

        DefaultRenderingProcess.FBO sceneCombined = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (sceneCombined != null) {

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneCombined.bindDepthTexture();
            program.setInt("texDepth", texId++);

            if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, filmGrainNoiseTexture.getId());
                program.setInt("texNoise", texId++);
                program.setFloat("grainIntensity", (Float) filmGrainIntensity.getValue());
                program.setFloat("noiseOffset", rand.randomPosFloat());

                program.setFloat2("noiseSize", filmGrainNoiseTexture.getWidth(), filmGrainNoiseTexture.getHeight());
                program.setFloat2("renderTargetSize", sceneCombined.width, sceneCombined.height);
            }
        }

        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        if (activeCamera != null && CoreRegistry.get(Config.class).getRendering().isMotionBlur()) {
            program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix());
            program.setMatrix4("prevViewProjMatrix", activeCamera.getPrevViewProjectionMatrix());
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(filmGrainIntensity);
        properties.add(blurStart);
        properties.add(blurLength);
    }
}
