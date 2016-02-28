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
import org.terasology.utilities.Assets;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.common.ThreadManager;
import org.terasology.math.TeraMath;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.world.WorldRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.lwjgl.opengl.GL11.*;

/**
 * The term "Post Processing" is in analogy to what occurs in the world of Photography:
 * first a number of shots are taken on location or in a studio. The resulting images
 * are then post-processed to achieve specific aesthetic or technical goals.
 *
 * In the context of Terasology, the WorldRenderer takes care of "taking the shots
 * on location" (a world of 3D data such as vertices, uv coordinates and so on)
 * while the PostProcessor starts from the resulting 2D buffers (which in most
 * cases can be thought as images) and through a series of steps it generates the
 * image seen on screen or saved in a screenshot.
 *
 * Most PostProcessor methods represent a single step of the process. I.e the
 * method generateLightShafts() only adds light shafts to the image.
 *
 * Also, most methods follow a typical pattern of operations:
 * 1) enable a material (a shader bundled with its control parameters)
 * 2) bind zero or more 2D buffers to sample from
 * 3) bind and configure an FBO to output to
 * 4) render a quad, usually covering the whole buffer area, but sometimes less
 *
 * Finally, post-processing can be thought as a part of the rendering pipeline,
 * the set of conceptually atomic steps (sometimes called "passes") that eventually
 * leads to the final image. In practice however these steps are not really arranged
 * in a "line" where a given step takes the output of the previous step and provides
 * an input to the next one. Some steps have no dependencies on previous steps and
 * only provide the basis for further processing. Other steps have instead one or
 * more inputs and in turn become the input to one or more further passes.
 *
 * The rendering pipeline the methods of the PostProcessor contribute to is therefore
 * better thought as a Directed Acyclic Graph (DAG) in which each step is a node
 * that might or might not have previous nodes in input but will always have at least
 * an output, with the final writing to the default framebuffer (the display) or
 * to a screenshot file.
 */
