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
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * Shader parameters for the Light Shafts shader program.
 *
 */
public class ShaderParametersLightShafts extends ShaderParametersBase {
    @Range(min = 0.0f, max = 10.0f)
    private float density = 1.0f;
    @Range(min = 0.0f, max = 0.01f)
    private float exposure = 0.0075f;
    @Range(min = 0.0f, max = 10.0f)
    private float weight = 8.0f;
    @Range(min = 0.0f, max = 0.99f)
    private float decay = 0.95f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.
        WorldRenderer worldRenderer = CoreRegistry.get(WorldRenderer.class);
        BackdropProvider backdropProvider = CoreRegistry.get(BackdropProvider.class);

        FBO sceneOpaqueFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);

        int texId = 0;

        // TODO: - move into node
        // TODO: - many null checks are happening in these shader parameter classes. I feel they are unnecessary
        // TODO:   as those objects should never be null. If they are I'd be happy receiving an NPE and debugging as needed.
        if (sceneOpaqueFbo != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindTexture();
            program.setInt("texScene", texId++, true);
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindDepthTexture();
            program.setInt("texDepth", texId++, true);
        }

        // TODO: move into Material?
        program.setFloat("density", density, true);
        program.setFloat("exposure", exposure, true);
        program.setFloat("weight", weight, true);
        program.setFloat("decay", decay, true);

        // TODO: eliminate null check?
        if (worldRenderer != null) {
            // TODO: move into Material?
            Vector3f sunDirection = backdropProvider.getSunDirection(true);

            Camera activeCamera = worldRenderer.getActiveCamera();

            Vector4f sunPositionWorldSpace4 = new Vector4f(sunDirection.x * 10000.0f, sunDirection.y * 10000.0f, sunDirection.z * 10000.0f, 1.0f);
            Vector4f sunPositionScreenSpace = new Vector4f(sunPositionWorldSpace4);
            activeCamera.getViewProjectionMatrix().transform(sunPositionScreenSpace);

            sunPositionScreenSpace.x /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.y /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.z /= sunPositionScreenSpace.w;
            sunPositionScreenSpace.w = 1.0f;

            program.setFloat("lightDirDotViewDir", activeCamera.getViewingDirection().dot(sunDirection), true);
            program.setFloat2("lightScreenPos", (sunPositionScreenSpace.x + 1.0f) / 2.0f, (sunPositionScreenSpace.y + 1.0f) / 2.0f, true);
        }
    }
}
