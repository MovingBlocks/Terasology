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
import org.terasology.editor.properties.Property;
import org.terasology.rendering.assets.GLSLShaderProgram;
import org.terasology.rendering.renderingProcesses.DefaultRenderingProcess;

import java.util.List;

/**
 * Shader parameters for the LightBufferPass shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersLightGeometryPass extends ShaderParametersBase {
    @Override
    public void applyParameters(GLSLShaderProgram program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO sceneOpaque = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        int texId = 0;
        if (sceneOpaque != null) {
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
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
    }
}
