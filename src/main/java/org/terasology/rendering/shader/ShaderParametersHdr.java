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
import org.terasology.logic.manager.DefaultRenderingProcess;
import org.terasology.editor.properties.Property;

import java.util.List;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersHdr extends ShaderParametersBase {

    final Property exposureBias = new Property("exposureBias", 4.0f, 0.0f, 10.0f);
    final Property whitePoint = new Property("whitePoint", 11.2f, 0.0f, 100.0f);

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        DefaultRenderingProcess.getInstance().getFBO("scenePrePost").bindTexture();

        program.setInt("texScene", 0);
        program.setFloat("exposure", DefaultRenderingProcess.getInstance().getExposure() * (Float) exposureBias.getValue());
        program.setFloat("whitePoint", (Float) whitePoint.getValue());
    }

    public void addPropertiesToList(List<Property> properties) {
        properties.add(exposureBias);
        properties.add(whitePoint);
    }
}
