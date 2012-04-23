/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
import org.terasology.game.CoreRegistry;
import org.terasology.game.Terasology;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.TextureManager;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Shader parameters for the Gel. Cube shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersGelCube implements IShaderParameters {

    public void applyParameters(ShaderProgram program) {
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        WorldRenderer worldRendererd = CoreRegistry.get(WorldRenderer.class);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        TextureManager.getInstance().bindTexture("slime");

        if (localPlayer != null) {
            program.setInt("carryingTorch", localPlayer.isCarryingTorch() ? 1 : 0);
        }
        if (worldRendererd != null) {
            program.setFloat("tick", (float) worldRendererd.getTick());
        }
    }

}
