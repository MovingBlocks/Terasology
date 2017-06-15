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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.dag.nodes.BloomBlurNode.ONE_8TH_SCALE_FBO;
import static org.terasology.rendering.dag.nodes.LightShaftsNode.LIGHT_SHAFTS_FBO;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * An instance of this node adds chromatic aberration (currently non-functional), light shafts,
 * 1/8th resolution bloom and vignette onto the rendering achieved so far, stored in the gbuffer.
 * Stores the result into the InitialPostProcessingNode.INITIAL_POST_FBO, to be used at a later stage.
 */
public class InitialPostProcessingNode extends AbstractNode implements PropertyChangeListener {
    static final ResourceUrn INITIAL_POST_FBO = new ResourceUrn("engine:fbo.initialPost");
    private static final ResourceUrn INITIAL_POST_MATERIAL = new ResourceUrn("engine:prog.initialPost");

    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;

    private Material initialPostMaterial;

    private boolean isBloom;
    private boolean isLightShafts;

    private StateChange setTexBloom;
    private StateChange setTexLightShafts;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetX = 0;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetY = 0;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float bloomFactor = 0.5f;

    public InitialPostProcessingNode(Context context) {
        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        // TODO: see if we could write this straight into a GBUFFER
        requiresFBO(new FBOConfig(INITIAL_POST_FBO, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(INITIAL_POST_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(INITIAL_POST_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(INITIAL_POST_MATERIAL));

        initialPostMaterial = getMaterial(INITIAL_POST_MATERIAL);

        renderingConfig = context.get(Config.class).getRendering();
        isBloom = renderingConfig.isBloom();
        renderingConfig.subscribe(RenderingConfig.BLOOM, this);
        isLightShafts = renderingConfig.isLightShafts();
        renderingConfig.subscribe(RenderingConfig.LIGHT_SHAFTS, this);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, READONLY_GBUFFER, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texScene"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:vignette", INITIAL_POST_MATERIAL, "texVignette"));
        setTexBloom = new SetInputTextureFromFbo(textureSlot++, ONE_8TH_SCALE_FBO, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texBloom");
        setTexLightShafts = new SetInputTextureFromFbo(textureSlot, LIGHT_SHAFTS_FBO, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL, "texLightShafts");

        if (isBloom) {
            addDesiredStateChange(setTexBloom);
        }
        if (isLightShafts) {
            addDesiredStateChange(setTexLightShafts);
        }
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.INITIAL_POST_FBO.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/initialPostProcessing");

        // Shader Parameters

        initialPostMaterial.setFloat3("inLiquidTint", worldProvider.getBlock(activeCamera.getPosition()).getTint(), true);

        if (isBloom) {
            initialPostMaterial.setFloat("bloomFactor", bloomFactor, true);
        }

        initialPostMaterial.setFloat2("aberrationOffset", aberrationOffsetX, aberrationOffsetY, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getOldValue() != event.getNewValue()) {
            if (event.getPropertyName().equals(RenderingConfig.BLOOM)) {
                isBloom = renderingConfig.isBloom();
                if (isBloom) {
                    addDesiredStateChange(setTexBloom);
                } else {
                    removeDesiredStateChange(setTexBloom);
                }
            } else {
                isLightShafts = renderingConfig.isLightShafts();
                if (isLightShafts) {
                    addDesiredStateChange(setTexLightShafts);
                } else {
                    removeDesiredStateChange(setTexLightShafts);
                }
            }

            worldRenderer.requestTaskListRefresh();
        }
    }
}
