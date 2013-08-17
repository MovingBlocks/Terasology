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
import org.terasology.rendering.opengl.DefaultRenderingProcess;

/**
 * Shader parameters for the Post-processing shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersHdr extends ShaderParametersBase {

    @EditorRange(min = 0.0f, max = 10.0f)
    private float exposureBias = 1.0f;
    @EditorRange(min = 0.0f, max = 100.0f)
    private float whitePoint = 5.0f;

    public void applyParameters(Material program) {
        super.applyParameters(program);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        DefaultRenderingProcess.getInstance().bindFboTexture("scenePrePost");

        program.setInt("texScene", 0, true);
        program.setFloat("exposure", DefaultRenderingProcess.getInstance().getExposure() * exposureBias, true);
        program.setFloat("whitePoint", whitePoint, true);
    }

}
