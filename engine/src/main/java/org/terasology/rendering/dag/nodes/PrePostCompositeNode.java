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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SwapGBuffers;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.SwappableFBO;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.nodes.BlurredAmbientOcclusionNode.SSAO_BLURRED_FBO_URI;
import static org.terasology.rendering.dag.nodes.HazeNode.FINAL_HAZE_FBO_URI;
import static org.terasology.rendering.dag.nodes.OutlineNode.OUTLINE_FBO_URI;
import static org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode.REFRACTIVE_REFLECTIVE_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.NormalsTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * An instance of this class takes advantage of the content of a number of previously filled buffers
 * to add screen-space ambient occlusion (SSAO), outlines, reflections [1], atmospheric haze and volumetric fog
 *
 * As this node does not quite use 3D geometry and only relies on 2D sources and a 2D output buffer, it
 * could be argued that, despite its name, it represents the first step of the PostProcessing portion
 * of the rendering engine. This line of thinking draws a parallel from the film industry where
 * Post-Processing (or Post-Production) is everything that happens -after- the footage for the film
 * has been shot on stage or on location.
 *
 * [1] And refractions? To be verified.
 */
public class PrePostCompositeNode extends AbstractNode implements PropertyChangeListener {
    private static final ResourceUrn PRE_POST_MATERIAL_URN = new ResourceUrn("engine:prog.prePostComposite");

    private RenderingConfig renderingConfig;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;

    private Material prePostMaterial;

    private boolean localReflectionsAreEnabled;
    private boolean ssaoIsEnabled;
    private boolean outlineIsEnabled;
    private boolean hazeIsEnabled;
    private boolean volumetricFogIsEnabled;

