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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.controllers.LocalPlayerSystem;
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.FastRandom;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPost extends ShaderParametersBase {

    FastRandom rand = new FastRandom();

    Texture vignetteTexture = Assets.getTexture("engine:vignette");
    Texture noiseTexture = Assets.getTexture("engine:noise");

    Property filmGrainIntensity = new Property("filmGrainIntensity", 0.1f, 0.0f, 1.0f);
    Property blurStart = new Property("blurStart", 0.1f, 0.0f, 1.0f);
    Property blurLength = new Property("blurLength", 0.1f, 0.0f, 1.0f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        LocalPlayerSystem localPlayerSystem = CoreRegistry.get(LocalPlayerSystem.class);

        DefaultRenderingProcess.FBO sceneCombined = DefaultRenderingProcess.getInstance().getFBO("sceneCombined");

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().getFBO("sceneToneMapped").bindTexture();
        program.setInt("texScene", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().getFBO("sceneBloom1").bindTexture();
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

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
        program.setInt("texVignette", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneCombined.bindDepthTexture();
        program.setInt("texDepth", texId++);

        if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture.getId());
            program.setInt("texNoise", texId++);
            program.setFloat("grainIntensity", (Float) filmGrainIntensity.getValue());
            program.setFloat("noiseOffset", rand.randomPosFloat());

            FloatBuffer rtSize = BufferUtils.createFloatBuffer(2);
            rtSize.put((float) sceneCombined.width).put((float) sceneCombined.height);
            rtSize.flip();

            program.setFloat2("renderTargetSize", rtSize);
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
