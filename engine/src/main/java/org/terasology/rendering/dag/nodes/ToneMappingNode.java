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
 * The exposure calculated earlier in the rendering process is used by an instance
 * of this node to remap the colors of the image rendered so far, brightening otherwise
 * undetailed dark areas or dimming otherwise burnt bright areas, depending on the circumstances.
 *
 * For more details on the specific algorithm used see shader resource toneMapping_frag.glsl.
 *
 * This node stores its output in the this.TONE_MAPPED_FBO.
 */
public class ToneMappingNode extends AbstractNode {
    public static final ResourceUrn TONE_MAPPING_FBO = new ResourceUrn("engine:fbo.toneMapping");
    public static final ResourceUrn TONE_MAPPING_MATERIAL = new ResourceUrn("engine:prog.toneMapping");

    public ToneMappingNode(Context context) {
        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(TONE_MAPPING_FBO, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(TONE_MAPPING_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(TONE_MAPPING_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(TONE_MAPPING_MATERIAL));

        // TODO: bind input textures from ShaderParametersCombine class
    }

    /**
     * Renders a full screen quad with the opengl state defined by the initialise() method,
     * using the GBUFFER as input and filling the TONE_MAPPED_FBO with the output of
     * the shader operations. As such, this method performs purely 2D operations.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/toneMapping");

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
