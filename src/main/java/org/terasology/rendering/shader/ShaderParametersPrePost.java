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

import org.lwjgl.opengl.GL13;
import org.terasology.config.Config;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.PostProcessingRenderer;

import java.util.List;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPrePost extends ShaderParametersBase {

    private Property outlineDepthThreshold = new Property("outlineDepthThreshold", 0.1f);
    private Property outlineThickness = new Property("outlineThickness", 1.0f);
    private Property shoreFactor = new Property("shoreFactor", 15.0f, 0.0f, 100.0f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        PostProcessingRenderer.FBO sceneOpaque = PostProcessingRenderer.getInstance().getFBO("sceneOpaque");

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindTexture();
        program.setInt("texSceneOpaque", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindDepthTexture();
        program.setInt("texSceneOpaqueDepth", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindNormalsTexture();
        program.setInt("texSceneOpaqueNormals", texId++);

        PostProcessingRenderer.FBO sceneTransparent = PostProcessingRenderer.getInstance().getFBO("sceneTransparent");

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneTransparent.bindTexture();
        program.setInt("texSceneTransparent", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneTransparent.bindDepthTexture();
        program.setInt("texSceneTransparentDepth", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneTransparent.bindNormalsTexture();
        program.setInt("texSceneTransparentNormals", texId++);

        if (CoreRegistry.get(Config.class).getRendering().isSsao()) {
            PostProcessingRenderer.FBO ssao = PostProcessingRenderer.getInstance().getFBO("ssaoBlurred1");
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            ssao.bindTexture();
            program.setInt("texSsao", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isOutline()) {
            PostProcessingRenderer.FBO sobel = PostProcessingRenderer.getInstance().getFBO("sobel");
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sobel.bindTexture();
            program.setInt("texEdges", texId++);

            program.setFloat("outlineDepthThreshold", (Float) outlineDepthThreshold.getValue());
            program.setFloat("outlineThickness", (Float) outlineThickness.getValue());
        }

        if (CoreRegistry.get(Config.class).getRendering().isLightShafts()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            PostProcessingRenderer.getInstance().getFBO("lightShafts").bindTexture();
            program.setInt("texLightShafts", texId++);
        }

        program.setFloat("shoreFactor", (Float) shoreFactor.getValue());
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(outlineThickness);
        properties.add(outlineDepthThreshold);
        properties.add(shoreFactor);
    }
}
