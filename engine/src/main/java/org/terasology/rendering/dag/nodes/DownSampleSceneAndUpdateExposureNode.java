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
import org.terasology.rendering.nui.properties.Range;
import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FBOConfig;
import org.terasology.rendering.opengl.PBO;
import static org.terasology.rendering.opengl.ScalingFactors.FULL_SCALE;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
import org.terasology.rendering.world.WorldRenderer;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Break this node into several nodes
 * TODO: Rework on dependency of ScreenGrabber's set/getExposure()
 */
public class DownSampleSceneAndUpdateExposureNode extends AbstractNode {
    public static final ResourceUrn SCENE_16 = new ResourceUrn("engine:scene16");
    public static final ResourceUrn SCENE_8 = new ResourceUrn("engine:scene8");
    public static final ResourceUrn SCENE_4 = new ResourceUrn("engine:scene4");
    public static final ResourceUrn SCENE_2 = new ResourceUrn("engine:scene2");
    public static final ResourceUrn SCENE_1 = new ResourceUrn("engine:scene1");
    public static final ResourceUrn SCENE_PRE_POST = new ResourceUrn("engine:scenePrePost");

    private static final Logger logger = LoggerFactory.getLogger(DownSampleSceneAndUpdateExposureNode.class);

    private PBO writeOnlyPBO;   // PBOs are 1x1 pixels buffers used to read GPU data back into the CPU.
    private PBO readOnlyPBO;    // This data is then used in the context of eye adaptation.
    private PBO temporaryPBO;

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

    private float currentSceneLuminance = 1.0f;

    @In
    private WorldRenderer worldRenderer;

    @In
    private ImmutableFBOs immutableFBOs;

    @In
    private DisplayResolutionDependentFBOs displayResolutionDependentFBOs;


    @In
    private Config config;

    @In
    private BackdropProvider backdropProvider;

    @In
    private ScreenGrabber screenGrabber;

    private RenderingConfig renderingConfig;

    private FBO scenePrePost;
    private FBO downSampledFBO;
    private FBO[] downSampledScene = new FBO[5];
    private Material downSampler;
    private ByteBuffer pixels;
    private float targetExposure;
    private float maxExposure;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        downSampler = worldRenderer.getMaterial("engine:prog.down");         // TODO: rename shader to downSampler
        downSampledScene[4] = requiresFBO(new FBOConfig(SCENE_16, 16, 16, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[3] = requiresFBO(new FBOConfig(SCENE_8, 8, 8, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[2] = requiresFBO(new FBOConfig(SCENE_4, 4, 4, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[1] = requiresFBO(new FBOConfig(SCENE_2, 2, 2, FBO.Type.DEFAULT), immutableFBOs);
        downSampledScene[0] = requiresFBO(new FBOConfig(SCENE_1, 1, 1, FBO.Type.DEFAULT), immutableFBOs);
        requiresFBO(new FBOConfig(SCENE_PRE_POST, FULL_SCALE, FBO.Type.HDR), displayResolutionDependentFBOs);

        createPBOs();
    }


    private void createPBOs() {
        writeOnlyPBO = new PBO(1, 1);
        readOnlyPBO = new PBO(1, 1);
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
            swapPBOs();
            pixels = readOnlyPBO.readBackPixels();

            if (pixels.limit() < 3) {
                logger.error("Failed to auto-update the exposure value.");
                return;
            }

            // TODO: make this line more readable by breaking it in smaller pieces
            currentSceneLuminance = 0.2126f * (pixels.get(2) & 0xFF) / 255.f + 0.7152f * (pixels.get(1) & 0xFF) / 255.f + 0.0722f * (pixels.get(0) & 0xFF) / 255.f;

            targetExposure = hdrMaxExposure;

            if (currentSceneLuminance > 0) {
                targetExposure = hdrTargetLuminance / currentSceneLuminance;
            }

            maxExposure = hdrMaxExposure;

            if (backdropProvider.getDaylight() == 0.0) {    // TODO: fetch the backdropProvider earlier and only once
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

    private void swapPBOs() {
        temporaryPBO = readOnlyPBO;
        readOnlyPBO = writeOnlyPBO;
        writeOnlyPBO = temporaryPBO;
    }

    private void downSampleSceneInto1x1pixelsBuffer() {
        PerformanceMonitor.startActivity("rendering/updateExposure/downSampleScene");
        scenePrePost = displayResolutionDependentFBOs.get(SCENE_PRE_POST);

        downSampler.enable();


        for (int i = 4; i >= 0; i--) {
            downSampledFBO = downSampledScene[i];
            downSampler.setFloat("size", downSampledFBO.width(), true);

            downSampledFBO.bind();

            setViewportToSizeOf(downSampledFBO);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

            // TODO: move this block above, for consistency
            if (i == 4) {
                scenePrePost.bindTexture();
            } else {
                downSampledScene[i + 1].bindTexture();
            }

            renderFullscreenQuad();

            bindDisplay(); // TODO: probably can be removed or moved out of the loop
        }

        setViewportToSizeOf(READ_ONLY_GBUFFER); // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }

}
