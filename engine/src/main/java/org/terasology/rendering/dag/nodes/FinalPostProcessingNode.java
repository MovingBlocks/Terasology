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
package org.terasology.rendering.dag.nodes;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.FINAL;

import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * An instance of this class adds depth of field blur, motion blur and film grain to the rendering
 * of the scene obtained so far. Furthermore, depending if a screenshot has been requested,
 * it instructs the ScreenGrabber to save it to a file.
 *
 * If RederingDebugConfig.isEnabled() returns true, this node is instead responsible for displaying
 * the content of a number of technical buffers rather than the final, post-processed rendering
 * of the scene.
 */
public class FinalPostProcessingNode extends AbstractNode implements PropertyChangeListener {
    private static final ResourceUrn POST_MATERIAL = new ResourceUrn("engine:prog.post");
    private static final ResourceUrn DEBUG_MATERIAL = new ResourceUrn("engine:prog.debug");

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private ScreenGrabber screenGrabber;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private RenderingDebugConfig renderingDebugConfig;
    private EnableMaterial enablePostMaterial;
    private EnableMaterial enableDebugMaterial;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        renderingDebugConfig = config.getRendering().getDebug();
        renderingDebugConfig.subscribe(RenderingDebugConfig.ENABLED, this);

        enablePostMaterial = new EnableMaterial(POST_MATERIAL.toString());
        enableDebugMaterial = new EnableMaterial(DEBUG_MATERIAL.toString());

        if (!renderingDebugConfig.isEnabled()) {
            addDesiredStateChange(enablePostMaterial);
        } else {
            addDesiredStateChange(enableDebugMaterial);
        }

        addDesiredStateChange(new BindFBO(FINAL.getName(), displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(FINAL.getName(), displayResolutionDependentFBOs));
    }

    /**
     * Execute the final post processing on the rendering of the scene obtained so far.
     *
     * It uses the GBUFFER as input and the FINAL FBO to store its output, rendering
     * everything to a quad.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/finalPostProcessing");

        renderFullscreenQuad();

        if (!screenGrabber.isNotTakingScreenshot()) {
            screenGrabber.saveScreenshot();
        }

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // we assume here that this property change event is fired only if there has been a change
        if (!renderingDebugConfig.isEnabled()) {
            removeDesiredStateChange(enableDebugMaterial);
            addDesiredStateChange(enablePostMaterial);
        } else {
            removeDesiredStateChange(enablePostMaterial);
            addDesiredStateChange(enableDebugMaterial);
        }
        worldRenderer.requestTaskListRefresh();
    }
}
