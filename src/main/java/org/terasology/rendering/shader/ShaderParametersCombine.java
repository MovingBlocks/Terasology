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
import org.terasology.rendering.assets.GLSLShaderProgram;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;

import javax.vecmath.Vector4f;
import java.util.List;

/**
 * Shader parameters for the Combine shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersCombine extends ShaderParametersBase {

    private Property outlineDepthThreshold = new Property("outlineDepthThreshold", 0.01f, 0.001f, 0.1f);
    private Property outlineThickness = new Property("outlineThickness", 1.0f);

    Property skyInscatteringLength = new Property("skyInscatteringLength", 0.25f, 0.0f, 1.0f);
    Property skyInscatteringStrength = new Property("skyInscatteringStrength", 0.3f, 0.0f, 1.0f);
    Property skyInscatteringThreshold = new Property("skyInscatteringThreshold", 1.0f, 0.0f, 1.0f);

    @Override
    public void applyParameters(GLSLShaderProgram program) {
        super.applyParameters(program);

        int texId = 0;

        DefaultRenderingProcess.FBO sceneOpaque = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (sceneOpaque != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindTexture();
            program.setInt("texSceneOpaque", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++);
        }

        DefaultRenderingProcess.FBO sceneTransparent = DefaultRenderingProcess.getInstance().getFBO("sceneTransparent");

        if (sceneTransparent != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneTransparent.bindTexture();
            program.setInt("texSceneTransparent", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isSsao()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred1");
            program.setInt("texSsao", texId++);
        }

        if (CoreRegistry.get(Config.class).getRendering().isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
            program.setInt("texEdges", texId++);

            program.setFloat("outlineDepthThreshold", (Float) outlineDepthThreshold.getValue());
            program.setFloat("outlineThickness", (Float) outlineThickness.getValue());
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
        program.setInt("texSceneSkyBand", texId++);

        Vector4f skyInscatteringSettingsFrag = new Vector4f();
        skyInscatteringSettingsFrag.y = (Float) skyInscatteringStrength.getValue();
        skyInscatteringSettingsFrag.z = (Float) skyInscatteringLength.getValue();
        skyInscatteringSettingsFrag.w = (Float) skyInscatteringThreshold.getValue();
        program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag);
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(skyInscatteringLength);
        properties.add(skyInscatteringStrength);
        properties.add(skyInscatteringThreshold);
        properties.add(outlineThickness);
        properties.add(outlineDepthThreshold);
    }
}
