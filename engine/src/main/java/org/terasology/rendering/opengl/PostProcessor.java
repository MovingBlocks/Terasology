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

import static org.lwjgl.opengl.EXTFramebufferObject.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * TODO: write javadoc
 */
public class PostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PostProcessor.class);

    /* PROPERTIES */
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

    /* HDR */
    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;

    private int displayListQuad = -1;
    private FBO.Dimensions fullScale;

    private LwjglRenderingProcess renderingProcess;
    private GraphicState graphicState;
    private Materials materials = new Materials();

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
        materials.sobel    = Assets.getMaterial("engine:prog.sobel");       // TODO: rename shader to outline
        materials.ssao     = Assets.getMaterial("engine:prog.ssao");
        materials.ssaoBlur = Assets.getMaterial("engine:prog.ssaoBlur");
        materials.combine  = Assets.getMaterial("engine:prog.combine");     // TODO: rename to prePostComposite

        // initial post-processing
        materials.lightShafts = Assets.getMaterial("engine:prog.lightshaft");   // TODO: rename shaders to lightShafts
        materials.downSampler = Assets.getMaterial("engine:prog.down");     // TODO: rename to downSampler
        materials.highPass    = Assets.getMaterial("engine:prog.highp");    // TODO: rename to highPass
        materials.blur        = Assets.getMaterial("engine:prog.blur");
        materials.hdr         = Assets.getMaterial("engine:prog.hdr");      // TODO: rename shader to toneMapping
        materials.prePost     = Assets.getMaterial("engine:prog.prePost");  // TODO: rename shader to initialPost

        // final post-processing
        materials.ocDistortion = Assets.getMaterial("engine:prog.ocDistortion");
        materials.post         = Assets.getMaterial("engine:prog.post");    // TODO: rename shader to finalPost
        materials.debug        = Assets.getMaterial("engine:prog.debug");
    }

    public void generateSkyBand(int id) {
        FBO skyBand = renderingProcess.getFBO("sceneSkyBand" + id);

        if (skyBand == null) {
            return;
        }

        skyBand.bind();
        graphicState.setRenderBufferMask(skyBand, true, false, false);

        materials.blur.enable();
        materials.blur.setFloat("radius", 8.0f, true);
        materials.blur.setFloat2("texelSize", 1.0f / skyBand.width(), 1.0f / skyBand.height(), true);

        if (id == 0) {
            renderingProcess.bindFboTexture("sceneOpaque");
        } else {
            renderingProcess.bindFboTexture("sceneSkyBand" + (id - 1));
        }

        setViewportTo(skyBand.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        skyBand.unbind();
        setViewportToFullSize();
    }

    public void applyLightBufferPass(String target) {
        materials.lightBufferPass.enable();

        FBO targetFbo = renderingProcess.getFBO(target);

        int texId = 0;
        if (targetFbo != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindTexture();
            materials.lightBufferPass.setInt("texSceneOpaque", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindDepthTexture();
            materials.lightBufferPass.setInt("texSceneOpaqueDepth", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindNormalsTexture();
            materials.lightBufferPass.setInt("texSceneOpaqueNormals", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindLightBufferTexture();
            materials.lightBufferPass.setInt("texSceneOpaqueLightBuffer", texId++, true);
        }

        FBO targetPingPong = renderingProcess.getFBO(target + "PingPong");
        targetPingPong.bind();
        graphicState.setRenderBufferMask(targetPingPong, true, true, true);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        renderingProcess.unbindFbo(target + "PingPong");

        renderingProcess.flipPingPongFbo(target);

        if (target.equals("sceneOpaque")) {
            attachDepthBufferToFbo("sceneOpaque", "sceneReflectiveRefractive");
        }
    }

    public void renderPreCombinedScene() {
        renderingProcess.createOrUpdateFullscreenFbos();

        if (renderingConfig.isOutline()) {
            generateSobel();
        }

        if (renderingConfig.isSsao()) {
            generateSSAO();
            generateBlurredSSAO();
        }

        generateCombinedScene();
    }

    private void generateSobel() {
        materials.sobel.enable();

        FBO sobel = renderingProcess.getFBO("sobel");

        if (sobel == null) {
            return;
        }

        sobel.bind();

        setViewportTo(sobel.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        sobel.unbind();
        setViewportToFullSize();
    }

    private void generateSSAO() {
        materials.ssao.enable();

        FBO ssao = renderingProcess.getFBO("ssao");

        if (ssao == null) {
            return;
        }

        materials.ssao.setFloat2("texelSize", 1.0f / ssao.width(), 1.0f / ssao.height(), true);
        materials.ssao.setFloat2("noiseTexelSize", 1.0f / 4.0f, 1.0f / 4.0f, true);

        ssao.bind();

        setViewportTo(ssao.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        ssao.unbind();
        setViewportToFullSize();
    }

    private void generateBlurredSSAO() {
        materials.ssaoBlur.enable();

        FBO ssao = renderingProcess.getFBO("ssaoBlurred");

        if (ssao == null) {
            return;
        }

        materials.ssaoBlur.setFloat2("texelSize", 1.0f / ssao.width(), 1.0f / ssao.height(), true);
        ssao.bind();

        setViewportTo(ssao.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderingProcess.bindFboTexture("ssao");

        renderFullscreenQuad();

        ssao.unbind();
        setViewportToFullSize();
    }

    private void generateCombinedScene() {
        materials.combine.enable();

        renderingProcess.bindFbo("sceneOpaquePingPong");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        renderingProcess.unbindFbo("sceneOpaquePingPong");

        renderingProcess.flipPingPongFbo("sceneOpaque");
        attachDepthBufferToFbo("sceneOpaque", "sceneReflectiveRefractive");
    }

    public void renderPost(WorldRenderer.WorldRenderingStage worldRenderingStage) {
        if (renderingConfig.isLightShafts()) {
            PerformanceMonitor.startActivity("Rendering light shafts");
            generateLightShafts();
            PerformanceMonitor.endActivity();
        }

        PerformanceMonitor.startActivity("Pre-post processing");
        generatePrePost();
        PerformanceMonitor.endActivity();

        if (renderingConfig.isEyeAdaptation()) {
            PerformanceMonitor.startActivity("Rendering eye adaption");
            generateDownsampledScene();
            PerformanceMonitor.endActivity();
        }

        PerformanceMonitor.startActivity("Updating exposure");
        updateExposure();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Tone mapping");
        generateToneMappedScene();
        PerformanceMonitor.endActivity();

        if (renderingConfig.isBloom()) {
            PerformanceMonitor.startActivity("Applying bloom");
            generateHighPass();
            for (int i = 0; i < 3; i++) {
                generateBloom(i);
            }
            PerformanceMonitor.endActivity();
        }

        PerformanceMonitor.startActivity("Applying blur");
        for (int i = 0; i < 2; i++) {
            if (renderingConfig.getBlurIntensity() != 0) {
                generateBlur(i);
            }
        }
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Rendering final scene");
        if (worldRenderingStage == WorldRenderer.WorldRenderingStage.LEFT_EYE
                || worldRenderingStage == WorldRenderer.WorldRenderingStage.RIGHT_EYE
                || (worldRenderingStage == WorldRenderer.WorldRenderingStage.MONO && renderingProcess.isTakingScreenshot())) {

            renderFinalSceneToRT(worldRenderingStage);

            if (renderingProcess.isTakingScreenshot()) {
                renderingProcess.saveScreenshot();
            }
        }

        if (worldRenderingStage == WorldRenderer.WorldRenderingStage.MONO
                || worldRenderingStage == WorldRenderer.WorldRenderingStage.RIGHT_EYE) {
            renderFinalScene();
        }
        PerformanceMonitor.endActivity();
    }

    private void generateLightShafts() {
        materials.lightShafts.enable();

        FBO lightshaft = renderingProcess.getFBO("lightShafts");

        if (lightshaft == null) {
            return;
        }

        lightshaft.bind();

        setViewportTo(lightshaft.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        lightshaft.unbind();
        setViewportToFullSize();
    }

    private void generatePrePost() {
        materials.prePost.enable();

        renderingProcess.bindFbo("scenePrePost");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        renderingProcess.unbindFbo("scenePrePost");
    }

    private void generateDownsampledScene() {
        materials.downSampler.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = TeraMath.pow(2, i + 1);

            int size = TeraMath.pow(2, i);
            materials.downSampler.setFloat("size", size, true);

            renderingProcess.bindFbo("scene" + size);
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 4) {
                renderingProcess.bindFboTexture("scenePrePost");
            } else {
                renderingProcess.bindFboTexture("scene" + sizePrev);
            }

            renderFullscreenQuad();

            renderingProcess.unbindFbo("scene" + size);
        }

        setViewportToFullSize();
    }

    private void updateExposure() {
        if (renderingConfig.isEyeAdaptation()) {
            FBO scene = renderingProcess.getFBO("scene1");

            if (scene == null) {
                return;
            }

            renderingProcess.getCurrentReadbackPBO().copyFromFBO(scene.fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

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
    }

    private void generateToneMappedScene() {
        materials.hdr.enable();

        renderingProcess.bindFbo("sceneToneMapped");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        renderingProcess.unbindFbo("sceneToneMapped");
    }

    private void generateHighPass() {
        materials.highPass.enable();
        materials.highPass.setFloat("highPassThreshold", bloomHighPassThreshold, true);

        FBO highPass = renderingProcess.getFBO("sceneHighPass");

        if (highPass == null) {
            return;
        }

        highPass.bind();

        FBO sceneOpaque = renderingProcess.getFBO("sceneOpaque");

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindTexture();
        materials.highPass.setInt("tex", texId++);

//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
//        sceneOpaque.bindDepthTexture();
//        program.setInt("texDepth", texId++);

        setViewportTo(highPass.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        highPass.unbind();
        setViewportToFullSize();
    }

    private void generateBloom(int id) {
        materials.blur.enable();
        materials.blur.setFloat("radius", bloomBlurRadius, true);

        FBO bloom = renderingProcess.getFBO("sceneBloom" + id);

        if (bloom == null) {
            return;
        }

        materials.blur.setFloat2("texelSize", 1.0f / bloom.width(), 1.0f / bloom.height(), true);

        bloom.bind();

        setViewportTo(bloom.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            renderingProcess.getFBO("sceneHighPass").bindTexture();
        } else {
            renderingProcess.getFBO("sceneBloom" + (id - 1)).bindTexture();
        }

        renderFullscreenQuad();

        bloom.unbind();
        setViewportToFullSize();
    }

    private void generateBlur(int id) {
        materials.blur.enable();
        materials.blur.setFloat("radius", overallBlurRadiusFactor * renderingConfig.getBlurRadius(), true);

        FBO blur = renderingProcess.getFBO("sceneBlur" + id);

        if (blur == null) {
            return;
        }

        materials.blur.setFloat2("texelSize", 1.0f / blur.width(), 1.0f / blur.height(), true);

        blur.bind();

        setViewportTo(blur.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            renderingProcess.bindFboTexture("sceneToneMapped");
        } else {
            renderingProcess.bindFboTexture("sceneBlur" + (id - 1));
        }

        renderFullscreenQuad();

        blur.unbind();

        setViewportToFullSize();
    }

    private void renderFinalSceneToRT(WorldRenderer.WorldRenderingStage renderingStage) {
        Material material;

        if (renderingDebugConfig.isEnabled()) {
            material = materials.debug;
        } else {
            material = materials.post;
        }

        material.enable();

        renderingProcess.bindFbo("sceneFinal");

        if (renderingStage == WorldRenderer.WorldRenderingStage.MONO || renderingStage == WorldRenderer.WorldRenderingStage.LEFT_EYE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        switch (renderingStage) {
            case MONO:
                renderFullscreenQuad(0, 0, fullScale.width(), fullScale.height());
                break;
            case LEFT_EYE:
                renderFullscreenQuad(0, 0, fullScale.width() / 2, fullScale.height());
                break;
            case RIGHT_EYE:
                renderFullscreenQuad(fullScale.width() / 2, 0, fullScale.width() / 2, fullScale.height());
                break;
        }

        renderingProcess.unbindFbo("sceneFinal");
    }

    private void updateOcShaderParametersForVP(Material program, int vpX, int vpY, int vpWidth, int vpHeight, WorldRenderer.WorldRenderingStage renderingStage) {
        float w = (float) vpWidth / fullScale.width();
        float h = (float) vpHeight / fullScale.height();
        float x = (float) vpX / fullScale.width();
        float y = (float) vpY / fullScale.height();

        float as = (float) vpWidth / vpHeight;

        program.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1],
                OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3], true);

        float ocLensCenter = (renderingStage == WorldRenderer.WorldRenderingStage.RIGHT_EYE) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        program.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f, true);
        program.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f, true);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        program.setFloat2("ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as, true);
        program.setFloat2("ocScaleIn", (2 / w), (2 / h) / as, true);
    }

    private void renderFinalScene() {

        Material material;

        if (renderingConfig.isOculusVrSupport()) {
            material = materials.ocDistortion;
            material.enable();

            updateOcShaderParametersForVP(material, 0, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.LEFT_EYE);
        } else {
            if (renderingDebugConfig.isEnabled()) {
                material = materials.debug;
            } else {
                material = materials.post;
            }

            material.enable();
        }

        renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), org.lwjgl.opengl.Display.getHeight());

        if (renderingConfig.isOculusVrSupport()) {
            updateOcShaderParametersForVP(material, fullScale.width() / 2, 0, fullScale.width() / 2, fullScale.height(), WorldRenderer.WorldRenderingStage.RIGHT_EYE);

            renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), Display.getHeight());
        }
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

    public boolean attachDepthBufferToFbo(String sourceFboName, String targetFboName) {
        FBO source = renderingProcess.getFBO(sourceFboName);
        FBO target = renderingProcess.getFBO(targetFboName);

        if (source == null || target == null) {
            return false;
        }

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, target.fboId);

        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, source.depthStencilRboId);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, source.depthStencilTextureId, 0);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        return true;
    }

    private void setViewportToFullSize() {
        glViewport(0, 0, fullScale.width(), fullScale.height());
    }

    private void setViewportTo(FBO.Dimensions dimensions) {
        glViewport(0, 0, dimensions.width(), dimensions.height());
    }

    public float getExposure() {
        return currentExposure;
    }

    public void setFullScale(FBO.Dimensions newFullScale) {
        this.fullScale = newFullScale;
    }

    private class Materials {
        // initial renderings
        public Material lightBufferPass;

        // pre-post composite
        public Material sobel;
        public Material ssao;
        public Material ssaoBlur;
        public Material combine;
        public Material prePost;

        // initial post-processing
        public Material lightShafts;
        public Material downSampler;
        public Material highPass;
        public Material blur;
        public Material hdr;

        // final post-processing
        public Material ocDistortion;
        public Material post;
        public Material debug;
    }
}