    private StateChange setReflectiveRefractiveNormalsInputTexture;
    private StateChange setSsaoInputTexture;
    private StateChange setEdgesInputTexture;
    private StateChange setHazeInputTexture;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.001f, max = 0.005f)
    private float outlineDepthThreshold = 0.001f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float outlineThickness = 0.65f;

    // TODO : Consider a more descriptive name for this variable.
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float hazeLength = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float hazeStrength = 0.25f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float hazeThreshold = 0.8f;
    
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float volumetricFogGlobalDensity = 0.005f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = -0.1f, max = 0.1f)
    private float volumetricFogHeightFalloff = -0.01f;

    public PrePostCompositeNode(String nodeUri, Context context) {
        super(nodeUri, context);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        SwappableFBO gBufferPair = displayResolutionDependentFBOs.getGBufferPair();

        addDesiredStateChange(new EnableMaterial(PRE_POST_MATERIAL_URN));
        addDesiredStateChange(new BindFbo(gBufferPair.getStaleFbo()));

        prePostMaterial = getMaterial(PRE_POST_MATERIAL_URN);

        renderingConfig = context.get(Config.class).getRendering();
        localReflectionsAreEnabled = renderingConfig.isLocalReflections();
        renderingConfig.subscribe(RenderingConfig.LOCAL_REFLECTIONS, this);
        ssaoIsEnabled = renderingConfig.isSsao();
        renderingConfig.subscribe(RenderingConfig.SSAO, this);
        outlineIsEnabled = renderingConfig.isOutline();
        renderingConfig.subscribe(RenderingConfig.OUTLINE, this);
        hazeIsEnabled = renderingConfig.isInscattering();
        renderingConfig.subscribe(RenderingConfig.INSCATTERING, this);
        volumetricFogIsEnabled = renderingConfig.isVolumetricFog();
        renderingConfig.subscribe(RenderingConfig.VOLUMETRIC_FOG, this);

        FBO lastUpdatedGBuffer = gBufferPair.getLastUpdatedFbo();
        FBO refractiveReflectiveFbo = displayResolutionDependentFBOs.get(REFRACTIVE_REFLECTIVE_FBO_URI);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, DepthStencilTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, lastUpdatedGBuffer, LightAccumulationTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneOpaqueLightBuffer"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, refractiveReflectiveFbo, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneReflectiveRefractive"));
        setReflectiveRefractiveNormalsInputTexture = new SetInputTextureFromFbo(textureSlot++, refractiveReflectiveFbo, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneReflectiveRefractiveNormals");
        setSsaoInputTexture = new SetInputTextureFromFbo(textureSlot++, SSAO_BLURRED_FBO_URI, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSsao");
        setEdgesInputTexture = new SetInputTextureFromFbo(textureSlot++, OUTLINE_FBO_URI, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texEdges");
        setHazeInputTexture = new SetInputTextureFromFbo(textureSlot, FINAL_HAZE_FBO_URI, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL_URN, "texSceneSkyBand");

        if (localReflectionsAreEnabled) {
            addDesiredStateChange(setReflectiveRefractiveNormalsInputTexture);
        }
        if (ssaoIsEnabled) {
            addDesiredStateChange(setSsaoInputTexture);
        }
        if (outlineIsEnabled) {
            addDesiredStateChange(setEdgesInputTexture);
        }
        if (hazeIsEnabled) {
            addDesiredStateChange(setHazeInputTexture);
        }

        addDesiredStateChange(new SwapGBuffers(gBufferPair));
    }

    /**
     * Called every frame, the shader program used by this method only composites per-pixel information from a number
     * of buffers and renders it into a full-screen quad, which is the only piece of geometry processed.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Shader Parameters

        prePostMaterial.setFloat("viewingDistance", renderingConfig.getViewDistance().getChunkDistance().x * 8.0f, true);
        prePostMaterial.setFloat3("cameraParameters", activeCamera.getzNear(), activeCamera.getzFar(), 0.0f, true);

        if (localReflectionsAreEnabled) {
            prePostMaterial.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
            prePostMaterial.setMatrix4("projMatrix", activeCamera.getProjectionMatrix(), true);
        }

        if (outlineIsEnabled) {
            prePostMaterial.setFloat("outlineDepthThreshold", outlineDepthThreshold, true);
            prePostMaterial.setFloat("outlineThickness", outlineThickness, true);
        }

        if (volumetricFogIsEnabled) {
            prePostMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            prePostMaterial.setFloat3("volumetricFogSettings", 1f, volumetricFogGlobalDensity, volumetricFogHeightFalloff, true);
        }

        if (hazeIsEnabled) {
            prePostMaterial.setFloat4("skyInscatteringSettingsFrag", 0, hazeStrength, hazeLength, hazeThreshold, true);
        }

        // TODO: We never set the "fogWorldPosition" uniform in prePostComposite_frag.glsl . Either use it, or remove it.

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.LOCAL_REFLECTIONS:
                localReflectionsAreEnabled = renderingConfig.isLocalReflections();
                if (localReflectionsAreEnabled) {
                    addDesiredStateChange(setReflectiveRefractiveNormalsInputTexture);
                } else {
                    removeDesiredStateChange(setReflectiveRefractiveNormalsInputTexture);
                }
                break;

            case RenderingConfig.SSAO:
                ssaoIsEnabled = renderingConfig.isSsao();
                if (ssaoIsEnabled) {
                    addDesiredStateChange(setSsaoInputTexture);
                } else {
                    removeDesiredStateChange(setSsaoInputTexture);
                }
                break;

            case RenderingConfig.OUTLINE:
                outlineIsEnabled = renderingConfig.isOutline();
                if (outlineIsEnabled) {
                    addDesiredStateChange(setEdgesInputTexture);
                } else {
                    removeDesiredStateChange(setEdgesInputTexture);
                }
                break;

            case RenderingConfig.INSCATTERING:
                hazeIsEnabled = renderingConfig.isInscattering();
                if (hazeIsEnabled) {
                    addDesiredStateChange(setHazeInputTexture);
                } else {
                    removeDesiredStateChange(setHazeInputTexture);
                }
                break;

            case RenderingConfig.VOLUMETRIC_FOG:
                volumetricFogIsEnabled = renderingConfig.isVolumetricFog();
                break;

            // default: no other cases are possible - see subscribe operations in initialize().
        }

        worldRenderer.requestTaskListRefresh();
    }
}
