/*
 * Copyright 2016 MovingBlocks
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
import org.terasology.rendering.dag.nodes.InitialPostProcessingNode;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

/**
 * Shader parameters for the Post-processing shader program.
 *
 */
public class ShaderParametersToneMapping extends ShaderParametersBase {

    @Range(min = 0.0f, max = 10.0f)
    private float exposureBias = 1.0f;
    @Range(min = 0.0f, max = 100.0f)
    private float whitePoint = 9f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.
        ScreenGrabber screenGrabber = CoreRegistry.get(ScreenGrabber.class);

        // TODO: move into a tone mapping node
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        displayResolutionDependentFBOs.bindFboColorTexture(InitialPostProcessingNode.INITIAL_POST_FBO);
        program.setInt("texScene", 0, true);

        program.setFloat("exposure", screenGrabber.getExposure() * exposureBias, true);
        program.setFloat("whitePoint", whitePoint, true);
    }

}
