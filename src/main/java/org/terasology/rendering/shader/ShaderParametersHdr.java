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
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.PostProcessingRenderer;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersHdr extends ShaderParametersBase {

    static final float exposureBias = 2.5f;

    public void applyParameters(ShaderProgram program) {
        super.applyParameters(program);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        PostProcessingRenderer.getInstance().getFBO("scenePrePost").bindTexture();

        program.setInt("texScene", 0);
        program.setFloat("exposure", PostProcessingRenderer.getInstance().getExposure() *exposureBias);
    }

}
