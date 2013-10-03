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
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.editor.EditorRange;
import org.terasology.engine.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.DefaultRenderingProcess;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Volumetric Lighting (ray marching pass) shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersVolumetricLightingRayMarching extends ShaderParametersBase {

    @EditorRange(min = 0.01f, max = 1.0f)
    private float volLightingDensity = 0.5f;
    @EditorRange(min = 0.001f, max = 0.1f)
    private float volLightingDecay = 0.005f;
    @EditorRange(min = -1.0f, max = -0.8f)
    private float volLightingScattering = -0.9f;
    @EditorRange(min = 0.0f, max = 10000.0f)
    private float volLightingPhi = 1000.0f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        int texId = 0;

        DefaultRenderingProcess.FBO sceneOpaque = DefaultRenderingProcess.getInstance().getFBO("sceneOpaque");

        if (CoreRegistry.get(Config.class).getRendering().isVolumetricLighting()) {

            if (sceneOpaque != null) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                sceneOpaque.bindDepthTexture();
                program.setInt("texSceneOpaqueDepth", texId++, true);
            }

            DefaultRenderingProcess.FBO volumetricLighting = DefaultRenderingProcess.getInstance().getFBO("volumetricLighting");

            program.setFloat2("texelSize", 1.0f / volumetricLighting.width,  1.0f / volumetricLighting.height, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
            program.setInt("texSceneShadowMap", texId++, true);

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            if (lightCamera != null && activeCamera != null) {
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);

                program.setMatrix4("lightViewMatrix", lightCamera.getViewMatrix(), true);
                program.setMatrix4("lightProjMatrix", lightCamera.getProjectionMatrix(), true);
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix(), true);

                Vector3f activeCameraToLightSpace = new Vector3f();
                activeCameraToLightSpace.sub(activeCamera.getPosition(), lightCamera.getPosition());
                program.setFloat3("activeCameraToLightSpace", activeCameraToLightSpace.x, activeCameraToLightSpace.y, activeCameraToLightSpace.z, true);
            }

            program.setFloat4("volumetricLightingSettings", volLightingDensity, volLightingDecay, volLightingPhi, volLightingScattering, true);

            if (CoreRegistry.get(Config.class).getRendering().isCloudShadows()) {
                Texture clouds = Assets.getTexture("engine:perlinNoiseTileable");
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                glBindTexture(GL11.GL_TEXTURE_2D, clouds.getId());
                program.setInt("texSceneClouds", texId++, true);
            }
        }
    }
}
