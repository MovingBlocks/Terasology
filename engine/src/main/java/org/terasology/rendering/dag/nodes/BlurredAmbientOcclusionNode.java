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
import org.terasology.config.RenderingConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.dag.ConditionDependentNode;
import org.terasology.rendering.dag.stateChanges.BindFBO;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.world.WorldRenderer;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.dag.stateChanges.SetInputTextureFromFBO.FboTexturesTypes.ColorTexture;

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
public class BlurredAmbientOcclusionNode extends ConditionDependentNode implements FBOManagerSubscriber {
    public static final ResourceUrn SSAO_BLURRED_FBO = new ResourceUrn("engine:ssaoBlurred");
    private static final ResourceUrn SSAO_FBO = new ResourceUrn("engine:ssao");
    private static final ResourceUrn SSAO_BLURRED_MATERIAL = new ResourceUrn("engine:prog.ssaoBlur");
    private static final int TEXTURE_SLOT_0 = 0;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    private Material ssaoBlurredMaterial;
    private float outputFboWidth;
    private float outputFboHeight;

    /**
     * This method must be called once shortly after instantiation to fully initialize the node
     * and make it ready for rendering.
     */
    @Override
    public void initialise() {
        RenderingConfig renderingConfig = config.getRendering();
        renderingConfig.subscribe(RenderingConfig.SSAO, this);
        requiresCondition(renderingConfig::isSsao);

        addDesiredStateChange(new EnableMaterial(SSAO_BLURRED_MATERIAL.toString()));
        ssaoBlurredMaterial = getMaterial(SSAO_BLURRED_MATERIAL);

        requiresFBO(new FBOConfig(SSAO_FBO, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        requiresFBO(new FBOConfig(SSAO_BLURRED_FBO, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFBO(SSAO_BLURRED_FBO, displayResolutionDependentFBOs));
        addDesiredStateChange(new SetViewportToSizeOf(SSAO_BLURRED_FBO, displayResolutionDependentFBOs));

        addDesiredStateChange(new SetInputTextureFromFBO(TEXTURE_SLOT_0,
                SSAO_FBO, ColorTexture, displayResolutionDependentFBOs, SSAO_BLURRED_MATERIAL, "tex"));

        displayResolutionDependentFBOs.subscribe(this);
        update(); // initializing outputFboWidth/outputFboHeight
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
        PerformanceMonitor.startActivity("rendering/blurredAmbientOcclusion");

        ssaoBlurredMaterial.setFloat2("texelSize", 1.0f / outputFboWidth, 1.0f / outputFboHeight, true);

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        FBO ssaoBlurredFBO = displayResolutionDependentFBOs.get(SSAO_BLURRED_FBO);
        outputFboWidth = ssaoBlurredFBO.width();
        outputFboHeight = ssaoBlurredFBO.height();
    }
}
