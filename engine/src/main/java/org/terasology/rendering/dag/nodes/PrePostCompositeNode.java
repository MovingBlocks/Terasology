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
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.nodes.BlurredAmbientOcclusionNode.SSAO_BLURRED_FBO;
import static org.terasology.rendering.dag.nodes.HazeNode.FINAL_HAZE_FBO;
import static org.terasology.rendering.dag.nodes.OutlineNode.OUTLINE_FBO;
import static org.terasology.rendering.dag.nodes.RefractiveReflectiveBlocksNode.REFRACTIVE_REFLECTIVE_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.DepthStencilTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.LightAccumulationTexture;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.NormalsTexture;
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
public class PrePostCompositeNode extends AbstractNode implements PropertyChangeListener {
    private static final ResourceUrn PRE_POST_MATERIAL = new ResourceUrn("engine:prog.prePostComposite");

    private RenderingConfig renderingConfig;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material prePostMaterial;

    private boolean isLocalReflections;
    private boolean isSsao;
    private boolean isOutline;
    private boolean isInscattering;
    private boolean isVolumetricFog;

    private StateChange setTexSceneReflectiveRefractiveNormals;
    private StateChange setTexSsao;
    private StateChange setTexEdges;
    private StateChange setTexSceneSkyBand;

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
        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();

        displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        addDesiredStateChange(new EnableMaterial(PRE_POST_MATERIAL));
        addDesiredStateChange(new BindFbo(WRITEONLY_GBUFFER, displayResolutionDependentFBOs));

        prePostMaterial = getMaterial(PRE_POST_MATERIAL);

        renderingConfig = context.get(Config.class).getRendering();
        isLocalReflections = renderingConfig.isLocalReflections();
        renderingConfig.subscribe(RenderingConfig.LOCAL_REFLECTIONS, this);
        isSsao = renderingConfig.isSsao();
        renderingConfig.subscribe(RenderingConfig.SSAO, this);
        isOutline = renderingConfig.isOutline();
        renderingConfig.subscribe(RenderingConfig.OUTLINE, this);
        isInscattering = renderingConfig.isInscattering();
        renderingConfig.subscribe(RenderingConfig.INSCATTERING, this);
        isVolumetricFog = renderingConfig.isVolumetricFog();
        renderingConfig.subscribe(RenderingConfig.VOLUMETRIC_FOG, this);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaque"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, DepthStencilTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueDepth"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueNormals"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, LightAccumulationTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneOpaqueLightBuffer"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, REFRACTIVE_REFLECTIVE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneReflectiveRefractive"));
        setTexSceneReflectiveRefractiveNormals = new SetInputTextureFromFbo(textureSlot++, REFRACTIVE_REFLECTIVE_FBO, NormalsTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneReflectiveRefractiveNormals");
        setTexSsao = new SetInputTextureFromFbo(textureSlot++, SSAO_BLURRED_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSsao");
        setTexEdges = new SetInputTextureFromFbo(textureSlot++, OUTLINE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texEdges");
        setTexSceneSkyBand = new SetInputTextureFromFbo(textureSlot, FINAL_HAZE_FBO, ColorTexture, displayResolutionDependentFBOs, PRE_POST_MATERIAL, "texSceneSkyBand");

        if (isLocalReflections) {
            addDesiredStateChange(setTexSceneReflectiveRefractiveNormals);
        }
        if (isSsao) {
            addDesiredStateChange(setTexSsao);
        }
        if (isOutline) {
            addDesiredStateChange(setTexEdges);
        }
        if (isInscattering) {
            addDesiredStateChange(setTexSceneSkyBand);
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

        if (isLocalReflections) {
            prePostMaterial.setMatrix4("invProjMatrix", activeCamera.getInverseProjectionMatrix(), true);
            prePostMaterial.setMatrix4("projMatrix", activeCamera.getProjectionMatrix(), true);
        }

        if (isOutline) {
            prePostMaterial.setFloat("outlineDepthThreshold", outlineDepthThreshold, true);
            prePostMaterial.setFloat("outlineThickness", outlineThickness, true);
        }

        if (isVolumetricFog) {
            prePostMaterial.setMatrix4("invViewProjMatrix", activeCamera.getInverseViewProjectionMatrix(), true);
            //TODO: Other parameters and volumetric fog test case is needed
        }

        if (isInscattering) {
            prePostMaterial.setFloat4("skyInscatteringSettingsFrag", 0, skyInscatteringStrength, skyInscatteringLength, skyInscatteringThreshold, true);
        }

        // TODO: We never set the "fogWorldPosition" uniform in prePostComposite_frag.glsl . Either use it, or remove it.

        // Actual Node Processing

        renderFullscreenQuad();

        // TODO: review - the following line is necessary, but at this stage it's unclear why.
        displayResolutionDependentFBOs.swapReadWriteBuffers();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getOldValue() != event.getNewValue()) {
            if (event.getPropertyName().equals(RenderingConfig.LOCAL_REFLECTIONS)) {
                isLocalReflections = renderingConfig.isLocalReflections();
                if (isLocalReflections) {
                    addDesiredStateChange(setTexSceneReflectiveRefractiveNormals);
                } else {
                    removeDesiredStateChange(setTexSceneReflectiveRefractiveNormals);
                }
            } else if (event.getPropertyName().equals(RenderingConfig.SSAO)) {
                isSsao = renderingConfig.isSsao();
                if (isSsao) {
                    addDesiredStateChange(setTexSsao);
                } else {
                    removeDesiredStateChange(setTexSsao);
                }
            } else if (event.getPropertyName().equals(RenderingConfig.OUTLINE)) {
                isOutline = renderingConfig.isOutline();
                if (isOutline) {
                    addDesiredStateChange(setTexEdges);
                } else {
                    removeDesiredStateChange(setTexEdges);
                }
            } else if (event.getPropertyName().equals(RenderingConfig.INSCATTERING)) {
                isInscattering = renderingConfig.isInscattering();
                if (isInscattering) {
                    addDesiredStateChange(setTexSceneSkyBand);
                } else {
                    removeDesiredStateChange(setTexSceneSkyBand);
                }
            } else if (event.getPropertyName().equals(RenderingConfig.VOLUMETRIC_FOG)) {
                isVolumetricFog = renderingConfig.isVolumetricFog();
            }

            worldRenderer.requestTaskListRefresh();
        }
    }
}
