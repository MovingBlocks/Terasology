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
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.PostProcessingRenderer;
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
    Property maxBlurSky = new Property("maxBlurSky", 1.0f, 0.0f, 1.0f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("sceneOpaque");

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        PostProcessingRenderer.getInstance().getFBO("sceneToneMapped").bindTexture();
        program.setInt("texScene", 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        PostProcessingRenderer.getInstance().getFBO("sceneBloom1").bindTexture();
        program.setInt("texBloom", 1);

        if (CoreRegistry.get(Config.class).getRendering().getBlurIntensity() != 0 || CoreRegistry.get(Config.class).getRendering().isMotionBlur()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE2);
            PostProcessingRenderer.getInstance().getFBO("sceneBlur1").bindTexture();
            program.setInt("texBlur", 2);

            program.setFloat("maxBlurSky", (Float) maxBlurSky.getValue());
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.getId());
        program.setInt("texVignette", 3);

        GL13.glActiveTexture(GL13.GL_TEXTURE4);
        scene.bindDepthTexture();
        program.setInt("texDepth", 4);

        if (CoreRegistry.get(Config.class).getRendering().isFilmGrain()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE5);
            glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture.getId());
            program.setInt("texNoise", 5);
            program.setFloat("grainIntensity", (Float) filmGrainIntensity.getValue());
            program.setFloat("noiseOffset", rand.randomPosFloat());

            FloatBuffer rtSize = BufferUtils.createFloatBuffer(2);
            rtSize.put((float) scene._width).put((float) scene._height);
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
        properties.add(maxBlurSky);
    }
}
