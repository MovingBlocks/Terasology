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
import org.terasology.engine.SimpleUri;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import static org.terasology.rendering.dag.nodes.InitialPostProcessingNode.INITIAL_POST_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * The exposure calculated earlier in the rendering process is used by an instance
 * of this node to remap the colors of the image rendered so far, brightening otherwise
 * undetailed dark areas or dimming otherwise burnt bright areas, depending on the circumstances.
 *
 * For more details on the specific algorithm used see shader resource toneMapping_frag.glsl.
 *
 * This node stores its output in TONE_MAPPED_FBO_URI.
 */
public class ToneMappingNode extends AbstractNode {
    public static final SimpleUri TONE_MAPPING_FBO_URI = new SimpleUri("engine:fbo.toneMapping");
    private static final ResourceUrn TONE_MAPPING_MATERIAL_URN = new ResourceUrn("engine:prog.toneMapping");

    private ScreenGrabber screenGrabber;

    private Material toneMappingMaterial;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 10.0f)
    private float exposureBias = 1.0f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 100.0f)
    private float whitePoint = 9f;

    public ToneMappingNode(String nodeUri, Context context) {
        super(nodeUri, context);

        screenGrabber = context.get(ScreenGrabber.class);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        FBO toneMappingFbo = requiresFBO(new FBOConfig(TONE_MAPPING_FBO_URI, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(toneMappingFbo));
        addDesiredStateChange(new SetViewportToSizeOf(toneMappingFbo));

        addDesiredStateChange(new EnableMaterial(TONE_MAPPING_MATERIAL_URN));

        toneMappingMaterial = getMaterial(TONE_MAPPING_MATERIAL_URN);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTextureFromFbo(textureSlot, INITIAL_POST_FBO_URI, ColorTexture, displayResolutionDependentFBOs, TONE_MAPPING_MATERIAL_URN, "texScene"));
    }

    /**
     * Renders a full screen quad with the opengl state defined by the initialise() method,
     * using the GBUFFER as input and filling the TONE_MAPPED_FBO_URI with the output of
     * the shader operations. As such, this method performs purely 2D operations.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        // Specific Shader Parameters
        toneMappingMaterial.setFloat("exposure", screenGrabber.getExposure() * exposureBias, true);
        toneMappingMaterial.setFloat("whitePoint", whitePoint, true);

        // Actual Node Processing

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }
}
