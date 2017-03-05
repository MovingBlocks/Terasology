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

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.PBO;
import org.terasology.rendering.opengl.ScreenGrabber;
import org.terasology.rendering.opengl.fbms.ImmutableFBOs;
import java.nio.ByteBuffer;

/**
 * An instance of this node takes advantage of a downsampled version of the scene,
 * calculates its relative luminance (1) and updates the exposure parameter of the
 * ScreenGrabber accordingly.
 *
 * Notice that while this node takes advantage of the content of an FBO, it
 * doesn't actually render anything.
 *
 * (1) See https://en.wikipedia.org/wiki/Luma_(video)#Use_of_relative_luminance
 */
public class UpdateExposureNode extends AbstractNode {

    private static final Logger logger = LoggerFactory.getLogger(UpdateExposureNode.class);

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
    private FBO downSampledScene;
    private PBO writeOnlyPBO;   // PBOs are 1x1 pixels buffers used to read GPU data back into the CPU.
                                // This data is then used in the context of eye adaptation.

    /**
     * Initializes an UpdateExposureNode instance.This method must be called once shortly after instantiation
     * to fully initialize the node and make it ready for rendering.
     */
    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        downSampledScene = requiresFBO(DownSamplerForExposureNode.FBO_1X1_CONFIG, immutableFBOs);
        writeOnlyPBO = new PBO(1, 1);
    }

    /**
     * If Eye Adaptation is enabled, given the 1-pixel output of the downSamplerNode,
     * calculates the relative luminance of the scene and updates the exposure accordingly.
     *
     * If Eye Adaptation is disabled, sets the exposure to default day/night values.
     */
    // TODO: verify if this can be achieved entirely in the GPU, during tone mapping perhaps?
    @Override
    public void process() {
        if (renderingConfig.isEyeAdaptation()) {
            PerformanceMonitor.startActivity("rendering/updateExposure");

            writeOnlyPBO.copyFromFBO(downSampledScene.fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);
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

}
