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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.math.geom.Vector4f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import static org.terasology.rendering.dag.nodes.BlurredAmbientOcclusionNode.SSAO_BLURRED_FBO;
import static org.terasology.rendering.dag.nodes.HazeNode.FINAL_HAZE_FBO;
import static org.terasology.rendering.dag.nodes.OutlineNode.OUTLINE_FBO;
import static org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode.REFRACTIVE_REFLECTIVE_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.WRITEONLY_GBUFFER;

/**
 * An instance of this class takes advantage of the content of a number of previously filled buffers
 * to add screen-space ambient occlusion (SSAO), outlines, reflections [1], atmospheric haze and volumetric fog [2]
 *
 * As this node does not quite use 3D geometry and only relies on 2D sources and a 2D output buffer, it
 * could be argued that, despite its name, it represents the first step of the PostProcessing portion
 * of the rendering engine. This line of thinking draws a parallel from the film industry where
 * Post-Processing (or Post-Production) is everything that happens -after- the footage for the film
 * has been shot on stage or on location.
 *
 * [1] And refractions? To be verified.
 * [2] Currently not working: the code is there but it is never enabled.
 */
public class PrePostCompositeNode extends AbstractNode {
    private static final ResourceUrn PRE_POST_MATERIAL = new ResourceUrn("engine:prog.prePostComposite");

    private RenderingConfig renderingConfig;
    private SubmersibleCamera activeCamera;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material prePostMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    private Vector4f skyInscatteringSettingsFrag = new Vector4f();

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.001f, max = 0.005f)
    private float outlineDepthThreshold = 0.001f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float outlineThickness = 0.65f;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringLength = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringStrength = 0.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float skyInscatteringThreshold = 0.8f;

    public PrePostCompositeNode(Context context) {
        renderingConfig = context.get(Config.class).getRendering();

        activeCamera = context.get(WorldRenderer.class).getActiveCamera();

        displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        addDesiredStateChange(new EnableMaterial(PRE_POST_MATERIAL));
        addDesiredStateChange(new BindFBO(WRITEONLY_GBUFFER, displayResolutionDependentFBOs));

        prePostMaterial = getMaterial(PRE_POST_MATERIAL);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, DepthStencilTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, READONLY_GBUFFER, LightAccumulationTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueLightBuffer"));
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, REFRACTIVE_REFLECTIVE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneReflectiveRefractive"));
        // TODO: monitor the property subscribing to it
        if (renderingConfig.isLocalReflections()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, REFRACTIVE_REFLECTIVE_FBO, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneReflectiveRefractiveNormals"));
        }
        // TODO: monitor the property subscribing to it
        if (renderingConfig.isSsao()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, SSAO_BLURRED_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSsao"));
        }
        // TODO: monitor the property subscribing to it
        if (renderingConfig.isOutline()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot++, OUTLINE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texEdges"));
        }
        // TODO: monitor the property subscribing to it
        if (renderingConfig.isInscattering()) {
            addDesiredStateChange(new SetInputTextureFromFBO(textureSlot, FINAL_HAZE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneSkyBand"));
        }
    }

    /**
     * Called every frame, the shader program used by this method only composites per-pixel information from a number
     * of buffers and renders it into a full-screen quad, which is the only piece of geometry processed.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/prePostComposite");

        // Shader Parameters

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isLocalReflections()) {
            prePostMaterial.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
            prePostMaterial.setMatrix4("projMatrix", activeCamera.getProjectionMatrix(), true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isOutline()) {
            prePostMaterial.setFloat("outlineDepthThreshold", outlineDepthThreshold, true);
            prePostMaterial.setFloat("outlineThickness", outlineThickness, true);
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isVolumetricFog()) {
            prePostMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            //TODO: Other parameters and volumetric fog test case is needed
        }

        // TODO: monitor the property subscribing to it
        if (renderingConfig.isInscattering()) {
            skyInscatteringSettingsFrag.set(0, skyInscatteringStrength, skyInscatteringLength, skyInscatteringThreshold);
            prePostMaterial.setFloat4("skyInscatteringSettingsFrag", skyInscatteringSettingsFrag, true);
        }

        // TODO: We never set the "fogWorldPosition" uniform in prePostComposite_frag.glsl . Either use it, or remove it.

        // Actual Node Processing

        renderFullscreenQuad();

        // TODO: review - the following line is necessary, but at this stage it's unclear why.
        displayResolutionDependentFBOs.swapReadWriteBuffers();

        PerformanceMonitor.endActivity();
    }
}
