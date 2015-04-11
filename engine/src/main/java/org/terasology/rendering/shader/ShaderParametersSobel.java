/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.editor.EditorRange;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.LwjglRenderingProcess;

import static org.terasology.rendering.assets.material.Material.StorageQualifier.UNIFORM;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel
 */
public class ShaderParametersSobel extends ShaderParametersBase {

    @EditorRange(min = 0.0f, max = 16.0f)
    float pixelOffsetX = 1.0f;
    @EditorRange(min = 0.0f, max = 16.0f)
    float pixelOffsetY = 1.0f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        FBO scene = LwjglRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            scene.bindDepthTexture();
            program.setInt(UNIFORM, "texDepth", 0);

            program.setFloat(UNIFORM, "texelWidth", 1.0f / scene.width);
            program.setFloat(UNIFORM, "texelHeight", 1.0f / scene.height);
        }

        program.setFloat(UNIFORM, "pixelOffsetX", pixelOffsetX);
        program.setFloat(UNIFORM, "pixelOffsetY", pixelOffsetY);
    }

}
