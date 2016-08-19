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
import org.terasology.rendering.dag.nodes.ShadowMapNode;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.fbms.ShadowMapResolutionDependentFBOs;
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the LightBufferPass shader program.
 *
 */
public class ShaderParametersLightGeometryPass extends ShaderParametersBase {

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        // TODO: obtain once in the superclass and monitor from there?
        // TODO: switch from CoreRegistry to Context.
        ShadowMapResolutionDependentFBOs shadowMapResolutionDependentFBOs = CoreRegistry.get(ShadowMapResolutionDependentFBOs.class);

        int texId = 0;
        if (READ_ONLY_GBUFFER.getFbo() != null) {
            // TODO: move content of this block into the node
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            READ_ONLY_GBUFFER.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            READ_ONLY_GBUFFER.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            READ_ONLY_GBUFFER.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++, true);
        }

        // TODO: monitor property by subscribing to it
        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()) {
            // TODO: move into node
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            shadowMapResolutionDependentFBOs.bindFboDepthTexture(ShadowMapNode.SHADOW_MAP);
            program.setInt("texSceneShadowMap", texId++, true);

            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera(); // TODO: shadowMapNode.camera here
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            if (lightCamera != null && activeCamera != null) {
                // TODO: move into material?
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix(), true);
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);

                Vector3f activeCameraToLightSpace = new Vector3f();
                activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
                program.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z, true);
            }

            if (CoreRegistry.get(Config.class).getRendering().isCloudShadows()) {
                // TODO: move into node - make sure to obtain texture only once and subscribe to it
                Texture clouds = Assets.getTexture("engine:perlinNoiseTileable").get();

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, clouds.getId());
                program.setInt("texSceneClouds", texId++, true);
            }
        }
    }

}
