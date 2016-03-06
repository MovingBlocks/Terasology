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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
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

        FrameBuffersManager buffersManager = CoreRegistry.get(FrameBuffersManager.class);
        FBO sceneOpaque = buffersManager.getFBO("sceneOpaque");

        int texId = 0;
        if (sceneOpaque != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isDynamicShadows()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            buffersManager.bindFboDepthTexture("sceneShadowMap");
            program.setInt("texSceneShadowMap", texId++, true);

            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

            if (lightCamera != null && activeCamera != null) {
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix(), true);
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);

                Vector3f activeCameraToLightSpace = new Vector3f();
                activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
                program.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z, true);
            }

            if (CoreRegistry.get(Config.class).getRendering().isCloudShadows()) {
                Texture clouds = Assets.getTexture("engine:perlinNoiseTileable").get();

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, clouds.getId());
                program.setInt("texSceneClouds", texId++, true);
            }
        }
    }

}
