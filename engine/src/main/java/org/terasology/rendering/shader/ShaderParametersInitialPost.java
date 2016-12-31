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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.nodes.BloomPassesNode;
import org.terasology.rendering.dag.nodes.LightShaftsNode;
import org.terasology.rendering.opengl.DefaultDynamicFBOs;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.util.Optional;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Post-processing shader program.
 *
 */
public class ShaderParametersInitialPost extends ShaderParametersBase {

    @Range(min = 0.0f, max = 0.1f)
    float aberrationOffsetX;
    @Range(min = 0.0f, max = 0.1f)
    float aberrationOffsetY;

    @Range(min = 0.0f, max = 1.0f)
    float bloomFactor = 0.5f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        // TODO: often used objects: perhaps to be obtained in BaseMaterial?
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.
        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);

        // TODO: move what follows into material and/or node
        Vector3f tint = worldProvider.getBlock(activeCamera.getPosition()).getTint();
        program.setFloat3("inLiquidTint", tint.x, tint.y, tint.z, true);

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        displayResolutionDependentFBOs.bindFboColorTexture(DefaultDynamicFBOs.READ_ONLY_GBUFFER.getName());
        program.setInt("texScene", texId++, true);

        // TODO: monitor config parameter by subscribing to it
        if (CoreRegistry.get(Config.class).getRendering().isBloom()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.bindFboColorTexture(BloomPassesNode.BLOOM_2);
            program.setInt("texBloom", texId++, true);

            program.setFloat("bloomFactor", bloomFactor, true);
        }

        program.setFloat2("aberrationOffset", aberrationOffsetX, aberrationOffsetY, true);

        // TODO: monitor config parameter by subscribing to it
        if (CoreRegistry.get(Config.class).getRendering().isLightShafts()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.bindFboColorTexture(LightShaftsNode.LIGHT_SHAFTS_FBO);
            program.setInt("texLightShafts", texId++, true);
        }

        // TODO: obtain once, monitor using Texture.subscribeToDisposal(Runnable)
        Optional<? extends Texture> vignetteTexture = Assets.getTexture("engine:vignette");

        if (vignetteTexture.isPresent()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            glBindTexture(GL11.GL_TEXTURE_2D, vignetteTexture.get().getId());
            program.setInt("texVignette", texId++, true);
        }
    }
}
