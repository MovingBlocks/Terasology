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
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.editor.properties.Property;
import org.terasology.rendering.assets.Texture;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSSAO extends ShaderParametersBase {

    Property ssaoStrength = new Property("ssaoStrength", 0.08f, 0.0f, 1.0f);
    Property ssaoTotalStrength = new Property("ssaoTotalStrength", 2.0f, 0.0f, 4.0f);
    Property ssaoFalloff = new Property("ssaoFalloff", 0.0f, 0.0f, 0.0001f);
    Property ssaoRad = new Property("ssaoRad", 0.02f, 0.00f, 0.2f);

    Texture noiseTexture = Assets.getTexture("engine:noise");

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO scene = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindDepthTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        scene.bindNormalsTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        glBindTexture(GL11.GL_TEXTURE_2D, noiseTexture.getId());

        program.setInt("texDepth", 0);
        program.setInt("texNormals", 1);
        program.setInt("texNoise", 2);

        program.setFloat("ssaoStrength", (Float) ssaoStrength.getValue());
        program.setFloat("ssaoTotalStrength", (Float) ssaoTotalStrength.getValue());
        program.setFloat("ssaoFalloff", (Float) ssaoFalloff.getValue());
        program.setFloat("ssaoRad", (Float) ssaoRad.getValue());

        FloatBuffer rtSize = BufferUtils.createFloatBuffer(2);
        rtSize.put((float) scene.width).put((float) scene.height);
        rtSize.flip();

        program.setFloat2("renderTargetSize", rtSize);
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(ssaoStrength);
        properties.add(ssaoRad);
        properties.add(ssaoTotalStrength);
        properties.add(ssaoFalloff);
    }
}
