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
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.*;
import static org.terasology.rendering.opengl.ScalingFactors.*;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;

/**
 * An instance of this class generates a high pass image out of the color content of the GBUFFER and stores
 * the result into HIGH_PASS_FBO, for other nodes to take advantage of it.
 */
public class HighPassNode extends ConditionDependentNode {
    public static final ResourceUrn HIGH_PASS_FBO = new ResourceUrn("engine:fbo.highPass");
    public static final FBOConfig HIGH_PASS_FBO_CONFIG = new FBOConfig(HIGH_PASS_FBO, FULL_SCALE, FBO.Type.DEFAULT);
    private static final ResourceUrn HIGH_PASS_MATERIAL = new ResourceUrn("engine:prog.highPass");

    @Range(min = 0.0f, max = 5.0f)
    private float highPassThreshold = 0.05f;

    @In
    private Config config;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    private Material highPass;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        RenderingConfig renderingConfig = config.getRendering();
        renderingConfig.subscribe(RenderingConfig.BLOOM, this);
        requiresCondition(renderingConfig::isBloom);

        requiresFBO(HIGH_PASS_FBO_CONFIG, displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(HIGH_PASS_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(HIGH_PASS_FBO, displayResolutionDependentFBOs));

        highPass = getMaterial(HIGH_PASS_MATERIAL);
        addDesiredStateChange(new EnableMaterial(HIGH_PASS_MATERIAL.toString()));

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFBO(textureSlot, READONLY_GBUFFER, ColorTexture,
                displayResolutionDependentFBOs, HIGH_PASS_MATERIAL, "tex"));

        // TODO: Investigate why this was commented out (right from the pre-refactoring code)
        //addDesiredStateChange(new SetInputTextureFromFBO(textureSlot + 1, READ_ONLY_GBUFFER, DepthTexture,
        //        displayResolutionDependentFBOs, HIGH_PASS_MATERIAL, "texDepth"));
        // TODO: also verify why SetInputTextureFromFBO only works with ColorTexture but no other texture types.
    }

    /**
     * Generates a high pass image out of the color content of the GBUFFER and stores it
     * into the HIGH_PASS_FBO.
     *
     * This is an entirely 2D process and the only "3D" geometry involved is a full screen quad.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/highpass");

        highPass.setFloat("highPassThreshold", highPassThreshold, true);

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
