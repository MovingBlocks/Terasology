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
import org.terasology.world.WorldProvider;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.glBindTexture;

/**
 * Shader parameters for the Combine shader program.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ShaderParametersCombine extends ShaderParametersBase {
    @EditorRange(min = 0.001f, max = 0.005f)
    private float outlineDepthThreshold = 0.001f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float outlineThickness = 0.65f;

    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringLength = 0.25f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringStrength = 0.35f;
    @EditorRange(min = 0.0f, max = 1.0f)
    private float skyInscatteringThreshold = 0.75f;

    @EditorRange(min = 0.001f, max = 1.0f)
    private float volFogDensityAtViewer = 0.15f;
    @EditorRange(min = 0.01f, max = 1.0f)
    private float volFogGlobalDensity = 0.15f;
    @EditorRange(min = 0.01f, max = 1.0f)
    private float volFogHeightFalloff = 0.05f;

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

        if (sceneOpaque != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaque.bindTexture();
            program.setInt("texSceneOpaque", texId++, true);

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

        if (CoreRegistry.get(Config.class).getRendering().isVolumetricFog()) {
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            }

            Vector3f fogWorldPosition = new Vector3f(0.0f, 32.0f - activeCamera.getPosition().y, 0.0f);
            program.setFloat3("fogWorldPosition", fogWorldPosition.x, fogWorldPosition.y, fogWorldPosition.z, true);

            // Fog density is set according to the fog density provided by the world
            // TODO: The 50% percent limit shouldn't be hardcoded
            final float worldFog = Math.min(CoreRegistry.get(WorldProvider.class).getFog(activeCamera.getPosition()), 0.5f);
            program.setFloat4("volumetricFogSettings", volFogDensityAtViewer, volFogGlobalDensity, volFogHeightFalloff, worldFog);
        }

        if (CoreRegistry.get(Config.class).getRendering().isVolumetricFog()
                || CoreRegistry.get(Config.class).getRendering().isVolumetricLighting()) {
            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            if (activeCamera != null) {
                program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            }
        }

        if (CoreRegistry.get(Config.class).getRendering().isVolumetricLighting()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboDepthTexture("sceneShadowMap");
            program.setInt("texSceneShadowMap", texId++, true);

            Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
            Camera lightCamera = CoreRegistry.get(WorldRenderer.class).getLightCamera();
            if (lightCamera != null && activeCamera != null) {
                program.setMatrix4("lightViewMatrix", lightCamera.getViewMatrix(), true);
                program.setMatrix4("lightProjMatrix", lightCamera.getProjectionMatrix(), true);
                program.setMatrix4("lightViewProjMatrix", lightCamera.getViewProjectionMatrix(), true);

                program.setMatrix4("viewMatrix", activeCamera.getViewMatrix(), true);

                Matrix4f invViewMatrix = new Matrix4f();
                invViewMatrix.invert(activeCamera.getViewMatrix());

                program.setMatrix4("invViewMatrix", invViewMatrix, true);

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

        DefaultRenderingProcess.FBO sceneTransparent = DefaultRenderingProcess.getInstance().getFBO("sceneTransparent");

        if (sceneTransparent != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneTransparent.bindTexture();
            program.setInt("texSceneTransparent", texId++, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isSsao()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("ssaoBlurred");
            program.setInt("texSsao", texId++, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sobel");
            program.setInt("texEdges", texId++, true);

            program.setFloat("outlineDepthThreshold", outlineDepthThreshold, true);
            program.setFloat("outlineThickness", outlineThickness, true);
        }

        if (CoreRegistry.get(Config.class).getRendering().isInscattering()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            DefaultRenderingProcess.getInstance().bindFboTexture("sceneSkyBand1");
            program.setInt("texSceneSkyBand", texId++, true);

            Vector4f skyInscatteringSettingsFrag = new Vector4f();
            skyInscatteringSettingsFrag.y = skyInscatteringStrength;
            skyInscatteringSettingsFrag.z = skyInscatteringLength;
            skyInscatteringSettingsFrag.w = skyInscatteringThreshold;
            program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag, true);
        }
    }
}
