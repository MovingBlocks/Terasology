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
import org.terasology.context.Context;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * An instance of this node adds chromatic aberration (currently non-functional), light shafts,
 * 1/8th resolution bloom and vignette onto the rendering achieved so far, stored in the gbuffer.
 * Stores the result into the InitialPostProcessingNode.INITIAL_POST_FBO, to be used at a later stage.
 */
public class InitialPostProcessingNode extends AbstractNode {
    public static final ResourceUrn INITIAL_POST_FBO = new ResourceUrn("engine:fbo.initialPost");
    public static final ResourceUrn INITIAL_POST_MATERIAL = new ResourceUrn("engine:prog.initialPost");

    public InitialPostProcessingNode(Context context) {
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        // TODO: see if we could write this straight into a GBUFFER - notice this FBO is used in ShaderParametersHdr
        requiresFBO(new FBOConfig(INITIAL_POST_FBO, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(INITIAL_POST_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(INITIAL_POST_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(INITIAL_POST_MATERIAL));

        // TODO: move content of ShaderParametersInitialPost to this class
    }

    /**
     * Renders a quad, in turn filling the InitialPostProcessingNode.INITIAL_POST_FBO.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/initialPostProcessing");

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
