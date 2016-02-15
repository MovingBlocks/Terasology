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
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;

/**
 * Shader parameters for the Post-processing shader program.
 *
 */
public class ShaderParametersSobel extends ShaderParametersBase {

    @Range(min = 0.0f, max = 16.0f)
    float pixelOffsetX = 1.0f;
    @Range(min = 0.0f, max = 16.0f)
    float pixelOffsetY = 1.0f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        FBO scene = CoreRegistry.get(FrameBuffersManager.class).getFBO("sceneOpaque");

        if (scene != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            scene.bindDepthTexture();
            program.setInt("texDepth", 0);

            program.setFloat("texelWidth", 1.0f / scene.width());
            program.setFloat("texelHeight", 1.0f / scene.height());
        }

        program.setFloat("pixelOffsetX", pixelOffsetX);
        program.setFloat("pixelOffsetY", pixelOffsetY);
    }

}
