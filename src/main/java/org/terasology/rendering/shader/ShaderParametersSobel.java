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
import org.terasology.logic.manager.DefaultRenderingProcess;

import java.util.List;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersSobel extends ShaderParametersBase {

    Property pixelOffsetX = new Property("pixelOffsetX", 1.0f, 0.0f, 16.0f);
    Property pixelOffsetY = new Property("pixelOffsetY", 1.0f, 0.0f, 16.0f);

    @Override
    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        DefaultRenderingProcess.FBO scene = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindDepthTexture();
        program.setInt("texDepth", 0);

        program.setFloat("texelWidth", 1.0f / scene.width);
        program.setFloat("texelHeight", 1.0f / scene.height);

        program.setFloat("pixelOffsetX", (Float) pixelOffsetX.getValue());
        program.setFloat("pixelOffsetY", (Float) pixelOffsetY.getValue());
    }

    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(pixelOffsetX);
        properties.add(pixelOffsetY);
    }
}
