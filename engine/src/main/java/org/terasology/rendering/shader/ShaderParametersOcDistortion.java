/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.assets.ResourceUrn;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.FINAL_BUFFER;

/**
 * Shader parameters for the Combine shader program.
 */
public class ShaderParametersOcDistortion extends ShaderParametersBase {
    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        // TODO: obtain once in superclass? The super class could then have the monitoring functionality.
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.

        // TODO: move into node
        int texId = 0;
        // TODO: convert the next three lines in a public method somewhere.
        // TODO: In the BaseMaterial class perhaps? Or an even more generic utility class?
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        displayResolutionDependentFBOs.bindFboColorTexture(FINAL_BUFFER);
        program.setInt("texSceneFinal", texId, true);
    }

}
