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
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.rendering.assets.Texture;

import javax.vecmath.Vector3f;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSSAO implements IShaderParameters {

    Texture noiseTexture = Assets.getTexture("engine:noise");

    @Override
    public void applyParameters(ShaderProgram program) {
        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindDepthTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        scene.bindNormalsTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture.getId());

        program.setInt("texDepth", 0);
        program.setInt("texNormals", 1);
        program.setInt("texNoise", 2);

        final int ssaoSamples = 16;
        program.setInt("ssaoSamples", ssaoSamples);
        program.setFloat("ssaoInvSamples", 1.0f / ssaoSamples);
        program.setFloat("ssaoStrength", 0.15f);
        program.setFloat("ssaoTotalStrength", 1.25f);
        program.setFloat("ssaoFalloff", 0.0000001f);
        program.setFloat("ssaoRad", 0.05f);

        FloatBuffer rtSize = BufferUtils.createFloatBuffer(2);
        rtSize.put((float) scene._width).put((float) scene._height);
        rtSize.flip();

        program.setFloat2("renderTargetSize", rtSize);
    }

}