// TODO: Future work should not only "think" in terms of a DAG-like rendering pipeline
// TODO: but actually implement one, see https://github.com/MovingBlocks/Terasology/issues/1741
public class PostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);

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

    @Range(min = 0.0f, max = 5.0f)
    private float bloomHighPassThreshold = 0.75f;
    @Range(min = 0.0f, max = 32.0f)
    private float bloomBlurRadius = 12.0f;

    @Range(min = 0.0f, max = 16.0f)
    private float overallBlurRadiusFactor = 0.8f;

    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;

    private int displayListQuad = -1;
    private FBO.Dimensions fullScale;

    private FrameBuffersManager buffersManager;
    private GraphicState graphicState;
    private Materials materials = new Materials();
    private Buffers buffers = new Buffers();

    ThreadManager threadManager = CoreRegistry.get(ThreadManager.class);

    private boolean isTakingScreenshot;

    private RenderingConfig renderingConfig = CoreRegistry.get(Config.class).getRendering();
    private RenderingDebugConfig renderingDebugConfig = renderingConfig.getDebug();

    /**
     * Returns a PostProcessor instance. On instantiation the returned instance is not
     * yet usable. It lacks references to Material assets and Frame Buffer Objects (FBOs)
     * it needs to function.
     *
     * Method initializeMaterials() must be called to initialize the Materials references.
     * Method obtainStaticFBOs() must be called to initialize unchanging FBOs references.
     * Method refreshDynamicFBOs() must be called at least once to initialize all other FBOs references.
     *
     * @param buffersManager An FrameBuffersManager instance, required to obtain FBO references.
     * @param graphicState A GraphicState instance, providing opengl state-changing methods.
     */
    public PostProcessor(FrameBuffersManager buffersManager, GraphicState graphicState) {
        this.buffersManager = buffersManager;
        this.graphicState = graphicState;
    }

    /**
     * Initializes the internal references to Materials assets.
     *
     * Must be called at least once before the PostProcessor instance is in use. Failure to do so will result
     * in NullPointerExceptions. Calling it additional times shouldn't hurt but shouldn't be necessary either:
     * the asset system refreshes the assets behind the scenes if necessary.
     */
    public void initializeMaterials() {
        // initial renderings
        materials.lightBufferPass = getMaterial("engine:prog.lightBufferPass");

        // pre-post composite
        materials.outline = getMaterial("engine:prog.sobel");
        materials.ssao = getMaterial("engine:prog.ssao");
        materials.ssaoBlurred = getMaterial("engine:prog.ssaoBlur");
        materials.prePostComposite = getMaterial("engine:prog.combine");

        // initial post-processing
        materials.lightShafts = getMaterial("engine:prog.lightshaft");   // TODO: rename shader to lightShafts
        materials.initialPost = getMaterial("engine:prog.prePost");      // TODO: rename shader to scenePrePost
        materials.downSampler = getMaterial("engine:prog.down");         // TODO: rename shader to downSampler
        materials.highPass = getMaterial("engine:prog.highp");           // TODO: rename shader to highPass
        materials.blur = getMaterial("engine:prog.blur");
        materials.toneMapping = getMaterial("engine:prog.hdr");          // TODO: rename shader to toneMapping

        // final post-processing
        materials.ocDistortion = getMaterial("engine:prog.ocDistortion");
        materials.finalPost = getMaterial("engine:prog.post");           // TODO: rename shader to finalPost
        materials.debug = getMaterial("engine:prog.debug");
    }

    private Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    /**
     * Fetches a number of static FBOs from the FrameBuffersManager instance and initializes a number of
     * internal references with them. They are called "static" as they do not change over the lifetime
     * of a PostProcessor instance.
     *
     * This method must to be called at least once for the PostProcessor instance to function, but does
     * not need to be called additional times.
     *
     * Failure to call this method -may- result in a NullPointerException. This is due to the
     * downsampleSceneAndUpdateExposure() method relying on these FBOs. But this method is fully executed
     * only if eye adaptation is enabled: an NPE would be thrown only in that case.
     */
    public void obtainStaticFBOs() {
        buffers.downSampledScene[4] = buffersManager.getFBO("scene16");
        buffers.downSampledScene[3] = buffersManager.getFBO("scene8");
        buffers.downSampledScene[2] = buffersManager.getFBO("scene4");
        buffers.downSampledScene[1] = buffersManager.getFBO("scene2");
        buffers.downSampledScene[0] = buffersManager.getFBO("scene1");
    }

    /**
     * Fetches a number of FBOs from the FrameBuffersManager instance and initializes or refreshes
     * a number of internal references with them. These FBOs may become obsolete over the lifetime
     * of a PostProcessor instance and refreshing the internal references might be needed.
     * These FBOs are therefore referred to as "dynamic" FBOs.
     *
     * This method must be called at least once for the PostProcessor instance to function.
     * Failure to do so will result in NullPointerExceptions. It will then need to be called
     * every time the dynamic FBOs become obsolete and the internal references need to be
     * refreshed with new FBOs.
     */
    public void refreshDynamicFBOs() {
        // initial renderings
        buffers.sceneOpaque         = buffersManager.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = buffersManager.getFBO("sceneOpaquePingPong");

        buffers.sceneSkyBand0   = buffersManager.getFBO("sceneSkyBand0");
        buffers.sceneSkyBand1   = buffersManager.getFBO("sceneSkyBand1");

        buffers.sceneReflectiveRefractive   = buffersManager.getFBO("sceneReflectiveRefractive");
        // sceneReflected, in case one wonders, is not used by the post-processor.

        // pre-post composite
        buffers.outline         = buffersManager.getFBO("outline");
        buffers.ssao            = buffersManager.getFBO("ssao");
        buffers.ssaoBlurred     = buffersManager.getFBO("ssaoBlurred");
        buffers.scenePrePost    = buffersManager.getFBO("scenePrePost");

        // initial post-processing
        buffers.lightShafts     = buffersManager.getFBO("lightShafts");
        buffers.sceneToneMapped = buffersManager.getFBO("sceneToneMapped");

        buffers.sceneHighPass   = buffersManager.getFBO("sceneHighPass");
        buffers.sceneBloom0     = buffersManager.getFBO("sceneBloom0");
        buffers.sceneBloom1     = buffersManager.getFBO("sceneBloom1");
        buffers.sceneBloom2     = buffersManager.getFBO("sceneBloom2");

        buffers.sceneBlur0     = buffersManager.getFBO("sceneBlur0");
        buffers.sceneBlur1     = buffersManager.getFBO("sceneBlur1");

        // final post-processing
        buffers.ocUndistorted   = buffersManager.getFBO("ocUndistorted");
        buffers.sceneFinal      = buffersManager.getFBO("sceneFinal");

        fullScale = buffers.sceneOpaque.dimensions();
    }

    /**
     * In a number of occasions the rendering loop swaps two important FBOs.
     * This method is used to trigger the PostProcessor instance into
     * refreshing the internal references to these FBOs.
     */
    public void refreshSceneOpaqueFBOs() {
        buffers.sceneOpaque         = buffersManager.getFBO("sceneOpaque");
        buffers.sceneOpaquePingPong = buffersManager.getFBO("sceneOpaquePingPong");
    }

    /**
     * Disposes of the PostProcessor instance.
     */
    // Not strictly necessary given the simplicity of the objects being nulled,
    // it is probably a good habit to have a dispose() method. It both properly
    // dispose support objects and it clearly marks the end of a PostProcessor
    // instance's lifecycle.
    public void dispose() {
        buffersManager = null;
        graphicState = null;
        fullScale = null;
    }

    /**
     * Generates SkyBands and stores them into their specific FBOs
     * if inscattering is enabled in the rendering config.
     *
     * SkyBands visually fade the far landscape and its entities into the color of
     * the sky, effectively constituting a form of depth cue.
     */
    public void generateSkyBands() {
        if (renderingConfig.isInscattering()) {
            generateSkyBand(buffers.sceneSkyBand0);
            generateSkyBand(buffers.sceneSkyBand1);
        }
    }

    private void generateSkyBand(FBO skyBand) {
        materials.blur.enable();
        materials.blur.setFloat("radius", 8.0f, true);
        materials.blur.setFloat2("texelSize", 1.0f / skyBand.width(), 1.0f / skyBand.height(), true);

        if (skyBand == buffers.sceneSkyBand0) {
            buffers.sceneOpaque.bindTexture();
        } else {
            buffers.sceneSkyBand0.bindTexture();
        }

        skyBand.bind();
        graphicState.setRenderBufferMask(skyBand, true, false, false);

        setViewportTo(skyBand.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary
    }

    /**
     * Part of the deferred lighting technique, this method applies lighting through screen-space
     * calculations to the previously flat-lit world rendering stored in the primary FBO.   // TODO: rename sceneOpaque* FBOs to primaryA/B
     *
     * See http://en.wikipedia.org/wiki/Deferred_shading as a starting point.
     */
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
        materials.lightBufferPass.setInt("texSceneOpaqueLightBuffer", texId, true);

        buffers.sceneOpaquePingPong.bind();
        graphicState.setRenderBufferMask(buffers.sceneOpaquePingPong, true, true, true);

        setViewportTo(buffers.sceneOpaquePingPong.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary

        buffersManager.swapSceneOpaqueFBOs();
        buffers.sceneOpaque.attachDepthBufferTo(buffers.sceneReflectiveRefractive);
    }

    /**
     * Enabled by the "outline" option in the render settings, this method generates
     * landscape/objects outlines and stores them into a buffer in its own FBO. The
     * stored image is eventually combined with others.
     *
     * The outlines visually separate a given object (including the landscape) or parts of it
     * from sufficiently distant objects it overlaps. It is effectively a depth-based edge
     * detection technique and internally uses a Sobel operator.
     *
     * For further information see: http://en.wikipedia.org/wiki/Sobel_operator
     */
    public void generateOutline() {
        if (renderingConfig.isOutline()) {
            materials.outline.enable();

            // TODO: verify inputs: shouldn't there be a texture binding here?
            buffers.outline.bind();

            setViewportTo(buffers.outline.dimensions());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

            renderFullscreenQuad();

            graphicState.bindDisplay();  // TODO: verify this is necessary
            setViewportToWholeDisplay(); // TODO: verify this is necessary
        }
    }

    /**
     * If Ambient Occlusion is enabled in the render settings, this method generates and
     * stores the necessary images into their own FBOs. The stored images are eventually
     * combined with others.
     *
     * For further information on Ambient Occlusion see: http://en.wikipedia.org/wiki/Ambient_occlusion
     */
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

        // TODO: verify if some textures should be bound here
        buffers.ssao.bind();

        setViewportTo(buffers.ssao.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary
    }

    private void generateBlurredSSAO() {
        materials.ssaoBlurred.enable();
        materials.ssaoBlurred.setFloat2("texelSize", 1.0f / buffers.ssaoBlurred.width(), 1.0f / buffers.ssaoBlurred.height(), true);

        buffers.ssao.bindTexture(); // TODO: verify this is the only input

        buffers.ssaoBlurred.bind();

        setViewportTo(buffers.ssaoBlurred.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary
    }

    /**
     * Adds outlines and ambient occlusion to the rendering obtained so far stored in the primary FBO.
     * Stores the resulting output back into the primary buffer.
     */
    public void generatePrePostComposite() {
        materials.prePostComposite.enable();

        // TODO: verify if there should be bound textures here.
        buffers.sceneOpaquePingPong.bind();

        setViewportTo(buffers.sceneOpaquePingPong.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary

        buffersManager.swapSceneOpaqueFBOs();
        buffers.sceneOpaque.attachDepthBufferTo(buffers.sceneReflectiveRefractive);
    }

    /**
     * Generates light shafts and stores them in their own FBO.
     */
    public void generateLightShafts() {
        if (renderingConfig.isLightShafts()) {
            PerformanceMonitor.startActivity("Rendering light shafts");

            materials.lightShafts.enable();
            // TODO: verify what the inputs are
            buffers.lightShafts.bind();

            setViewportTo(buffers.lightShafts.dimensions());
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

            renderFullscreenQuad();

            graphicState.bindDisplay();     // TODO: verify this is necessary
            setViewportToWholeDisplay();    // TODO: verify this is necessary

            PerformanceMonitor.endActivity();
        }
    }

    /**
     * Adds chromatic aberration, light shafts, 1/8th resolution bloom, vignette onto the rendering achieved so far.
     * Stores the result into its own buffer to be used at a later stage.
     */
    public void initialPostProcessing() {
        PerformanceMonitor.startActivity("Initial Post-Processing");
        materials.initialPost.enable();

        // TODO: verify what the inputs are
        buffers.scenePrePost.bind(); // TODO: see if we could write this straight into sceneOpaque

        setViewportTo(buffers.scenePrePost.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary

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

            // TODO: move this block above, for consistency
            if (i == 4) {
                buffers.scenePrePost.bindTexture();
            } else {
                buffers.downSampledScene[i + 1].bindTexture();
            }

            renderFullscreenQuad();

            graphicState.bindDisplay(); // TODO: probably can be removed or moved out of the loop
        }

        setViewportToWholeDisplay();    // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }

    /**
     * First downsamples the rendering obtained so far, after the initial post processing, into a 1x1 pixel buffer.
     * Then calculate its pixel's luma to update the exposure value. This is used later, during tone mapping.
     */
    // TODO: verify if this can be achieved entirely in the GPU, during tone mapping perhaps?
    public void downsampleSceneAndUpdateExposure() {
        if (renderingConfig.isEyeAdaptation()) {
            PerformanceMonitor.startActivity("Updating exposure");

            downsampleSceneInto1x1pixelsBuffer();

            buffersManager.getCurrentReadbackPBO().copyFromFBO(buffers.downSampledScene[0].fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

            buffersManager.swapReadbackPBOs();

            ByteBuffer pixels = buffersManager.getCurrentReadbackPBO().readBackPixels();

            if (pixels.limit() < 3) {
                logger.error("Failed to auto-update the exposure value.");
                return;
            }

            // TODO: make this line more readable by breaking it in smaller pieces
            currentSceneLuminance = 0.2126f * (pixels.get(2) & 0xFF) / 255.f + 0.7152f * (pixels.get(1) & 0xFF) / 255.f + 0.0722f * (pixels.get(0) & 0xFF) / 255.f;

            float targetExposure = hdrMaxExposure;

            if (currentSceneLuminance > 0) {
                targetExposure = hdrTargetLuminance / currentSceneLuminance;
            }

            float maxExposure = hdrMaxExposure;

            if (CoreRegistry.get(BackdropProvider.class).getDaylight() == 0.0) {    // TODO: fetch the backdropProvider earlier and only once
                maxExposure = hdrMaxExposureNight;
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < hdrMinExposure) {
                targetExposure = hdrMinExposure;
            }

            currentExposure = TeraMath.lerp(currentExposure, targetExposure, hdrExposureAdjustmentSpeed);

        } else {
            if (CoreRegistry.get(BackdropProvider.class).getDaylight() == 0.0) {
                currentExposure = hdrMaxExposureNight;
            } else {
                currentExposure = hdrExposureDefault;
            }
        }
        PerformanceMonitor.endActivity();
    }

    /**
     * // TODO: write javadoc
     */
    // TODO: Tone mapping usually maps colors from HDR to a more limited range,
    // TODO: i.e. the 24 bit a monitor can display. This method however maps from an HDR buffer
    // TODO: to another HDR buffer and this puzzles me. Will need to dig deep in the shader to
    // TODO: see what it does.
    public void generateToneMappedScene() {
        PerformanceMonitor.startActivity("Tone mapping");

        materials.toneMapping.enable();

        buffers.sceneToneMapped.bind();

        setViewportTo(buffers.sceneToneMapped.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);     // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary

        PerformanceMonitor.endActivity();
    }

    /**
     * If bloom is enabled via the rendering settings, this method generates the images needed
     * for the bloom shader effect and stores them in their own frame buffers.
     *
     * This effects renders adds fringes (or "feathers") of light to areas of intense brightness.
     * This in turn give the impression of those areas partially overwhelming the camera or the eye.
     *
     * For more information see: http://en.wikipedia.org/wiki/Bloom_(shader_effect)
     */
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

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        buffers.sceneOpaque.bindTexture();
        materials.highPass.setInt("tex", texId);

//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
//        buffers.sceneOpaque.bindDepthTexture();
//        program.setInt("texDepth", texId++);

        buffers.sceneHighPass.bind();

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

        if (sceneBloom == buffers.sceneBloom0) {
            buffers.sceneHighPass.bindTexture();
        } else if (sceneBloom == buffers.sceneBloom1) {
            buffers.sceneBloom0.bindTexture();
        } else {
            buffers.sceneBloom1.bindTexture();
        }

        sceneBloom.bind();

        setViewportTo(sceneBloom.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // TODO: verify this is necessary

        renderFullscreenQuad();

        graphicState.bindDisplay();     // TODO: verify this is necessary
        setViewportToWholeDisplay();    // TODO: verify this is necessary
    }

    /**
     * If blur is enabled through the rendering settings, this method generates the images used
     * by the Blur effect when underwater and for the Depth of Field effect when above water.
     *
     * For more information on blur: http://en.wikipedia.org/wiki/Defocus_aberration
     * For more information on DoF: http://en.wikipedia.org/wiki/Depth_of_field
     */
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

        if (sceneBlur == buffers.sceneBlur0) {
            buffers.sceneToneMapped.bindTexture();
        } else {
            buffers.sceneBlur0.bindTexture();
        }

        sceneBlur.bind();

        setViewportTo(sceneBlur.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        graphicState.bindDisplay();
        setViewportToWholeDisplay();
    }

    // Final Post-Processing: depth-of-field blur, motion blur, film grain, grading, OculusVR distortion

    /**
     * If each is enabled through the rendering settings, this method
     * adds depth-of-field blur, motion blur and film grain to the rendering
     * obtained so far. If OculusVR support is enabled, it composes (over two
     * calls) the images for each eye into a single image, and applies a distortion
     * pattern to each, to match the optics in the OculusVR headset.
     *
     * Finally, it either sends the image to the display or, when taking a screenshot,
     * instructs the FrameBuffersManager to save it to a file.
     *
     * @param renderingStage Can be MONO, LEFT_EYE or RIGHT_EYE, and communicates to the method weather
     *                       it is dealing with a standard display or an OculusVR setup, and in the
     *                       latter case, which eye is currently being rendered. Notice that if the
     *                       OculusVR support is enabled, the image is sent to screen or saved to
     *                       file only when the value passed in is RIGHT_EYE, as the processing for
     *                       the LEFT_EYE comes first and leads to an incomplete image.
     */
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

        if (isNotTakingScreenshot()) {
            graphicState.bindDisplay();
            renderFullscreenQuad(0, 0, Display.getWidth(), Display.getHeight());

        } else {
            buffers.sceneFinal.bind();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());

            saveScreenshot();
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
        if (isNotTakingScreenshot()) {
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

                if (isNotTakingScreenshot()) {
                    graphicState.bindDisplay();
                    applyOculusDistortion(buffers.sceneFinal);

                } else {
                    buffers.sceneFinal.bind();
                    applyOculusDistortion(buffers.ocUndistorted);
                    saveScreenshot();
                    // when saving a screenshot we do NOT send the image to screen,
                    // to avoid the brief flicker of the screenshot for one frame
                }

                break;
            case MONO:
                break;
        }
    }

    private void applyOculusDistortion(FBO inputBuffer) {
        materials.ocDistortion.enable();

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        inputBuffer.bindTexture();
        materials.ocDistortion.setInt("texInputBuffer", texId, true);

        if (isNotTakingScreenshot()) {
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

    /**
     * Renders a quad filling the whole currently set viewport.
     */
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

    /**
     * First sets a viewport and then renders a quad filling it.
     *
     * @param x an integer representing the x coordinate (in pixels) of the origin of the viewport.
     * @param y an integer representing the y coordinate (in pixels) of the origin of the viewport.
     * @param viewportWidth an integer representing the width (in pixels) the viewport.
     * @param viewportHeight an integer representing the height (in pixels) the viewport.
     */
    // TODO: perhaps remove this method and make sure the viewport is set explicitely.
    public void renderFullscreenQuad(int x, int y, int viewportWidth, int viewportHeight) {
        glViewport(x, y, viewportWidth, viewportHeight);
        renderFullscreenQuad();
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

    /**
     * Returns the current exposure value (set in downsampleSceneAndUpdateExposure()).
     *
     * @return a float representing the current exposure value.
     */
    public float getExposure() {
        return currentExposure;
    }

    /**
     * Triggers a screenshot.
     *
     * Notice that this method just starts the process: screenshot data is captured and written to file
     * as soon as possible but not necessarily immediately after the trigger.
     */
    public void takeScreenshot() {
        isTakingScreenshot = true;
    }

    /**
     * Schedules the saving of screenshot data to file.
     *
     * Screenshot data from the GPU is obtained as soon as this method executes. However, the data is only scheduled
     * to be written to file, by submitting a task to the ThreadManager. The task is then executed as soon as possible
     * but not necessarily immediately.
     *
     * The file is then saved in the designated screenshot folder with a filename in the form:
     *
     *     Terasology-[yyMMddHHmmss]-[width]x[height].[format]
     *
     * If no screenshot data is available an error is logged and the method returns doing nothing.
     */
    public void saveScreenshot() {
        final ByteBuffer buffer = buffersManager.getSceneFinalRawData();
        if(buffer == null) {
            logger.error("No screenshot data available. No screenshot will be saved.");
            return;
        }

        int width = buffers.sceneFinal.width();
        int height = buffers.sceneFinal.height();

        Runnable task = () -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

            final String format = renderingConfig.getScreenshotFormat();
            final String fileName = "Terasology-" + sdf.format(new Date()) + "-" + width + "x" + height + "." + format;
            Path path = PathManager.getInstance().getScreenshotPath().resolve(fileName);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int i = (x + width * y) * 4;
                    int r = buffer.get(i) & 0xFF;
                    int g = buffer.get(i + 1) & 0xFF;
                    int b = buffer.get(i + 2) & 0xFF;
                    image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }

            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                ImageIO.write(image, format, out);
                logger.info("Screenshot '" + fileName + "' saved! ");
            } catch (IOException e) {
                logger.warn("Failed to save screenshot!", e);
            }
        };

        threadManager.submitTask("Write screenshot", task);
        isTakingScreenshot = false;
    }

    /**
     * Returns true if the rendering engine is not in the process of taking a screenshot.
     * Returns false if a screenshot is being taken.
     *
     * @return true if no screenshot is being taken, false otherwise
     */
    // for code readability it make sense to have this method rather than its opposite.
    public boolean isNotTakingScreenshot() {
        return !isTakingScreenshot;
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
        public FBO scenePrePost;

        // initial post-processing
        public FBO lightShafts;

        public FBO[] downSampledScene = new FBO[5];

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
