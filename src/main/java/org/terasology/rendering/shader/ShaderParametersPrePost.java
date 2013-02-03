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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.asset.Assets;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;
import org.terasology.rendering.assets.Texture;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPrePost extends ShaderParametersBase {

    Texture noiseTexture = Assets.getTexture("engine:noise");

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

        if (Config.getInstance().isSSAO()) {
            PostProcessingRenderer.FBO ssao = PostProcessingRenderer.getInstance().getFBO("ssaoBlurred1");
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            ssao.bindTexture();
            program.setInt("texSsao", 1);
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindTexture();
        program.setInt("texScene", 0);

        GL13.glActiveTexture(GL13.GL_TEXTURE2);
        scene.bindDepthTexture();
        program.setInt("texDepth", 2);

        PostProcessingRenderer.FBO sobel = PostProcessingRenderer.getInstance().getFBO("sobel");
        GL13.glActiveTexture(GL13.GL_TEXTURE3);
        sobel.bindTexture();
        program.setInt("texEdges", 3);

        program.setFloat("outlineDepthThreshold", 0.05f);
        program.setFloat("outlineThickness", 1.0f);
    }

}
