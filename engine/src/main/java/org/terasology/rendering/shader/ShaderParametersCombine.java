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

import org.lwjgl.opengl.GL13;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.geom.Vector4f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.nodes.AmbientOcclusionPassesNode;
import org.terasology.rendering.dag.nodes.ChunksRefractiveReflectiveNode;
import org.terasology.rendering.dag.nodes.OutlineNode;
import org.terasology.rendering.dag.nodes.SkyBandsNode;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.DefaultDynamicFBOs;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.fbms.DynamicFBM;
import org.terasology.rendering.world.WorldRenderer;

/**
 * Shader parameters for the Combine shader program.
 *
 */
public class ShaderParametersCombine extends ShaderParametersBase {
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
        DynamicFBM dynamicFBM = CoreRegistry.get(DynamicFBM.class);
        FBO sceneOpaque = dynamicFBM.getFBO(DefaultDynamicFBOs.ReadOnlyGBuffer.getResourceUrn());

        // TODO: move texture bindings to the appropriate nodes
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

        FBO sceneReflectiveRefractive = dynamicFBM.getFBO(ChunksRefractiveReflectiveNode.REFRACTIVE_REFLECTIVE_URN);

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
            dynamicFBM.bindFboColorTexture(AmbientOcclusionPassesNode.SSAO_BLURRED_URN);
            program.setInt("texSsao", texId++, true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isOutline()) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            dynamicFBM.bindFboColorTexture(OutlineNode.OUTLINE_URN);
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
            dynamicFBM.bindFboColorTexture(SkyBandsNode.SKY_BAND_1_URN);
            program.setInt("texSceneSkyBand", texId++, true);

            Vector4f skyInscatteringSettingsFrag = new Vector4f();
            skyInscatteringSettingsFrag.y = skyInscatteringStrength;
            skyInscatteringSettingsFrag.z = skyInscatteringLength;
            skyInscatteringSettingsFrag.w = skyInscatteringThreshold;
            program.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag, true);
        }
    }
}
