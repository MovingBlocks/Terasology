/*
 * Copyright 2019 MovingBlocks
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
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.stateChanges.*;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.FINAL_BUFFER;
import static org.terasology.rendering.dag.nodes.FinalPostProcessingNode.POST_FBO_URI;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.ScalingFactors.ONE_8TH_SCALE;

/**
 * An instance of this node adds vignette onto the rendering achieved so far, stored in the gbuffer.
 * It should provide ability to use various vignette textures and tinting.
 * 1 Channeled transparency texture is used atm.
 * Stores the result into the VignetteNode.VIGNETTE_FBO_URI, to be used at a later stage.
 * Requirements: https://github.com/MovingBlocks/Terasology/issues/3040
 */
public class VignetteNode extends AbstractNode implements PropertyChangeListener {
    public static final SimpleUri VIGNETTE_FBO_URI = new SimpleUri("engine:fbo.vignette");
    private static final ResourceUrn VIGNETTE_MATERIAL_URN = new ResourceUrn("engine:prog.vignette");

    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;
    private WorldRenderer worldRenderer;
    private SubmersibleCamera activeCamera;

    private Material vignetteMaterial;

    private boolean vignetteIsEnabled;
    // TODO: figure where from to set this variable
    private Vector3f tint = new Vector3f(.0f,.0f,.0f);
//    private Vector3f tint = new Vector3f(0.937f,0.126f,0.016f);

    private StateChange setVignetteInputTexture;

    public VignetteNode(String nodeUri, Context context) {
        super(nodeUri, context);

        worldProvider = context.get(WorldProvider.class);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        // TODO: see if we could write this straight into a GBUFFER
       // FBO vignetteFbo = requiresFBO(new FBOConfig(VIGNETTE_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        FBO finalBuffer = displayResolutionDependentFBOs.get(FINAL_BUFFER);
        addDesiredStateChange(new BindFbo(finalBuffer));
        addDesiredStateChange(new SetViewportToSizeOf(finalBuffer));

        addDesiredStateChange(new EnableMaterial(VIGNETTE_MATERIAL_URN));

        vignetteMaterial = getMaterial(VIGNETTE_MATERIAL_URN);

        renderingConfig = context.get(Config.class).getRendering();
        vignetteIsEnabled = renderingConfig.isVignette();
        renderingConfig.subscribe(RenderingConfig.VIGNETTE, this);

        int textureSlot = 0;
        //addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo(), ColorTexture, displayResolutionDependentFBOs, VIGNETTE_MATERIAL_URN, "texScene"));
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot++, POST_FBO_URI , ColorTexture, displayResolutionDependentFBOs, VIGNETTE_MATERIAL_URN, "texScene"));

        setVignetteInputTexture = new SetInputTexture2D(textureSlot++, "engine:vignette", VIGNETTE_MATERIAL_URN, "texVignette");

        if(vignetteIsEnabled) {
            addDesiredStateChange(setVignetteInputTexture);
        }
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.VIGNETTE_FBO_URI.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Common Shader Parameters

        vignetteMaterial.setFloat("swimming", activeCamera.isUnderWater() ? 1.0f : 0.0f, true);

        // Shader Parameters

        vignetteMaterial.setFloat3("inLiquidTint", worldProvider.getBlock(activeCamera.getPosition()).getTint(), true);
        vignetteMaterial.setFloat3("tint", tint);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.VIGNETTE:
                vignetteIsEnabled = renderingConfig.isVignette();
                if (vignetteIsEnabled) {
                    addDesiredStateChange(setVignetteInputTexture);
                } else {
                    removeDesiredStateChange(setVignetteInputTexture);
                }
                break;

            // default: no other cases are possible - see subscribe operations in initialize().
        }

        worldRenderer.requestTaskListRefresh();
    }
}
