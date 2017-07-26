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
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.FBOManagerSubscriber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.shader.ShaderParametersSSAO;

import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;

/**
 * Instances of this node work in tandem with instances of the BlurredAmbientOcclusionNode class.
 * Together they constitute an ambient occlusion pass.
 *
 * This particular node generates a first, sharper ambient occlusion output. Subsequently that's
 * used by the BlurredAmbientOcclusionNode to make it softer.
 *
 * At this stage only the output of BlurredAmbientOcclusionNode is used to enhance the image eventually
 * shown on screen to the user. It is currently not possible to use the sharper output produced by
 * this node alone, i.e. to have lower quality but faster ambient occlusions.
 *
 * Ambient occlusion is a subtle visual effect that makes the rendering of the world more pleasing
 * at the cost of some additional milliseconds per frame. Disabling it may lead to increased frame
 * rate while the gameplay remains unaffected.
 *
 * See http://en.wikipedia.org/wiki/Ambient_occlusion for more information on this technique.
 */
public class AmbientOcclusionNode extends ConditionDependentNode implements FBOManagerSubscriber {
    public static final SimpleUri SSAO_FBO_URI = new SimpleUri("engine:fbo.ssao");
    private static final ResourceUrn SSAO_MATERIAL_URN = new ResourceUrn("engine:prog.ssao");
    private static final float NOISE_TEXEL_SIZE = 0.25f;

    private Material ssaoMaterial;
    private float outputFboWidth;
    private float outputFboHeight;

    private FBO ssaoFbo;

    public AmbientOcclusionNode(Context context) {
        super(context);

        RenderingConfig renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.SSAO, this);
        requiresCondition(renderingConfig::isSsao);

        addDesiredStateChange(new EnableMaterial(SSAO_MATERIAL_URN));
        ssaoMaterial = getMaterial(SSAO_MATERIAL_URN);

        DisplayResolutionDependentFBOs displayResolutionDependentFBOs = context.get(DisplayResolutionDependentFBOs.class);
        ssaoFbo = requiresFBO(new FBOConfig(SSAO_FBO_URI, FULL_SCALE, FBO.Type.DEFAULT), displayResolutionDependentFBOs);
        addDesiredStateChange(new BindFbo(ssaoFbo));
        addDesiredStateChange(new SetViewportToSizeOf(ssaoFbo));
        update(); // Cheeky way to initialise outputFboWidth, outputFboHeight
        displayResolutionDependentFBOs.subscribe(this);

        ShaderParametersSSAO.setLastUpdatedGBuffer(displayResolutionDependentFBOs.getGBufferPair().getLastUpdatedFbo());

        // TODO: check for input textures brought in by the material
    }

    /**
     * If Ambient Occlusion is enabled in the render settings, this method generates and
     * stores the necessary images into their own FBOs. The stored images are eventually
     * combined with others.
     * <p>
     * For further information on Ambient Occlusion see: http://en.wikipedia.org/wiki/Ambient_occlusion
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/ambientOcclusion");

        ssaoMaterial.setFloat2("texelSize", 1.0f / outputFboWidth, 1.0f / outputFboHeight, true);
        ssaoMaterial.setFloat2("noiseTexelSize", NOISE_TEXEL_SIZE, NOISE_TEXEL_SIZE, true);

        renderFullscreenQuad();

        PerformanceMonitor.endActivity();
    }

    @Override
    public void update() {
        outputFboWidth = ssaoFbo.width();
        outputFboHeight = ssaoFbo.height();
    }
}
