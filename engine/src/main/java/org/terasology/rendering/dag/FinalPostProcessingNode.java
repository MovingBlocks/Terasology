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
package org.terasology.rendering.dag;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL13;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.PostProcessor;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;
import static org.terasology.rendering.opengl.OpenGLUtils.renderFullscreenQuad;
import static org.terasology.rendering.opengl.OpenGLUtils.setViewportToSizeOf;

/**
 * TODO: Add diagram of this node
 * TODO: Break into two different nodes
 */
public class FinalPostProcessingNode implements Node {

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private PostProcessor postProcessor;

    @In
    private FrameBuffersManager frameBuffersManager;

    private RenderingDebugConfig renderingDebugConfig;
    private RenderingConfig renderingConfig;

    private FBO.Dimensions fullScale;

    private Material finalPost;
    private Material debug;
    private Material ocDistortion;

    private FBO sceneFinal;
    private FBO ocUndistorted;
    private FBO sceneOpaque;

    @Override
    public void initialise() {
        renderingConfig = config.getRendering();
        renderingDebugConfig = renderingConfig.getDebug();

        ocDistortion = worldRenderer.getMaterial("engine:prog.ocDistortion");
        finalPost = worldRenderer.getMaterial("engine:prog.post"); // TODO: rename shader to finalPost
        debug = worldRenderer.getMaterial("engine:prog.debug");
    }

    /**
     * If each is enabled through the rendering settings, this method
     * adds depth-of-field blur, motion blur and film grain to the rendering
     * obtained so far. If OculusVR support is enabled, it composes (over two
     * calls) the images for each eye into a single image, and applies a distortion
     * pattern to each, to match the optics in the OculusVR headset.
     * <p>
     * Finally, it either sends the image to the display or, when taking a screenshot,
     * instructs the FrameBuffersManager to save it to a file.
     * <p>
     * worldRenderer.getCurrentRenderStage() Can be MONO, LEFT_EYE or RIGHT_EYE, and communicates to the method weather
     * it is dealing with a standard display or an OculusVR setup, and in the
     * latter case, which eye is currently being rendered. Notice that if the
     * OculusVR support is enabled, the image is sent to screen or saved to
     * file only when the value passed in is RIGHT_EYE, as the processing for
     * the LEFT_EYE comes first and leads to an incomplete image.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/finalpostprocessing");

        ocUndistorted = frameBuffersManager.getFBO("ocUndistorted");
        sceneFinal = frameBuffersManager.getFBO("sceneFinal");
        sceneOpaque = frameBuffersManager.getFBO("sceneOpaque");

        fullScale = sceneOpaque.dimensions();

        if (!renderingDebugConfig.isEnabled()) {
            finalPost.enable();
        } else {
            debug.enable();
        }

        if (!renderingConfig.isOculusVrSupport()) {
            renderFinalMonoImage();
        } else {
            renderFinalStereoImage(worldRenderer.getCurrentRenderStage());
        }

        PerformanceMonitor.endActivity();
    }

    private void renderFinalMonoImage() {
        if (postProcessor.isNotTakingScreenshot()) {
            bindDisplay();
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());

        } else {
            sceneFinal.bind();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());

            postProcessor.saveScreenshot();
            // when saving a screenshot we do not send the image to screen,
            // to avoid the brief one-frame flicker of the screenshot

            // This is needed to avoid the UI (which is not currently saved within the
            // screenshot) being rendered for one frame with buffers.sceneFinal size.
            setViewportToSizeOf(sceneOpaque);
        }
    }

    // TODO: have a flag to invert the eyes (Cross Eye 3D), as mentioned in
    // TODO: http://forum.terasology.org/threads/happy-coding.1018/#post-11264
    private void renderFinalStereoImage(RenderingStage renderingStage) {
        if (postProcessor.isNotTakingScreenshot()) {
            sceneFinal.bind();
        } else {
            ocUndistorted.bind();
        }

        switch (renderingStage) {
            case LEFT_EYE:
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad(0, 0, fullScale.width() / 2, fullScale.height());

                break;

            case RIGHT_EYE:
                // no glClear() here: the rendering for the second eye is being added besides the first eye's rendering
                renderFullscreenQuad(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height());

                if (postProcessor.isNotTakingScreenshot()) {
                    bindDisplay();
                    applyOculusDistortion(sceneFinal);

                } else {
                    sceneFinal.bind();
                    applyOculusDistortion(ocUndistorted);
                    postProcessor.saveScreenshot();
                    // when saving a screenshot we do NOT send the image to screen,
                    // to avoid the brief flicker of the screenshot for one frame
                }

                break;
            case MONO:
                break;
        }
    }

    private void applyOculusDistortion(FBO inputBuffer) {
        ocDistortion.enable();

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        inputBuffer.bindTexture();
        ocDistortion.setInt("texInputBuffer", texId, true);

        if (postProcessor.isNotTakingScreenshot()) {
            updateOcShaderParametersForVP(0, 0, fullScale.width() / 2, fullScale.height(), RenderingStage.LEFT_EYE);
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());
            updateOcShaderParametersForVP(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height(), RenderingStage.RIGHT_EYE);
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());

        } else {
            // what follows -should- work also when there is no screenshot being taken, but somehow it doesn't, hence the block above
            updateOcShaderParametersForVP(0, 0, fullScale.width() / 2, fullScale.height(), RenderingStage.LEFT_EYE);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
            updateOcShaderParametersForVP(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height(), RenderingStage.RIGHT_EYE);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
        }
    }

    private void updateOcShaderParametersForVP(int vpX, int vpY, int vpWidth, int vpHeight, RenderingStage renderingStage) {
        float w = (float) vpWidth / fullScale.width();
        float h = (float) vpHeight / fullScale.height();
        float x = (float) vpX / fullScale.width();
        float y = (float) vpY / fullScale.height();

        float as = (float) vpWidth / vpHeight;

        ocDistortion.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1],
                OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3], true);

        float ocLensCenter = (renderingStage == RenderingStage.RIGHT_EYE)
                ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        ocDistortion.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f, true);
        ocDistortion.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f, true);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        ocDistortion.setFloat2("ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as, true);
        ocDistortion.setFloat2("ocScaleIn", (2 / w), (2 / h) / as, true);
    }
}
