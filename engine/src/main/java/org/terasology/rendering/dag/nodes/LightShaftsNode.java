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
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.HALF_SCALE;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * An instance of this class takes advantage of the color and depth buffers attached to the read-only gbuffer
 * and produces light shafts from the main light (sun/moon). It is therefore a relatively inexpensive
 * 2D effect rendered on a full screen quad - no 3D geometry involved.
 *
 * Trivia: the more correct term would be Crepuscular Rays [1], an atmospheric effect. One day we might
 * be able to provide indoor light shafts through other means and it might be appropriate to rename
 * this node accordingly.
 *
 * [1] https://en.wikipedia.org/wiki/Crepuscular_rays
 */
public class LightShaftsNode extends ConditionDependentNode {
    public static final ResourceUrn LIGHT_SHAFTS_FBO = new ResourceUrn("engine:fbo.lightShafts");
    public static final ResourceUrn LIGHT_SHAFTS_MATERIAL = new ResourceUrn("engine:prog.lightShafts");

    public LightShaftsNode(Context context) {
        super(context);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.LIGHT_SHAFTS, this);
        requiresCondition(renderingConfig::isLightShafts);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(LIGHT_SHAFTS_FBO, HALF_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(LIGHT_SHAFTS_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(LIGHT_SHAFTS_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new EnableMaterial(LIGHT_SHAFTS_MATERIAL));

        // TODO: move content of ShaderParametersLightShafts to this class
    }

    /**
     * Renders light shafts, taking advantage of the information provided
     * by the color buffer and especially the depth buffer attached to the FBO
     * currently set as read-only.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/lightShafts");

        // The source code for this method is quite short because everything happens in the shader and its setup.
        // In particular see the class ShaderParametersLightShafts and resource lightShafts_frag.glsl
        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
