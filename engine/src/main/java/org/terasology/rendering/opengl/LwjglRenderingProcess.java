/*
 * Copyright 2013 MovingBlocks
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

import com.google.common.collect.Maps;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.editor.EditorRange;
import org.terasology.engine.GameEngine;
import org.terasology.engine.paths.PathManager;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.world.WorldRenderer.WorldRenderingStage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_RENDERBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteFramebuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glDeleteRenderbuffersEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferRenderbufferEXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glFramebufferTexture2DEXT;
import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DECR;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_FRONT_AND_BACK;
import static org.lwjgl.opengl.GL11.GL_INCR;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_NOTEQUAL;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_STENCIL_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_STENCIL_TEST;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glCullFace;
import static org.lwjgl.opengl.GL11.glDepthMask;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3i;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.glStencilOpSeparate;

/**
 * The Default Rendering Process class.
 *
 * @author Benjamin Glatzel
 */
public class LwjglRenderingProcess {

    private static final Logger logger = LoggerFactory.getLogger(LwjglRenderingProcess.class);

    private static LwjglRenderingProcess instance;

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
    private PBO readBackPBOFront;
    private PBO readBackPBOBack;
    private PBO readBackPBOCurrent;

    /* RTs */
    private int rtFullWidth;
    private int rtFullHeight;
    private int rtWidth2;
    private int rtHeight2;
    private int rtWidth4;
    private int rtHeight4;
    private int rtWidth8;
    private int rtHeight8;
    private int rtWidth16;
    private int rtHeight16;
    private int rtWidth32;
    private int rtHeight32;

    private int overwriteRtWidth;
    private int overwriteRtHeight;

    private String currentlyBoundFboName = "";
    private FBO currentlyBoundFbo;
    //private int currentlyBoundTextureId = -1;

    /* VARIOUS */
    private boolean takeScreenshot;
    private int displayListQuad = -1;

    // Note: this assumes that the settings in the configs might change at runtime,
    // but the config objects will not. At some point this might change, i.e. implementing presets.
    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();
    private RenderingDebugConfig renderingDebugConfig = renderingConfig.getDebug();

    private Map<String, FBO> fboLookup = Maps.newHashMap();

    public LwjglRenderingProcess() {
        initialize();
    }

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
    public static LwjglRenderingProcess getInstance() {
        if (instance == null) {
            instance = new LwjglRenderingProcess();
        }

        return instance;
    }

    public void initialize() {
        createOrUpdateFullscreenFbos();

        // Note: the FBObuilder takes care of registering thew new FBOs on fboLookup.
        new FBObuilder("scene16", 16, 16, FBO.Type.DEFAULT).build();
        new FBObuilder("scene8",   8,  8, FBO.Type.DEFAULT).build();
        new FBObuilder("scene4",   4,  4, FBO.Type.DEFAULT).build();
        new FBObuilder("scene2",   2,  2, FBO.Type.DEFAULT).build();
        new FBObuilder("scene1",   1,  1, FBO.Type.DEFAULT).build();

        readBackPBOFront = new PBO(1, 1);
        readBackPBOBack = new PBO(1, 1);
        readBackPBOCurrent = readBackPBOFront;
    }

