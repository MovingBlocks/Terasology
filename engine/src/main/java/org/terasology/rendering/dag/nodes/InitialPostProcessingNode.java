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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add diagram of this node
 */
public class InitialPostProcessingNode extends AbstractNode {
    public static final ResourceUrn SCENE_PRE_POST = new ResourceUrn("engine:scenePrePost");

    @In
    private Config config;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;


    @Override
    public void initialise() {
        requiresFBO(new FBOConfig(SCENE_PRE_POST, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);

        addDesiredStateChange(new EnableMaterial("engine:prog.prePost")); // TODO: rename shader to scenePrePost
        // TODO: verify what the inputs are
        addDesiredStateChange(new BindFBO(SCENE_PRE_POST, displayResolutionDependentFBOs)); // TODO: see if we could write this straight into sceneOpaque
        addDesiredStateChange(new SetViewportToSizeOf(SCENE_PRE_POST, displayResolutionDependentFBOs));
    }

    /**
     * Adds chromatic aberration, light shafts, 1/8th resolution bloom, vignette onto the rendering achieved so far.
     * Stores the result into its own buffer to be used at a later stage.
     */
    @Override
    public void process() {
        // Initial Post-Processing: chromatic aberration, light shafts, 1/8th resolution bloom, vignette
        PerformanceMonitor.startActivity("rendering/initialPostProcessing");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
