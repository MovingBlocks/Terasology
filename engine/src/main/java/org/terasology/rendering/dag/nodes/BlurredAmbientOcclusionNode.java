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
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;

import java.beans.PropertyChangeEvent;

import static org.terasology.rendering.dag.nodes.AmbientOcclusionNode.SSAO_FBO_URI;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFbo.FboTexturesTypes.ColorTexture;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.POST_FBO_REGENERATION;

/**
 * Instances of this node work in tandem with instances of the AmbientOcclusionNode class.
 * Together they constitute an ambient occlusion pass.
 *
 * This particular node blurs the ambient occlusion output produced by the AmbientOcclusionNode,
 * making it softer and more subtle. At this stage only the output of this node is used to enhance
 * the image eventually shown on screen to the user. It is currently not possible to use the (sharper)
 * output of the AmbientOcclusionNode alone, i.e. to have lower quality but faster ambient occlusion.
 *
 * Ambient occlusion is a subtle visual effect that makes the rendering of the world more pleasing
 * at the cost of some additional milliseconds per frame. Disabling it may lead to increased frame
 * rate while the gameplay remains unaffected.
 *
 * See http://en.wikipedia.org/wiki/Ambient_occlusion for more information on this technique.
 */
public class BlurredAmbientOcclusionNode extends ConditionDependentNode {
    public static final SimpleUri SSAO_BLURRED_FBO_URI = new SimpleUri("engine:fbo.ssaoBlurred");
    private static final ResourceUrn SSAO_BLURRED_MATERIAL_URN = new ResourceUrn("engine:prog.ssaoBlur");

    private Material ssaoBlurredMaterial;
    private float outputFboWidth;
    private float outputFboHeight;

    private FBO ssaoBlurredFbo;

    public BlurredAmbientOcclusionNode(String nodeUri, Context context) {
        super(nodeUri, context);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.SSAO, this);
        requiresCondition(renderingConfig::isSsao);

        addDesiredStateChange(new EnableMaterial(SSAO_BLURRED_MATERIAL_URN));
        ssaoBlurredMaterial = getMaterial(SSAO_BLURRED_MATERIAL_URN);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        requiresFBO(new FBOConfig(SSAO_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        ssaoBlurredFbo = requiresFBO(new FBOConfig(SSAO_BLURRED_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(ssaoBlurredFbo));
        addDesiredStateChange(new SetViewportToSizeOf(ssaoBlurredFbo));
        displayResolutionDependentFBOs.subscribe(POST_FBO_REGENERATION, this);

        retrieveFboDimensions();

        addDesiredStateChange(new SetInputTextureFromFbo(0, SSAO_FBO_URI, ColorTexture,
                displayResolutionDependentFBOs, SSAO_BLURRED_MATERIAL_URN, "tex"));
    }

    /**
     * If Ambient Occlusion is enabled in the render settings, this method generates and
     * stores the necessary images into their own FBOs. The stored images are eventually
     * combined with others to enhance the image shown on screen.
     * <p>
     * For further information on Ambient Occlusion see: http://en.wikipedia.org/wiki/Ambient_occlusion
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/" + getUri());

        ssaoBlurredMaterial.setFloat2("texelSize", 1.0f / outputFboWidth, 1.0f / outputFboHeight, true);

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String propertyName = event.getPropertyName();

        switch (propertyName) {
            case RenderingConfig.SSAO:
                super.propertyChange(event);
                break;

            case POST_FBO_REGENERATION:
                retrieveFboDimensions();
                break;

            // default: no other cases are possible - see subscribe operations in initialize.
        }
    }

    private void retrieveFboDimensions() {
        outputFboWidth = ssaoBlurredFbo.width();
        outputFboHeight = ssaoBlurredFbo.height();
    }
}
