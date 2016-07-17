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
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.PostProcessor;
import org.terasology.rendering.world.WorldRenderer;
import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;

/**
 * TODO: Add node of this diagram
 */
public class DownSampleSceneAndUpdateExposureNode extends AbstractNode {
    private static final Logger logger = LoggerFactory.getLogger(DownSampleSceneAndUpdateExposureNode.class);

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
    private FrameBuffersManager frameBuffersManager;

    @In
    private Config config;

    @In
    private BackdropProvider backdropProvider;

    @In
    private PostProcessor postProcessor;

    private RenderingConfig renderingConfig;
    private FBO sceneOpaque;
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
        obtainStaticFBOs();
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

            frameBuffersManager.getCurrentReadbackPBO().copyFromFBO(downSampledScene[0].fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

            frameBuffersManager.swapReadbackPBOs();

            pixels = frameBuffersManager.getCurrentReadbackPBO().readBackPixels();

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

            postProcessor.setExposure(TeraMath.lerp(postProcessor.getExposure(), targetExposure, hdrExposureAdjustmentSpeed));

            PerformanceMonitor.endActivity();
        } else {
            if (backdropProvider.getDaylight() == 0.0) {
                postProcessor.setExposure(hdrMaxExposureNight);
            } else {
                postProcessor.setExposure(hdrExposureDefault);
            }
        }

    }

    private void downSampleSceneInto1x1pixelsBuffer() {
        PerformanceMonitor.startActivity("rendering/updateExposure/downSampleScene");
        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");
        scenePrePost = frameBuffersManager.getFBO("scenePrePost");

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

        setViewportToSizeOf(sceneOpaque);    // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }

    /**
     * Fetches a number of static FBOs from the FrameBuffersManager instance and initializes a number of
     * internal references with them. They are called "static" as they do not change over the lifetime
     * of a PostProcessor instance.
     * <p>
     * This method must to be called at least once for the PostProcessor instance to function, but does
     * not need to be called additional times.
     * <p>
     * Failure to call this method -may- result in a NullPointerException. This is due to the
     * downsampleSceneAndUpdateExposure() method relying on these FBOs. But this method is fully executed
     * only if eye adaptation is enabled: an NPE would be thrown only in that case.
     */
    private void obtainStaticFBOs() {
        downSampledScene[4] = frameBuffersManager.getFBO("scene16");
        downSampledScene[3] = frameBuffersManager.getFBO("scene8");
        downSampledScene[2] = frameBuffersManager.getFBO("scene4");
        downSampledScene[1] = frameBuffersManager.getFBO("scene2");
        downSampledScene[0] = frameBuffersManager.getFBO("scene1");
    }
}
