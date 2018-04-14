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
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * An instance of this class generates a high pass image out of the color content of the GBUFFER and stores
 * the result into HIGH_PASS_FBO_URI, for other nodes to take advantage of it.
 */
public class HighPassNode extends ConditionDependentNode {
    public static final SimpleUri HIGH_PASS_FBO_URI = new SimpleUri("engine:fbo.highPass");
    public static final FBOConfig HIGH_PASS_FBO_CONFIG = new FBOConfig(HIGH_PASS_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT);
    private static final ResourceUrn HIGH_PASS_MATERIAL_URN = new ResourceUrn("engine:prog.highPass");

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 5.0f)
    private float highPassThreshold = 0.05f;

    private Material highPass;

    public HighPassNode(String nodeUri, Context context) {
        super(nodeUri, context);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.BLOOM, this);
        requiresCondition(renderingConfig::isBloom);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO highPassFbo = requiresFBO(HIGH_PASS_FBO_CONFIG, displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(highPassFbo));
        addDesiredStateChange(new SetViewportToSizeOf(highPassFbo));

        highPass = getMaterial(HIGH_PASS_MATERIAL_URN);
        addDesiredStateChange(new EnableMaterial(HIGH_PASS_MATERIAL_URN));

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot, displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo(), ColorTexture,
                displayResolutionDependentFBOs, HIGH_PASS_MATERIAL_URN, "tex"));
    }

    /**
     * Generates a high pass image out of the color content of the GBUFFER and stores it
     * into the HIGH_PASS_FBO_URI.
     *
     * This is an entirely 2D process and the only "3D" geometry involved is a full screen quad.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        highPass.setFloat("highPassThreshold", highPassThreshold, true);

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