    /**
     * Creates the scene FBOs and updates them according to the size of the viewport. The current size
     * provided by the display class is only used if the parameters overwriteRTWidth and overwriteRTHeight are set
     * to zero.
     */
    private void createOrUpdateFullscreenFbos() {
        rtFullWidth = overwriteRtWidth;
        rtFullHeight = overwriteRtHeight;

        if (overwriteRtWidth == 0) {
            rtFullWidth = org.lwjgl.opengl.Display.getWidth();
        }

        if (overwriteRtHeight == 0) {
            rtFullHeight = org.lwjgl.opengl.Display.getHeight();
        }

        rtFullWidth *= renderingConfig.getFboScale() / 100f;
        rtFullHeight *= renderingConfig.getFboScale() / 100f;

        if (renderingConfig.isOculusVrSupport()) {
            if (overwriteRtWidth == 0) {
                rtFullWidth *= OculusVrHelper.getScaleFactor();
            }
            if (overwriteRtHeight == 0) {
                rtFullHeight *= OculusVrHelper.getScaleFactor();
            }
        }

        rtWidth2 = rtFullWidth / 2;
        rtHeight2 = rtFullHeight / 2;
        rtWidth4 = rtWidth2 / 2;
        rtHeight4 = rtHeight2 / 2;
        rtWidth8 = rtWidth4 / 2;
        rtHeight8 = rtHeight4 / 2;
        rtWidth16 = rtHeight8 / 2;
        rtHeight16 = rtWidth8 / 2;
        rtWidth32 = rtHeight16 / 2;
        rtHeight32 = rtWidth16 / 2;

        FBO scene = fboLookup.get("sceneOpaque");
        final boolean recreate = scene == null || (scene.width() != rtFullWidth || scene.height() != rtFullHeight);

        if (!recreate) {
            return;
        }

        // Note: the FBObuilder takes care of registering thew new FBOs on fboLookup.
        int shadowMapResolution = renderingConfig.getShadowMapResolution();
        new FBObuilder("sceneShadowMap", shadowMapResolution, shadowMapResolution, FBO.Type.NO_COLOR).useDepthBuffer().build();

        // buffers for the initial renderings
        FBO sceneOpaque =
                new FBObuilder("sceneOpaque", rtFullWidth, rtFullHeight, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();
        new FBObuilder("sceneOpaquePingPong", rtFullWidth, rtFullHeight, FBO.Type.HDR).useDepthBuffer().useNormalBuffer().useLightBuffer().useStencilBuffer().build();

        new FBObuilder("sceneSkyBand0",   rtWidth16, rtHeight16, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneSkyBand1",   rtWidth32, rtHeight32, FBO.Type.DEFAULT).build();

        FBO sceneReflectiveRefractive = new FBObuilder("sceneReflectiveRefractive", rtFullWidth, rtFullHeight, FBO.Type.HDR).useNormalBuffer().build();
        sceneOpaque.attachDepthBufferTo(sceneReflectiveRefractive);

        new FBObuilder("sceneReflected",  rtWidth2, rtHeight2, FBO.Type.DEFAULT).useDepthBuffer().build();

        // buffers for the prePost-Processing composite
        new FBObuilder("sobel",           rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
        new FBObuilder("ssao",            rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
        new FBObuilder("ssaoBlurred",     rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
        new FBObuilder("scenePrePost",    rtFullWidth, rtFullHeight, FBO.Type.HDR).build();

        // buffers for the Initial Post-Processing
        new FBObuilder("lightShafts",     rtWidth2,    rtHeight2,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneToneMapped", rtFullWidth, rtFullHeight, FBO.Type.HDR).build();

        new FBObuilder("sceneHighPass",   rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom0",     rtWidth2,    rtHeight2,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom1",     rtWidth4,    rtHeight4,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBloom2",     rtWidth8,    rtHeight8,    FBO.Type.DEFAULT).build();

        new FBObuilder("sceneBlur0",      rtWidth2,    rtHeight2,    FBO.Type.DEFAULT).build();
        new FBObuilder("sceneBlur1",      rtWidth2,    rtHeight2,    FBO.Type.DEFAULT).build();

        // buffers for the Final Post-Processing
        new FBObuilder("ocUndistorted",   rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
        new FBObuilder("sceneFinal",      rtFullWidth, rtFullHeight, FBO.Type.DEFAULT).build();
    }

    public void deleteFBO(String title) {
        if (fboLookup.containsKey(title)) {
            FBO fbo = fboLookup.get(title);

            glDeleteFramebuffersEXT(fbo.fboId);
            glDeleteRenderbuffersEXT(fbo.depthStencilRboId);
            GL11.glDeleteTextures(fbo.normalsBufferTextureId);
            GL11.glDeleteTextures(fbo.depthStencilTextureId);
            GL11.glDeleteTextures(fbo.colorBufferTextureId);
        }
    }

    public boolean attachDepthBufferToFbo(String sourceFboName, String targetFboName) {
        FBO source = getFBO(sourceFboName);
        FBO target = getFBO(targetFboName);

        if (source == null || target == null) {
            return false;
        }

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, target.fboId);

        glFramebufferRenderbufferEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL_RENDERBUFFER_EXT, source.depthStencilRboId);
        glFramebufferTexture2DEXT(GL_FRAMEBUFFER_EXT, GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, source.depthStencilTextureId, 0);

        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);

        return true;
    }

    private void updateExposure() {
        if (renderingConfig.isEyeAdaptation()) {
            FBO scene = getFBO("scene1");

            if (scene == null) {
                return;
            }

            readBackPBOCurrent.copyFromFBO(scene.fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

            if (readBackPBOCurrent == readBackPBOFront) {
                readBackPBOCurrent = readBackPBOBack;
            } else {
                readBackPBOCurrent = readBackPBOFront;
            }

            ByteBuffer pixels = readBackPBOCurrent.readBackPixels();

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

    public void clear() {
        bindFbo("sceneOpaque");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        unbindFbo("sceneOpaque");
        bindFbo("sceneReflectiveRefractive");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        unbindFbo("sceneReflectiveRefractive");
    }

    public void beginRenderSceneOpaque() {
        bindFbo("sceneOpaque");
        setRenderBufferMask(true, true, true);
    }

    public void endRenderSceneOpaque() {
        setRenderBufferMask(true, true, true);
        unbindFbo("sceneOpaque");
    }

    public void beginRenderSimpleBlendMaterialsIntoCombinedPass() {
        beginRenderSceneOpaque();
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
    }

    public void endRenderSimpleBlendMaterialsIntoCombinedPass() {
        GL11.glDisable(GL_BLEND);
        GL11.glDepthMask(true);
        endRenderSceneOpaque();
    }

    public void beginRenderLightGeometryStencilPass() {
        bindFbo("sceneOpaque");
        setRenderBufferMask(false, false, false);
        glDepthMask(false);

        glClear(GL_STENCIL_BUFFER_BIT);

        glCullFace(GL_FRONT);
        glDisable(GL_CULL_FACE);

        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 0, 0);

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR, GL_KEEP);
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR, GL_KEEP);
    }

    public void endRenderLightGeometryStencilPass() {
        setRenderBufferMask(true, true, true);
        unbindFbo("sceneOpaque");
    }

    public void beginRenderLightGeometry() {
        bindFbo("sceneOpaque");

        // Only write to the light buffer
        setRenderBufferMask(false, false, true);

        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);

        glDepthMask(true);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
    }

    public void endRenderLightGeometry() {
        glDisable(GL_STENCIL_TEST);
        glCullFace(GL_BACK);

        unbindFbo("sceneOpaque");
    }

    public void beginRenderDirectionalLights() {
        bindFbo("sceneOpaque");
    }

    public void endRenderDirectionalLights() {
        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_DEPTH_TEST);

        setRenderBufferMask(true, true, true);
        unbindFbo("sceneOpaque");

        applyLightBufferPass("sceneOpaque");
    }

    public void preChunkRenderSetup(Vector3f chunkPositionRelativeToCamera) {
        GL11.glPushMatrix();
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);
    }

    public void postChunkRenderCleanup() {
        GL11.glPopMatrix();
    }

    public void enableWireframeIf(boolean isWireframeEnabledInRenderingDebugConfig) {
        if (isWireframeEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    public void disableWireframeIf(boolean isWireframeEnabledInRenderingDebugConfig) {
        if (isWireframeEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    public void setRenderBufferMask(boolean color, boolean normal, boolean lightBuffer) {
        setRenderBufferMask(currentlyBoundFboName, color, normal, lightBuffer);
    }

    public void setRenderBufferMask(String fboTitle, boolean color, boolean normal, boolean lightBuffer) {
        setRenderBufferMask(getFBO(fboTitle), color, normal, lightBuffer);
    }

    public void setRenderBufferMask(FBO fbo, boolean color, boolean normal, boolean lightBuffer) {
        if (fbo == null) {
            return;
        }

        int attachmentId = 0;

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);

        if (fbo.colorBufferTextureId != 0) {
            if (color) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.normalsBufferTextureId != 0) {
            if (normal) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.lightBufferTextureId != 0) {
            if (lightBuffer) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    public void beginRenderSceneReflectiveRefractive(boolean isHeadUnderWater) {
        bindFbo("sceneReflectiveRefractive");

        // Make sure the water surface is rendered if the player is underwater.
        if (isHeadUnderWater) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public void endRenderSceneReflectiveRefractive(boolean isHeadUnderWater) {
        if (isHeadUnderWater) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        unbindFbo("sceneReflectiveRefractive");
    }

    public void beginRenderReflectedScene() {
        FBO reflected = getFBO("sceneReflected");

        if (reflected == null) {
            return;
        }

        reflected.bind();

        glViewport(0, 0, reflected.width(), reflected.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glCullFace(GL11.GL_FRONT);
    }

    public void endRenderReflectedScene() {
        GL11.glCullFace(GL11.GL_BACK);
        unbindFbo("sceneReflected");
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneShadowMap() {
        FBO shadowMap = getFBO("sceneShadowMap");

        if (shadowMap == null) {
            return;
        }

        shadowMap.bind();

        glViewport(0, 0, shadowMap.width(), shadowMap.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL_CULL_FACE);
    }

    public void endRenderSceneShadowMap() {
        GL11.glEnable(GL_CULL_FACE);
        unbindFbo("sceneShadowMap");
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneSky() {
        setRenderBufferMask(true, false, false);
    }

    public void endRenderSceneSky() {
        setRenderBufferMask(true, true, true);

        if (renderingConfig.isInscattering()) {
            generateSkyBand(0);
            generateSkyBand(1);
        }

        bindFbo("sceneOpaque");
    }

    public void beginRenderFirstPerson() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
    }

    public void endRenderFirstPerson() {
        GL11.glDepthFunc(GL_LEQUAL);
        GL11.glPopMatrix();
    }

    public void renderPreCombinedScene() {
        createOrUpdateFullscreenFbos();

        if (renderingConfig.isOutline()) {
            generateSobel();
        }

        if (renderingConfig.isSsao()) {
            generateSSAO();
            generateBlurredSSAO();
        }

        generateCombinedScene();
    }

    public void renderPost(WorldRenderingStage worldRenderingStage) {
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
        if (worldRenderingStage == WorldRenderingStage.LEFT_EYE
                || worldRenderingStage == WorldRenderingStage.RIGHT_EYE
                || (worldRenderingStage == WorldRenderingStage.MONO && takeScreenshot)) {

            renderFinalSceneToRT(worldRenderingStage);

            if (takeScreenshot) {
                saveScreenshot();
            }
        }

        if (worldRenderingStage == WorldRenderingStage.MONO
                || worldRenderingStage == WorldRenderingStage.RIGHT_EYE) {
            renderFinalScene();
        }
        PerformanceMonitor.endActivity();
    }

    private void renderFinalSceneToRT(WorldRenderingStage renderingStage) {
        Material material;

        if (renderingDebugConfig.isEnabled()) {
            material = Assets.getMaterial("engine:prog.debug");
        } else {
            material = Assets.getMaterial("engine:prog.post");
        }

        material.enable();

        bindFbo("sceneFinal");

        if (renderingStage == WorldRenderingStage.MONO || renderingStage == WorldRenderingStage.LEFT_EYE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        switch (renderingStage) {
            case MONO:
                renderFullscreenQuad(0, 0, rtFullWidth, rtFullHeight);
                break;
            case LEFT_EYE:
                renderFullscreenQuad(0, 0, rtFullWidth / 2, rtFullHeight);
                break;
            case RIGHT_EYE:
                renderFullscreenQuad(rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight);
                break;
        }

        unbindFbo("sceneFinal");
    }

    private void updateOcShaderParametersForVP(Material program, int vpX, int vpY, int vpWidth, int vpHeight, WorldRenderingStage renderingStage) {
        float w = (float) vpWidth / rtFullWidth;
        float h = (float) vpHeight / rtFullHeight;
        float x = (float) vpX / rtFullWidth;
        float y = (float) vpY / rtFullHeight;

        float as = (float) vpWidth / vpHeight;

        program.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1],
                OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3], true);

        float ocLensCenter = (renderingStage == WorldRenderingStage.RIGHT_EYE) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        program.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f, true);
        program.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f, true);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        program.setFloat2("ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as, true);
        program.setFloat2("ocScaleIn", (2 / w), (2 / h) / as, true);
    }

    private void renderFinalScene() {

        Material material;

        if (renderingConfig.isOculusVrSupport()) {
            material = Assets.getMaterial("engine:prog.ocDistortion");
            material.enable();

            updateOcShaderParametersForVP(material, 0, 0, rtFullWidth / 2, rtFullHeight, WorldRenderingStage.LEFT_EYE);
        } else {
            if (renderingDebugConfig.isEnabled()) {
                material = Assets.getMaterial("engine:prog.debug");
            } else {
                material = Assets.getMaterial("engine:prog.post");
            }

            material.enable();
        }

        renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), org.lwjgl.opengl.Display.getHeight());

        if (renderingConfig.isOculusVrSupport()) {
            updateOcShaderParametersForVP(material, rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight, WorldRenderingStage.RIGHT_EYE);

            renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), Display.getHeight());
        }
    }

    private void generateCombinedScene() {
        Assets.getMaterial("engine:prog.combine").enable();

        bindFbo("sceneOpaquePingPong");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneOpaquePingPong");

        flipPingPongFbo("sceneOpaque");
        attachDepthBufferToFbo("sceneOpaque", "sceneReflectiveRefractive");
    }

    private void applyLightBufferPass(String target) {
        Material program = Assets.getMaterial("engine:prog.lightBufferPass");
        program.enable();

        FBO targetFbo = getFBO(target);

        int texId = 0;
        if (targetFbo != null) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindTexture();
            program.setInt("texSceneOpaque", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindDepthTexture();
            program.setInt("texSceneOpaqueDepth", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindNormalsTexture();
            program.setInt("texSceneOpaqueNormals", texId++);

            GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
            targetFbo.bindLightBufferTexture();
            program.setInt("texSceneOpaqueLightBuffer", texId++, true);
        }

        bindFbo(target + "PingPong");
        setRenderBufferMask(true, true, true);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo(target + "PingPong");

        flipPingPongFbo(target);

        if (target.equals("sceneOpaque")) {
            attachDepthBufferToFbo("sceneOpaque", "sceneReflectiveRefractive");
        }
    }

    private void generateSkyBand(int id) {
        FBO skyBand = getFBO("sceneSkyBand" + id);

        if (skyBand == null) {
            return;
        }

        skyBand.bind();
        setRenderBufferMask(true, false, false);

        Material material = Assets.getMaterial("engine:prog.blur");

        material.enable();
        material.setFloat("radius", 8.0f, true);
        material.setFloat2("texelSize", 1.0f / skyBand.width(), 1.0f / skyBand.height(), true);

        if (id == 0) {
            bindFboTexture("sceneOpaque");
        } else {
            bindFboTexture("sceneSkyBand" + (id - 1));
        }

        glViewport(0, 0, skyBand.width(), skyBand.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        skyBand.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateToneMappedScene() {
        Assets.getMaterial("engine:prog.hdr").enable();

        bindFbo("sceneToneMapped");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneToneMapped");
    }

    private void generateLightShafts() {
        Assets.getMaterial("engine:prog.lightshaft").enable();

        FBO lightshaft = getFBO("lightShafts");

        if (lightshaft == null) {
            return;
        }

        lightshaft.bind();

        glViewport(0, 0, lightshaft.width(), lightshaft.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        lightshaft.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateSSAO() {
        Material ssaoShader = Assets.getMaterial("engine:prog.ssao");
        ssaoShader.enable();

        FBO ssao = getFBO("ssao");

        if (ssao == null) {
            return;
        }

        ssaoShader.setFloat2("texelSize", 1.0f / ssao.width(), 1.0f / ssao.height(), true);
        ssaoShader.setFloat2("noiseTexelSize", 1.0f / 4.0f, 1.0f / 4.0f, true);

        ssao.bind();

        glViewport(0, 0, ssao.width(), ssao.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        ssao.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateSobel() {
        Assets.getMaterial("engine:prog.sobel").enable();

        FBO sobel = getFBO("sobel");

        if (sobel == null) {
            return;
        }

        sobel.bind();

        glViewport(0, 0, sobel.width(), sobel.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        sobel.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBlurredSSAO() {
        Material shader = Assets.getMaterial("engine:prog.ssaoBlur");
        shader.enable();

        FBO ssao = getFBO("ssaoBlurred");

        if (ssao == null) {
            return;
        }

        shader.setFloat2("texelSize", 1.0f / ssao.width(), 1.0f / ssao.height(), true);
        ssao.bind();

        glViewport(0, 0, ssao.width(), ssao.height());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        bindFboTexture("ssao");

        renderFullscreenQuad();

        ssao.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generatePrePost() {
        Assets.getMaterial("engine:prog.prePost").enable();

        bindFbo("scenePrePost");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("scenePrePost");
    }

    private void generateHighPass() {
        Material program = Assets.getMaterial("engine:prog.highp");
        program.setFloat("highPassThreshold", bloomHighPassThreshold, true);
        program.enable();

        FBO highPass = getFBO("sceneHighPass");

        if (highPass == null) {
            return;
        }

        highPass.bind();

        FBO sceneOpaque = getFBO("sceneOpaque");

        int texId = 0;
        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindTexture();
        program.setInt("tex", texId++);

//        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
//        sceneOpaque.bindDepthTexture();
//        program.setInt("texDepth", texId++);

        glViewport(0, 0, highPass.width(), highPass.height());

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        highPass.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBlur(int id) {
        Material material = Assets.getMaterial("engine:prog.blur");
        material.enable();

        material.setFloat("radius", overallBlurRadiusFactor * renderingConfig.getBlurRadius(), true);

        FBO blur = getFBO("sceneBlur" + id);

        if (blur == null) {
            return;
        }

        material.setFloat2("texelSize", 1.0f / blur.width(), 1.0f / blur.height(), true);

        blur.bind();

        glViewport(0, 0, blur.width(), blur.height());

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            bindFboTexture("sceneToneMapped");
        } else {
            bindFboTexture("sceneBlur" + (id - 1));
        }

        renderFullscreenQuad();

        blur.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBloom(int id) {
        Material shader = Assets.getMaterial("engine:prog.blur");

        shader.enable();
        shader.setFloat("radius", bloomBlurRadius, true);

        FBO bloom = getFBO("sceneBloom" + id);

        if (bloom == null) {
            return;
        }

        shader.setFloat2("texelSize", 1.0f / bloom.width(), 1.0f / bloom.height(), true);

        bloom.bind();

        glViewport(0, 0, bloom.width(), bloom.height());

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            getFBO("sceneHighPass").bindTexture();
        } else {
            getFBO("sceneBloom" + (id - 1)).bindTexture();
        }

        renderFullscreenQuad();

        bloom.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateDownsampledScene() {
        Material shader = Assets.getMaterial("engine:prog.down");
        shader.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = TeraMath.pow(2, i + 1);

            int size = TeraMath.pow(2, i);
            shader.setFloat("size", size, true);

            bindFbo("scene" + size);
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 4) {
                bindFboTexture("scenePrePost");
            } else {
                bindFboTexture("scene" + sizePrev);
            }

            renderFullscreenQuad();

            unbindFbo("scene" + size);
        }

        glViewport(0, 0, rtFullWidth, rtFullHeight);
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

    public void takeScreenshot() {
        takeScreenshot = true;

        overwriteRtWidth = renderingConfig.getScreenshotSize().getWidth(Display.getWidth());
        overwriteRtHeight = renderingConfig.getScreenshotSize().getHeight(Display.getHeight());

        createOrUpdateFullscreenFbos();
    }

    public void saveScreenshot() {
        if (!takeScreenshot) {
            return;
        }

        final FBO fboSceneFinal = getFBO("sceneFinal");

        if (fboSceneFinal == null) {
            return;
        }

        final ByteBuffer buffer = BufferUtils.createByteBuffer(fboSceneFinal.width() * fboSceneFinal.height() * 4);

        fboSceneFinal.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        fboSceneFinal.unbindTexture();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                final String format = renderingConfig.getScreenshotFormat().toString();
                final String fileName = "Terasology-" + sdf.format(new Date()) + "-" + fboSceneFinal.width() + "x" + fboSceneFinal.height() + "." + format;
                Path path = PathManager.getInstance().getScreenshotPath().resolve(fileName);
                BufferedImage image = new BufferedImage(fboSceneFinal.width(), fboSceneFinal.height(), BufferedImage.TYPE_INT_RGB);

                for (int x = 0; x < fboSceneFinal.width(); x++) {
                    for (int y = 0; y < fboSceneFinal.height(); y++) {
                        int i = (x + fboSceneFinal.width() * y) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        image.setRGB(x, fboSceneFinal.height() - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                    ImageIO.write(image, format, out);
                    logger.info("Screenshot '" + fileName + "' saved! ");
                } catch (IOException e) {
                    logger.warn("Failed to save screenshot!", e);
                }
            }
        };

        CoreRegistry.get(GameEngine.class).submitTask("Write screenshot", task);

        takeScreenshot = false;
        overwriteRtWidth = 0;
        overwriteRtHeight = 0;

        createOrUpdateFullscreenFbos();
    }

    public float getExposure() {
        return currentExposure;
    }

    public FBO getFBO(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + title + "'!");
        }

        return fbo;
    }

    public boolean bindFbo(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bind();
            currentlyBoundFboName = title;
            return true;
        }

        logger.error("Failed to bind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean unbindFbo(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.unbind();
            currentlyBoundFboName = "";
            return true;
        }

        logger.error("Failed to unbind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboDepthTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboNormalsTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboLightBufferTexture(String title) {
        FBO fbo = fboLookup.get(title);

        if (fbo != null) {
            fbo.bindLightBufferTexture();
            return true;
        }

        logger.error("Failed to bind FBO texture since the requested FBO could not be found!");
        return false;
    }

    public void flipPingPongFbo(String title) {
        FBO fbo1 = getFBO(title);
        FBO fbo2 = getFBO(title + "PingPong");

        if (fbo1 == null || fbo2 == null) {
            return;
        }

        fboLookup.put(title, fbo2);
        fboLookup.put(title + "PingPong", fbo1);
    }

    /**
     * Builder class to simplify the syntax creating an FBO.
     * <p>
     * Once the desired characteristics of the FBO are set via the Builder's constructor and its
     * use*Buffer() methods, the build() method can be called for the actual FBO to be generated,
     * alongside the underlying FrameBuffer and its attachments on the GPU.
     * <p>
     * The new FBO is automatically registered with the LwjglRenderingProcess, overwriting any
     * existing FBO with the same title.
     */
    public class FBObuilder {

        private FBO generatedFBO;

        private String title;
        private FBO.Dimensions dimensions;
        private FBO.Type type;

        private boolean useDepthBuffer;
        private boolean useNormalBuffer;
        private boolean useLightBuffer;
        private boolean useStencilBuffer;

        /**
         * Constructs an FBO builder capable of building the two most basic FBOs:
         * an FBO with no attachments or one with a single color buffer attached to it.
         * <p>
         * To attach additional buffers, see the use*Buffer() methods.
         * <p>
         * Example: FBO basicFBO = new FBObuilder("basic", new Dimensions(1920, 1080), Type.DEFAULT).build();
         *
         * @param title A string identifier, the title is used to later manipulate the FBO through
         *              methods such as LwjglRenderingProcess.getFBO(title) and LwjglRenderingProcess.bindFBO(title).
         * @param dimensions A Dimensions object providing width and height information.
         * @param type Type.DEFAULT will result in a 32 bit color buffer attached to the FBO. (GL_RGBA, GL11.GL_UNSIGNED_BYTE, GL_LINEAR)
         *             Type.HDR will result in a 64 bit color buffer attached to the FBO. (GL_RGBA, GL_HALF_FLOAT_ARB, GL_LINEAR)
         *             Type.NO_COLOR will result in -no- color buffer attached to the FBO
         *             (WARNING: this could result in an FBO with Status.DISPOSED - see FBO.getStatus()).
         */
        public FBObuilder(String title, FBO.Dimensions dimensions, FBO.Type type) {
            this.title = title;
            this.dimensions = dimensions;
            this.type = type;
        }

        /**
         * Same as the previous FBObuilder constructor, but taking in input
         * explicit, integer width and height instead of a Dimensions object.
         */
        public FBObuilder(String title, int width, int height, FBO.Type type) {
            this(title,  new FBO.Dimensions(width, height), type);
        }

/*
 *  * @param useDepthBuffer If true the FBO will have a 24 bit depth buffer attached to it. (GL_DEPTH_COMPONENT24, GL_UNSIGNED_INT, GL_NEAREST)
    * @param useNormalBuffer If true the FBO will have a 32 bit normals buffer attached to it. (GL_RGBA, GL_UNSIGNED_BYTE, GL_LINEAR)
    * @param useLightBuffer If true the FBO will have 32/64 bit light buffer attached to it, depending if Type is DEFAULT/HDR.
*                       (GL_RGBA/GL_RGBA16F_ARB, GL_UNSIGNED_BYTE/GL_HALF_FLOAT_ARB, GL_LINEAR)
    * @param useStencilBuffer If true the depth buffer will also have an 8 bit Stencil buffer associated with it.
    *                         (GL_DEPTH24_STENCIL8_EXT, GL_UNSIGNED_INT_24_8_EXT, GL_NEAREST)
                *                         */

        /**
         * Sets the builder to generate, allocate and attach a 24 bit depth buffer to the FrameBuffer to be built.
         * If useStencilBuffer() is also used, an 8 bit stencil buffer will also be associated with the depth buffer.
         * For details on the specific characteristics of the buffers, see the FBO.create() method.
         *
         * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useDepthBuffer().build();
         */
        public FBObuilder useDepthBuffer() {
            useDepthBuffer = true;
            return this;
        }

        /**
         * Sets the builder to generate, allocate and attach a normals buffer to the FrameBuffer to be built.
         * For details on the specific characteristics of the buffer, see the FBO.create() method.
         *
         * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useNormalsBuffer().build();
         */
        public FBObuilder useNormalBuffer() {
            useNormalBuffer = true;
            return this;
        }

        /**
         * Sets the builder to generate, allocate and attach a light buffer to the FrameBuffer to be built.
         * Be aware that the number of bits per channel for this buffer changes with the set FBO.Type.
         * For details see the FBO.create() method.
         *
         * @return The calling instance, to chain calls, i.e.: new FBObuilder(...).useLightBuffer().build();
         */
        public FBObuilder useLightBuffer() {
            useLightBuffer = true;
            return this;
        }

        /**
         * -IF- the builder has been set to generate a depth buffer, using this method sets the builder to
         * generate a depth buffer inclusive of stencil buffer, with the following characteristics:
         * internal format GL_DEPTH24_STENCIL8_EXT, data type GL_UNSIGNED_INT_24_8_EXT and filtering GL_NEAREST.
         *
         * @return The calling instance of FBObuilder, to chain calls,
         *         i.e.: new FBObuilder(...).useDepthBuffer().useStencilBuffer().build();
         */
        public FBObuilder useStencilBuffer() {
            useStencilBuffer = true;
            return this;
        }

        /**
         * Given information set through the constructor and the use*Buffer() methods, builds and returns
         * an FBO instance, inclusive the underlying OpenGL FrameBuffer and any requested attachments.
         * <p>
         * The FBO is also automatically registered with the LwjglRenderingProcess through its title string.
         * This allows its retrieval and binding through methods such as getFBO(String title) and
         * bindFBO(String title). If another FBO is registered with the same title, it is disposed and
         * the new FBO registered in its place.
         * <p>
         * This method is effectively mono-use: calling it more than once will return the exact same FBO
         * returned the first time. To build a new FBO with identical or different characteristics it's
         * necessary to instantiate a new builder.
         *
         * @return An FBO. Make sure to check it with FBO.getStatus() before using it.
         */
        public FBO build() {
            if (generatedFBO != null) {
                return generatedFBO;
            }

            FBO oldFBO = fboLookup.get(title);
            if (oldFBO != null) {
                oldFBO.dispose();
                fboLookup.remove(title);
                logger.warn("FBO " + title + " has been overwritten. Ideally it would have been deleted first.");
            }

            generatedFBO = FBO.create(title, dimensions, type, useDepthBuffer, useNormalBuffer, useLightBuffer, useStencilBuffer);
            handleIncompleteAndUnexpectedStatus(generatedFBO);
            fboLookup.put(title, generatedFBO);
            return generatedFBO;
        }

        private void handleIncompleteAndUnexpectedStatus(FBO fbo) {
            // At this stage it's unclear what should be done in this circumstances as I (manu3d) do not know what
            // the effects of using an incomplete FrameBuffer are. Throw an exception? Live with visual artifacts?
            if (fbo.getStatus() == FBO.Status.INCOMPLETE) {
                logger.error("FBO " + title + " is incomplete. Look earlier in the log for details.");
            } else if (fbo.getStatus() == FBO.Status.UNEXPECTED) {
                logger.error("FBO " + title + " has generated an unexpected status code. Look earlier in the log for details.");
            }
        }
    }
}

