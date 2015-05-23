/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.opengl;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.editor.EditorRange;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.world.WorldRenderer;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO: write javadoc
 */
public class PostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);

    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrExposureDefault = 2.5f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMaxExposure = 8.0f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMaxExposureNight = 8.0f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMinExposure = 1.0f;
    @EditorRange(min = 0.0f, max = 4.0f)
    private float hdrTargetLuminance = 1.0f;
    @EditorRange(min = 0.0f, max = 0.5f)
    private float hdrExposureAdjustmentSpeed = 0.05f;

    @EditorRange(min = 0.0f, max = 5.0f)
    private float bloomHighPassThreshold = 0.75f;
    @EditorRange(min = 0.0f, max = 32.0f)
    private float bloomBlurRadius = 12.0f;

    @EditorRange(min = 0.0f, max = 16.0f)
    private float overallBlurRadiusFactor = 0.8f;

    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;

    private int displayListQuad = -1;
    private FBO.Dimensions fullScale;

    private LwjglRenderingProcess renderingProcess;
    private GraphicState graphicState;
    private Materials materials = new Materials();
    private Buffers buffers = new Buffers();

    private RenderingConfig renderingConfig = CoreRegistry.get(Config.class).getRendering();
    private RenderingDebugConfig renderingDebugConfig = renderingConfig.getDebug();

    public PostProcessor(LwjglRenderingProcess renderingProcess, GraphicState graphicState) {
        this.renderingProcess = renderingProcess;
        this.graphicState = graphicState;
    }

    public void initializeMaterials() {
        // initial renderings
        materials.lightBufferPass = Assets.getMaterial("engine:prog.lightBufferPass");

        // pre-post composite
        materials.outline          = Assets.getMaterial("engine:prog.sobel");
        materials.ssao             = Assets.getMaterial("engine:prog.ssao");
        materials.ssaoBlurred      = Assets.getMaterial("engine:prog.ssaoBlur");
        materials.prePostComposite = Assets.getMaterial("engine:prog.combine");

        // initial post-processing
        materials.lightShafts = Assets.getMaterial("engine:prog.lightshaft");   // TODO: rename shader to lightShafts
        materials.initialPost = Assets.getMaterial("engine:prog.prePost");      // TODO: rename shader to initialPost
        materials.downSampler = Assets.getMaterial("engine:prog.down");         // TODO: rename shader to downSampler
        materials.highPass    = Assets.getMaterial("engine:prog.highp");        // TODO: rename shader to highPass
        materials.blur        = Assets.getMaterial("engine:prog.blur");
        materials.toneMapping = Assets.getMaterial("engine:prog.hdr");          // TODO: rename shader to toneMapping

        // final post-processing
        materials.ocDistortion = Assets.getMaterial("engine:prog.ocDistortion");
        materials.finalPost    = Assets.getMaterial("engine:prog.post");        // TODO: rename shader to finalPost
        materials.debug        = Assets.getMaterial("engine:prog.debug");
    }

    public void obtainStaticFBOs() {
        buffers.downSampledScene[4] = renderingProcess.getFBO("scene16");
        buffers.downSampledScene[3] = renderingProcess.getFBO("scene8");
        buffers.downSampledScene[2] = renderingProcess.getFBO("scene4");
        buffers.downSampledScene[1] = renderingProcess.getFBO("scene2");
        buffers.downSampledScene[0] = renderingProcess.getFBO("scene1");
    }

    public void refreshDynamicFBOs() {
        // initial renderings
        buffers.sceneOpaque         = renderingProcess.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = renderingProcess.getFBO("sceneOpaquePingPong");

        buffers.sceneSkyBand0   = renderingProcess.getFBO("sceneSkyBand0");
        buffers.sceneSkyBand1   = renderingProcess.getFBO("sceneSkyBand1");

        buffers.sceneReflectiveRefractive   = renderingProcess.getFBO("sceneReflectiveRefractive");
        // sceneReflected, in case one wonders, is not used by the post-processor.

        // pre-post composite
        buffers.outline         = renderingProcess.getFBO("outline");
        buffers.ssao            = renderingProcess.getFBO("ssao");
        buffers.ssaoBlurred     = renderingProcess.getFBO("ssaoBlurred");

        // initial post-processing
        buffers.lightShafts     = renderingProcess.getFBO("lightShafts");
        buffers.initialPost     = renderingProcess.getFBO("initialPost");
        buffers.currentReadbackPBO = renderingProcess.getCurrentReadbackPBO();
        buffers.sceneToneMapped = renderingProcess.getFBO("sceneToneMapped");

        buffers.sceneHighPass   = renderingProcess.getFBO("sceneHighPass");
        buffers.sceneBloom0     = renderingProcess.getFBO("sceneBloom0");
        buffers.sceneBloom1     = renderingProcess.getFBO("sceneBloom1");
        buffers.sceneBloom2     = renderingProcess.getFBO("sceneBloom2");

        buffers.sceneBlur0     = renderingProcess.getFBO("sceneBlur0");
        buffers.sceneBlur1     = renderingProcess.getFBO("sceneBlur1");

        // final post-processing
        buffers.ocUndistorted   = renderingProcess.getFBO("ocUndistorted");
        buffers.sceneFinal      = renderingProcess.getFBO("sceneFinal");

        fullScale = buffers.sceneOpaque.dimensions();
    }

    public void refreshSceneOpaqueFBOs() {
        buffers.sceneOpaque         = renderingProcess.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = renderingProcess.getFBO("sceneOpaquePingPong");
    }

    public void dispose() {
        renderingProcess = null;
        graphicState = null;
        fullScale = null;
    }

    public void generateSkyBands() {
        if (renderingConfig.isInscattering()) {
            generateSkyBand(buffers.sceneSkyBand0);
            generateSkyBand(buffers.sceneSkyBand1);
        }
    }

    public void generateSkyBand(FBO skyBand) {
        skyBand.bind();
        graphicState.setRenderBufferMask(skyBand, true, false, false);

        materials.blur.enable();
        materials.blur.setFloat("radius", 8.0f, true);
        materials.blur.setFloat2("texelSize", 1.0f / skyBand.width(), 1.0f / skyBand.height(), true);

        if (skyBand == buffers.sceneSkyBand0) {
            buffers.sceneOpaque.bindTexture();
        } else {
            buffers.sceneSkyBand0.bindTexture();
        }

        setViewportTo(skyBand.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    public void applyLightBufferPass() {

        int texId = 0;

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindTexture();
        materials.lightBufferPass.setInt("texSceneOpaque", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindDepthTexture();
        materials.lightBufferPass.setInt("texSceneOpaqueDepth", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindNormalsTexture();
        materials.lightBufferPass.setInt("texSceneOpaqueNormals", texId++);

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindLightBufferTexture();
        materials.lightBufferPass.setInt("texSceneOpaqueLightBuffer", texId++, true);

        buffers.sceneOpaquePingPong.bind();
        graphicState.setRenderBufferMask(buffers.sceneOpaquePingPong, true, true, true);

        setViewportTo(buffers.sceneOpaquePingPong.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();

        renderingProcess.swapSceneOpaqueFBOs();
        buffers.sceneOpaque.attachDepthBufferTo(buffers.sceneReflectiveRefractive);
    }

    public void generateOutline() {
        if (renderingConfig.isOutline()) {
            materials.outline.enable();

            buffers.outline.bind();

            setViewportTo(buffers.outline.dimensions());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderFullscreenQuad();

            graphicState.bindDisplay();
            setViewportToWholeDisplay();
        }
    }

    public void generateAmbientOcclusionPasses() {
        if (renderingConfig.isSsao()) {
            generateSSAO();
            generateBlurredSSAO();
        }
    }

    private void generateSSAO() {
        materials.ssao.enable();

        materials.ssao.setFloat2("texelSize", 1.0f / buffers.ssao.width(), 1.0f / buffers.ssao.height(), true);
        materials.ssao.setFloat2("noiseTexelSize", 1.0f / 4.0f, 1.0f / 4.0f, true);

        buffers.ssao.bind();

        setViewportTo(buffers.ssao.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    private void generateBlurredSSAO() {
        materials.ssaoBlurred.enable();
        materials.ssaoBlurred.setFloat2("texelSize", 1.0f / buffers.ssaoBlurred.width(), 1.0f / buffers.ssaoBlurred.height(), true);

        buffers.ssaoBlurred.bind();

        setViewportTo(buffers.ssaoBlurred.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        buffers.ssao.bindTexture();

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    public void generatePrePostComposite() {
        materials.prePostComposite.enable();

        buffers.sceneOpaquePingPong.bind();
        setViewportTo(buffers.sceneOpaquePingPong.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();

        renderingProcess.swapSceneOpaqueFBOs();
        buffers.sceneOpaque.attachDepthBufferTo(buffers.sceneReflectiveRefractive);
    }

    public void generateLightShafts() {
        if (renderingConfig.isLightShafts()) {
            PerformanceMonitor.startActivity("Rendering light shafts");

            materials.lightShafts.enable();

            buffers.lightShafts.bind();

            setViewportTo(buffers.lightShafts.dimensions());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            renderFullscreenQuad();

            graphicState.bindDisplay();
            setViewportToWholeDisplay();

            PerformanceMonitor.endActivity();
        }
    }

    public void initialPostProcessing() {
        PerformanceMonitor.startActivity("Initial Post-Processing");
        materials.initialPost.enable();

        buffers.initialPost.bind();

        setViewportTo(buffers.initialPost.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();

        PerformanceMonitor.endActivity();
    }

    private void downsampleSceneInto1x1pixelsBuffer() {
        PerformanceMonitor.startActivity("Rendering eye adaption");

        materials.downSampler.enable();
        FBO downSampledFBO;

        for (int i = 4; i >= 0; i--) {

            downSampledFBO = buffers.downSampledScene[i];
            materials.downSampler.setFloat("size", downSampledFBO.width(), true);

            downSampledFBO.bind();

            setViewportTo(downSampledFBO.dimensions());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 4) {
                buffers.initialPost.bindTexture();
            } else {
                buffers.downSampledScene[i + 1].bindTexture();
            }

            renderFullscreenQuad();

            graphicState.bindDisplay(); // TODO: probably can be removed or moved out of the loop
        }

        setViewportToWholeDisplay();

        PerformanceMonitor.endActivity();
    }

    public void downsampleSceneAndUpdateExposure() {
        if (renderingConfig.isEyeAdaptation()) {
            PerformanceMonitor.startActivity("Updating exposure");

            downsampleSceneInto1x1pixelsBuffer();

            renderingProcess.getCurrentReadbackPBO().copyFromFBO(buffers.downSampledScene[0].fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

            renderingProcess.swapReadbackPBOs();

            ByteBuffer pixels = renderingProcess.getCurrentReadbackPBO().readBackPixels();

            if (pixels.limit() < 3) {
                logger.error("Failed to auto-update the exposure value.");
                return;
            }

            currentSceneLuminance = 0.2126f * (pixels.get(2) & 0xFF) / 255.f + 0.7152f * (pixels.get(1) & 0xFF) / 255.f + 0.0722f * (pixels.get(0) & 0xFF) / 255.f;

            float targetExposure = hdrMaxExposure;

            if (currentSceneLuminance > 0) {
                targetExposure = hdrTargetLuminance / currentSceneLuminance;
            }

            float maxExposure = hdrMaxExposure;

            if (CoreRegistry.get(BackdropProvider.class).getDaylight() == 0.0) {
                maxExposure = hdrMaxExposureNight;
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < hdrMinExposure) {
                targetExposure = hdrMinExposure;
            }

            currentExposure = (float) TeraMath.lerp(currentExposure, targetExposure, hdrExposureAdjustmentSpeed);

        } else {
            if (CoreRegistry.get(BackdropProvider.class).getDaylight() == 0.0) {
                currentExposure = hdrMaxExposureNight;
            } else {
                currentExposure = hdrExposureDefault;
            }
        }
        PerformanceMonitor.endActivity();
    }

    public void generateToneMappedScene() {
        PerformanceMonitor.startActivity("Tone mapping");

        materials.toneMapping.enable();

        buffers.sceneToneMapped.bind();
        setViewportTo(buffers.sceneToneMapped.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();

        PerformanceMonitor.endActivity();
    }

    public void generateBloomPasses() {
        if (renderingConfig.isBloom()) {
            PerformanceMonitor.startActivity("Generating Bloom Passes");
            generateHighPass();
            generateBloom(buffers.sceneBloom0);
            generateBloom(buffers.sceneBloom1);
            generateBloom(buffers.sceneBloom2);
            PerformanceMonitor.endActivity();
        }
    }

    private void generateHighPass() {
        materials.highPass.enable();
        materials.highPass.setFloat("highPassThreshold", bloomHighPassThreshold, true);

        buffers.sceneHighPass.bind();

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindTexture();
        materials.highPass.setInt("tex", texId++);

//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
//        buffers.sceneOpaque.bindDepthTexture();
//        program.setInt("texDepth", texId++);

        setViewportTo(buffers.sceneHighPass.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    private void generateBloom(FBO sceneBloom) {
        materials.blur.enable();
        materials.blur.setFloat("radius", bloomBlurRadius, true);
        materials.blur.setFloat2("texelSize", 1.0f / sceneBloom.width(), 1.0f / sceneBloom.height(), true);

        sceneBloom.bind();

        graphicState.setViewportToSizeOf(sceneBloom);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (sceneBloom == buffers.sceneBloom0) {
            buffers.sceneHighPass.bindTexture();
        } else if (sceneBloom == buffers.sceneBloom1) {
            buffers.sceneBloom0.bindTexture();
        } else {
            buffers.sceneBloom1.bindTexture();
        }

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    public void generateBlurPasses() {
        if (renderingConfig.getBlurIntensity() != 0) {
            PerformanceMonitor.startActivity("Generating Blur Passes");
            generateBlur(buffers.sceneBlur0);
            generateBlur(buffers.sceneBlur1);
            PerformanceMonitor.endActivity();
        }
    }

    private void generateBlur(FBO sceneBlur) {
        materials.blur.enable();
        materials.blur.setFloat("radius", overallBlurRadiusFactor * renderingConfig.getBlurRadius(), true);
        materials.blur.setFloat2("texelSize", 1.0f / sceneBlur.width(), 1.0f / sceneBlur.height(), true);

        sceneBlur.bind();

        graphicState.setViewportToSizeOf(sceneBlur);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (sceneBlur == buffers.sceneBlur0) {
            buffers.sceneToneMapped.bindTexture();
        } else {
            buffers.sceneBlur0.bindTexture();
        }

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    public void finalPostProcessing(WorldRenderer.WorldRenderingStage renderingStage) {
        PerformanceMonitor.startActivity("Rendering final scene");

        if (!renderingDebugConfig.isEnabled()) {
            materials.finalPost.enable();
        } else {
            materials.debug.enable();
        }

        if (!renderingConfig.isOculusVrSupport()) {
            renderFinalMonoImage();
        } else {
            renderFinalStereoImage(renderingStage);
        }

        PerformanceMonitor.endActivity();
    }

    private void renderFinalMonoImage() {

        if (renderingProcess.isNotTakingScreenshot()) {
            graphicState.bindDisplay();
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());

        } else {
            buffers.sceneFinal.bind();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());

            renderingProcess.saveScreenshot();
            // when saving a screenshot we do not send the image to screen,
            // to avoid the brief one-frame flicker of the screenshot

            // This is needed to avoid the UI (which is not currently saved within the
            // screenshot) being rendered for one frame with buffers.sceneFinal size.
            setViewportToWholeDisplay();
        }
    }

    // TODO: have a flag to invert the eyes (Cross Eye 3D), as mentioned in
    // TODO: http://forum.terasology.org/threads/happy-coding.1018/#post-11264
    private void renderFinalStereoImage(WorldRenderer.WorldRenderingStage renderingStage) {
        if (renderingProcess.isNotTakingScreenshot()) {
            buffers.sceneFinal.bind();
        } else {
            buffers.ocUndistorted.bind();
        }

        switch (renderingStage) {
            case LEFT_EYE:
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                renderFullscreenQuad(0, 0, fullScale.width() / 2, fullScale.height());

                break;

            case RIGHT_EYE:
                // no glClear() here: the rendering for the second eye is being added besides the first eye's rendering
                renderFullscreenQuad(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height());

                if (renderingProcess.isNotTakingScreenshot()) {
                    graphicState.bindDisplay();
                    applyOculusDistortion(buffers.sceneFinal);

                } else {
                    buffers.sceneFinal.bind();
                    applyOculusDistortion(buffers.ocUndistorted);
                    renderingProcess.saveScreenshot();
                    // when saving a screenshot we do NOT send the image to screen,
                    // to avoid the brief flicker of the screenshot for one frame
                }

                break;
        }
    }

    private void applyOculusDistortion(FBO inputBuffer) {
        materials.ocDistortion.enable();

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        inputBuffer.bindTexture();
        materials.ocDistortion.setInt("texInputBuffer", texId, true);

        if (renderingProcess.isNotTakingScreenshot()) {
            updateOcShaderParametersForVP(0, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.LEFT_EYE);
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());
            updateOcShaderParametersForVP(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.RIGHT_EYE);
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());

        } else {
            // what follows -should- work also when there is no screenshot being taken, but somehow it doesn't, hence the block above
            updateOcShaderParametersForVP(0, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.LEFT_EYE);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
            updateOcShaderParametersForVP(fullScale.width() / 2 + 1, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.RIGHT_EYE);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
        }
    }

    private void updateOcShaderParametersForVP(int vpX, int vpY, int vpWidth, int vpHeight, WorldRenderer.WorldRenderingStage renderingStage) {
        float w = (float) vpWidth / fullScale.width();
        float h = (float) vpHeight / fullScale.height();
        float x = (float) vpX / fullScale.width();
        float y = (float) vpY / fullScale.height();

        float as = (float) vpWidth / vpHeight;

        materials.ocDistortion.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1],
                OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3], true);

        float ocLensCenter = (renderingStage == WorldRenderer.WorldRenderingStage.RIGHT_EYE)
                ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        materials.ocDistortion.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f, true);
        materials.ocDistortion.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f, true);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        materials.ocDistortion.setFloat2("ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as, true);
        materials.ocDistortion.setFloat2("ocScaleIn", (2 / w), (2 / h) / as, true);
    }

    public void renderFullscreenQuad() {
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        renderQuad();

        glPopMatrix();

        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    public void renderFullscreenQuad(int x, int y, int viewportWidth, int viewportHeight) {
        glViewport(x, y, viewportWidth, viewportHeight);
        // TODO: replace what follows with renderFullscreenQuad()

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();

        renderQuad();

        glPopMatrix();

        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }

    // TODO: replace with a proper resident buffer with interleaved vertex and uv coordinates
    private void renderQuad() {
        if (displayListQuad == -1) {
            displayListQuad = glGenLists(1);

            glNewList(displayListQuad, GL11.GL_COMPILE);

            glBegin(GL_QUADS);
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

            glTexCoord2d(0.0, 0.0);
            glVertex3i(-1, -1, -1);

            glTexCoord2d(1.0, 0.0);
            glVertex3i(1, -1, -1);

            glTexCoord2d(1.0, 1.0);
            glVertex3i(1, 1, -1);

            glTexCoord2d(0.0, 1.0);
            glVertex3i(-1, 1, -1);

            glEnd();

            glEndList();
        }

        glCallList(displayListQuad);
    }

    private void setViewportToWholeDisplay() {
        glViewport(0, 0, fullScale.width(), fullScale.height());
    }

    private void setViewportTo(FBO.Dimensions dimensions) {
        glViewport(0, 0, dimensions.width(), dimensions.height());
    }

    public float getExposure() {
        return currentExposure;
    }

    private class Materials {
        // initial renderings
        public Material lightBufferPass;

        // pre-post composite
        public Material outline;
        public Material ssao;
        public Material ssaoBlurred;
        public Material prePostComposite;

        // initial post-processing
        public Material lightShafts;
        public Material downSampler;
        public Material highPass;
        public Material blur;
        public Material toneMapping;
        public Material initialPost;

        // final post-processing
        public Material ocDistortion;
        public Material finalPost;
        public Material debug;
    }

    private class Buffers {
        // initial renderings
        public FBO sceneOpaque;
        public FBO sceneOpaquePingPong;

        public FBO sceneSkyBand0;
        public FBO sceneSkyBand1;

        public FBO sceneReflectiveRefractive;
        // sceneReflected is not used by the postProcessor

        // pre-post composite
        public FBO outline;
        public FBO ssao;
        public FBO ssaoBlurred;
        public FBO initialPost;

        // initial post-processing
        public FBO lightShafts;

        public FBO[] downSampledScene = new FBO[5];
        public PBO currentReadbackPBO;

        public FBO sceneToneMapped;

        public FBO sceneHighPass;
        public FBO sceneBloom0;
        public FBO sceneBloom1;
        public FBO sceneBloom2;

        public FBO sceneBlur0;
        public FBO sceneBlur1;

        // final post-processing
        public FBO ocUndistorted;
        public FBO sceneFinal;
    }
}
