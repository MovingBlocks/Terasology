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
import org.terasology.engine.SimpleUri;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTexture2D;
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

import static org.terasology.rendering.dag.nodes.LightShaftsNode.LIGHT_SHAFTS_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_8TH_SCALE;

/**
 * An instance of this node adds chromatic aberration (currently non-functional), light shafts,
 * 1/8th resolution bloom and vignette onto the rendering achieved so far, stored in the gbuffer.
 * Stores the result into the InitialPostProcessingNode.INITIAL_POST_FBO_URI, to be used at a later stage.
 */
public class InitialPostProcessingNode extends AbstractNode implements PropertyChangeListener {
    static final SimpleUri INITIAL_POST_FBO_URI = new SimpleUri("engine:fbo.initialPost");
    private static final ResourceUrn INITIAL_POST_MATERIAL_URN = new ResourceUrn("engine:prog.initialPost");

    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;

    private Material initialPostMaterial;

    private boolean bloomIsEnabled;
    private boolean lightShaftsAreEnabled;

    private StateChange setBloomInputTexture;
    private StateChange setLightShaftsInputTexture;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetX;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.1f)
    private float aberrationOffsetY;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 1.0f)
    private float bloomFactor = 0.5f;

    public InitialPostProcessingNode(String nodeUri, Context context) {
        super(nodeUri, context);

        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        // TODO: see if we could write this straight into a GBUFFER
        FBO initialPostFbo = requiresFBO(new FBOConfig(INITIAL_POST_FBO_URI, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(initialPostFbo));
        addDesiredStateChange(new SetViewportToSizeOf(initialPostFbo));

        addDesiredStateChange(new EnableMaterial(INITIAL_POST_MATERIAL_URN));

        initialPostMaterial = getMaterial(INITIAL_POST_MATERIAL_URN);

        renderingConfig = context.get(Config.class).getRendering();
        bloomIsEnabled = renderingConfig.isBloom();
        renderingConfig.subscribe(RenderingConfig.BLOOM, this);
        lightShaftsAreEnabled = renderingConfig.isLightShafts();
        renderingConfig.subscribe(RenderingConfig.LIGHT_SHAFTS, this);

        // TODO: Temporary hack for now.
        FBOConfig one8thScaleBloomConfig = new FBOConfig(BloomBlurNode.ONE_8TH_SCALE_FBO_URI, ONE_8TH_SCALE, FBO.Type.DEFAULT);
        FBO one8thBloomFbo = requiresFBO(one8thScaleBloomConfig, displayResolutionDependentFBOs);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo(), ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL_URN, "texScene"));
        addDesiredStateChange(new SetInputTexture2D(textureSlot++, "engine:vignette", INITIAL_POST_MATERIAL_URN, "texVignette"));
        setBloomInputTexture = new SetInputTextureFromFbo(textureSlot++, one8thBloomFbo, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL_URN, "texBloom");
        setLightShaftsInputTexture = new SetInputTextureFromFbo(textureSlot, LIGHT_SHAFTS_FBO_URI, ColorTexture, displayResolutionDependentFBOs, INITIAL_POST_MATERIAL_URN, "texLightShafts");

        if (bloomIsEnabled) {
            addDesiredStateChange(setBloomInputTexture);
        }
        if (lightShaftsAreEnabled) {
            addDesiredStateChange(setLightShaftsInputTexture);
        }
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.INITIAL_POST_FBO_URI.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Common Shader Parameters

        initialPostMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);

        // Shader Parameters

        initialPostMaterial.setFloat3("inLiquidTint", worldProvider.getBlock(activeCamera.getPosition()).getTint(), true);

        if (bloomIsEnabled) {
            initialPostMaterial.setFloat("bloomFactor", bloomFactor, true);
        }

        initialPostMaterial.setFloat2("aberrationOffset", aberrationOffsetX, aberrationOffsetY, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.BLOOM:
                bloomIsEnabled = renderingConfig.isBloom();
                if (bloomIsEnabled) {
                    addDesiredStateChange(setBloomInputTexture);
                } else {
                    removeDesiredStateChange(setBloomInputTexture);
                }
                break;

            case RenderingConfig.LIGHT_SHAFTS:
                lightShaftsAreEnabled = renderingConfig.isLightShafts();
                if (lightShaftsAreEnabled) {
                    addDesiredStateChange(setLightShaftsInputTexture);
                } else {
                    removeDesiredStateChange(setLightShaftsInputTexture);
                }
                break;

            // default: no other cases are possible - see subscribe operations in initialize().
        }

        worldRenderer.requestTaskListRefresh();
    }
}
