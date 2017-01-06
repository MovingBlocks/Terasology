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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.nui.properties.Range;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.PBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
import java.nio.ByteBuffer;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Break this node into several nodes
 * TODO: Rework on dependency of ScreenGrabber's set/getExposure()
 */
public class DownSampleSceneAndUpdateExposureNode extends AbstractNode {

    private static final Logger logger = LoggerFactory.getLogger(DownSampleSceneAndUpdateExposureNode.class);

    private static final ResourceUrn DOWN_SAMPLER_MATERIAL = new ResourceUrn("engine:prog.downSampler");

    private static final ResourceUrn SCENE_16 = new ResourceUrn("engine:fbo.scene16");
    private static final ResourceUrn SCENE_8 = new ResourceUrn("engine:fbo.scene8");
    private static final ResourceUrn SCENE_4 = new ResourceUrn("engine:fbo.scene4");
    private static final ResourceUrn SCENE_2 = new ResourceUrn("engine:fbo.scene2");
    private static final ResourceUrn SCENE_1 = new ResourceUrn("engine:fbo.scene1");

    private PBO writeOnlyPBO;   // PBOs are 1x1 pixels buffers used to read GPU data back into the CPU.
                                // This data is then used in the context of eye adaptation.

    @Range(min = 0.0f, max = 10.0f)
    private float hdrExposureDefault = 2.5f;
    @Range(min = 0.0f, max = 10.0f)
    private float hdrMaxExposure = 8.0f;
    @Range(min = 0.0f, max = 10.0f)
    private float hdrMaxExposureNight = 8.0f;
    @Range(min = 0.0f, max = 10.0f)
    private float hdrMinExposure = 1.0f;
    @Range(min = 0.0f, max = 4.0f)
    private float hdrTargetLuminance = 1.0f;
    @Range(min = 0.0f, max = 0.5f)
    private float hdrExposureAdjustmentSpeed = 0.05f;

    @In
    private ImmutableFBOs immutableFBOs;

    @In
    private Config config;

    @In
    private BackdropProvider backdropProvider;

    @In
    private ScreenGrabber screenGrabber;

    private RenderingConfig renderingConfig;
    private FBO[] downSampledScene = new FBO[5];
    private Material downSampler;

    @Override
    public void initialise() {

        renderingConfig = config.getRendering();

        downSampledScene[4] = requiresFBO(new FBOConfig(SCENE_16, 16, 16, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[3] = requiresFBO(new FBOConfig(SCENE_8, 8, 8, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[2] = requiresFBO(new FBOConfig(SCENE_4, 4, 4, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[1] = requiresFBO(new FBOConfig(SCENE_2, 2, 2, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[0] = requiresFBO(new FBOConfig(SCENE_1, 1, 1, FBO.Type.DEFAULT), immutableFBOs);

        addDesiredStateChange(new EnableMaterial(DOWN_SAMPLER_MATERIAL.toString()));
        downSampler = getMaterial(DOWN_SAMPLER_MATERIAL);

        writeOnlyPBO = new PBO(1, 1);
    }

    /**
     * First downsamples the rendering obtained so far, after the initial post processing, into a 1x1 pixel buffer.
     * Then calculate its pixel's luma to update the exposure value. This is used later, during tone mapping.
     */
    // TODO: verify if this can be achieved entirely in the GPU, during tone mapping perhaps?
    @Override
    public void process() {
        if (renderingConfig.isEyeAdaptation()) {
            PerformanceMonitor.startActivity("rendering/updateExposure");

            downSampleSceneInto1x1pixelsBuffer();

            writeOnlyPBO.copyFromFBO(downSampledScene[0].fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);
            ByteBuffer pixels = writeOnlyPBO.readBackPixels();

            if (pixels.limit() < 3) {
                logger.error("Failed to auto-update the exposure value.");
                return;
            }

            float red   = (pixels.get(2) & 0xFF) / 255.f;
            float green = (pixels.get(1) & 0xFF) / 255.f;
            float blue  = (pixels.get(0) & 0xFF) / 255.f;

            // See: https://en.wikipedia.org/wiki/Luma_(video)#Use_of_relative_luminance for the constants below.
            float currentSceneLuminance = 0.2126f * red + 0.7152f * green + 0.0722f * blue;

            float targetExposure = hdrMaxExposure;

            if (currentSceneLuminance > 0) {
                targetExposure = hdrTargetLuminance / currentSceneLuminance;
            }

            float maxExposure = hdrMaxExposure;

            if (backdropProvider.getDaylight() == 0.0) {
                maxExposure = hdrMaxExposureNight;
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < hdrMinExposure) {
                targetExposure = hdrMinExposure;
            }

            screenGrabber.setExposure(TeraMath.lerp(screenGrabber.getExposure(), targetExposure, hdrExposureAdjustmentSpeed));

            PerformanceMonitor.endActivity();
        } else {
            if (backdropProvider.getDaylight() == 0.0) {
                screenGrabber.setExposure(hdrMaxExposureNight);
            } else {
                screenGrabber.setExposure(hdrExposureDefault);
            }
        }
    }

    private void downSampleSceneInto1x1pixelsBuffer() {
        PerformanceMonitor.startActivity("rendering/updateExposure/downSampleScene");

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
