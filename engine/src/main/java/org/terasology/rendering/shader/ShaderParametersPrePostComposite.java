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
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.nodes.BlurredAmbientOcclusionNode;
import org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.HazeNode;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * Shader parameters for the Combine shader program.
 *
 */
public class ShaderParametersPrePostComposite extends ShaderParametersBase {
    @Range(min = 0.001f, max = 0.005f)
    private float outlineDepthThreshold = 0.001f;
    @Range(min = 0.0f, max = 1.0f)
    private float outlineThickness = 0.65f;

    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringLength = 1.0f;
    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringStrength = 0.25f;
    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringThreshold = 0.8f;

    @Override
    public void applyParameters(Material program) {
        super.applyParameters(program);

        int texId = 0;
        // TODO: obtain these objects once in superclass and add there monitoring functionality as needed?
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = CoreRegistry.get(DisplayResolutionDependentFBOs.class); // TODO: switch from CoreRegistry to Context.

        FBO sceneOpaqueFbo = displayResolutionDependentFBOs.get(READONLY_GBUFFER);

        // TODO: move texture bindings to the appropriate nodes
        if (sceneOpaqueFbo != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindTexture();
            program.setInt("texSceneOpaque", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++, true);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneOpaqueFbo.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++, true);
        }

        FBO sceneReflectiveRefractive = displayResolutionDependentFBOs.get(RefractiveReflectiveBlocksNode.REFRACTIVE_REFLECTIVE_FBO);

        if (sceneReflectiveRefractive != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            sceneReflectiveRefractive.bindTexture();
            program.setInt("texSceneReflectiveRefractive", texId++, true);
        }

        RenderingConfig renderingConfig = CoreRegistry.get(Config.class).getRendering();
        Camera activeCamera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();

        // TODO: review - unnecessary? Probably from a time when shaders were initialized
        // TODO:          on application startup rather than renderer startup.
        if (renderingConfig == null || activeCamera == null) {
            return;
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isLocalReflections()) {
            if (sceneReflectiveRefractive != null) {
                GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
                sceneReflectiveRefractive.bindNormalsTexture();
                program.setInt("texSceneReflectiveRefractiveNormals", texId++, true);
            }

            program.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
            program.setMatrix4("projMatrix", activeCamera.getProjectionMatrix(), true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isSsao()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.bindFboColorTexture(BlurredAmbientOcclusionNode.SSAO_BLURRED_FBO);
            program.setInt("texSsao", texId++, true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.bindFboColorTexture(OutlineNode.OUTLINE_FBO);
            program.setInt("texEdges", texId++, true);

            program.setFloat("outlineDepthThreshold", outlineDepthThreshold, true);
            program.setFloat("outlineThickness", outlineThickness, true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isVolumetricFog()) {
            program.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            //TODO: Other parameters and volumetric fog test case is needed
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isInscattering()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            displayResolutionDependentFBOs.bindFboColorTexture(HazeNode.FINAL_HAZE_FBO);
            program.setInt("texSceneSkyBand", texId++, true);

            Vector4f skyInscatteringSettingsFrag = new Vector4f();
            skyInscatteringSettingsFrag.y = skyInscatteringStrength;
            skyInscatteringSettingsFrag.z = skyInscatteringLength;
            skyInscatteringSettingsFrag.w = skyInscatteringThreshold;
            program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag, true);
        }
    }
}
