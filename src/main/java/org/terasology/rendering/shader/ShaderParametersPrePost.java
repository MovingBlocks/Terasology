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

import java.util.List;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersPrePost extends ShaderParametersBase {

    Property abberationOffsetX = new Property("abberationOffsetX", 0.0f, 0.0f, 0.1f);
    Property abberationOffsetY = new Property("abberationOffsetY", 0.0f, 0.0f, 0.1f);

    @Override
    public void applyParameters(GLSLShaderProgram program) {
        super.applyParameters(program);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        DefaultRenderingProcess.getInstance().bindFboTexture("sceneOpaque");
        program.setInt("texScene", texId++);

        program.setFloat2("abberationOffset", (Float) abberationOffsetX.getValue(), (Float) abberationOffsetY.getValue());

        if (CoreRegistry.get(Config.class).getRendering().isLightShafts()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("lightShafts");
            program.setInt("texLightShafts", texId++);
        }
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(abberationOffsetX);
        properties.add(abberationOffsetY);
    }
}
