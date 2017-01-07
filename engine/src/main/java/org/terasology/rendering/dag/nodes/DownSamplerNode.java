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
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

/**
 * TODO: write
 */
public class DownSamplerNode extends ConditionDependentNode {

    public static final ResourceUrn SCENE_1 = new ResourceUrn("engine:fbo.scene1");

    private static final ResourceUrn SCENE_2 = new ResourceUrn("engine:fbo.scene2");
    private static final ResourceUrn SCENE_4 = new ResourceUrn("engine:fbo.scene4");
    private static final ResourceUrn SCENE_8 = new ResourceUrn("engine:fbo.scene8");
    private static final ResourceUrn SCENE_16 = new ResourceUrn("engine:fbo.scene16");

    private static final ResourceUrn DOWN_SAMPLER_MATERIAL = new ResourceUrn("engine:prog.downSampler");

    @In
    private ImmutableFBOs immutableFBOs;

    @In
    private Config config;

    private FBO[] downSampledScene = new FBO[5];
    private Material downSampler;

    @Override
    public void initialise() {
        RenderingConfig renderingConfig = config.getRendering();
        renderingConfig.subscribe(RenderingConfig.EYE_ADAPTATION, this);
        requiresCondition(renderingConfig::isEyeAdaptation);

        downSampledScene[4] = requiresFBO(new FBOConfig(SCENE_16, 16, 16, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[3] = requiresFBO(new FBOConfig(SCENE_8, 8, 8, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[2] = requiresFBO(new FBOConfig(SCENE_4, 4, 4, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[1] = requiresFBO(new FBOConfig(SCENE_2, 2, 2, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[0] = requiresFBO(new FBOConfig(SCENE_1, 1, 1, FBO.Type.DEFAULT), immutableFBOs);

        addDesiredStateChange(new EnableMaterial(DOWN_SAMPLER_MATERIAL.toString()));
        downSampler = getMaterial(DOWN_SAMPLER_MATERIAL);
    }

    /**
     * DownSamples the content of the GBUFFER into a 1x1 FBO.
     */
    // TODO: verify if this can be achieved entirely in the GPU, during tone mapping perhaps?
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/downSampling");

        for (int i = 4; i >= 0; i--) {
            FBO downSampledFBO = downSampledScene[i];
            downSampler.setFloat("size", downSampledFBO.width(), true);

            if (i == 4) {
                READ_ONLY_GBUFFER.bindTexture();
            } else {
                downSampledScene[i + 1].bindTexture();
            }

            downSampledFBO.bind();
            setViewportToSizeOf(downSampledFBO);

            renderFullscreenQuad();
        }

        PerformanceMonitor.endActivity();
    }

}
