/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.logic.manager;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.editor.properties.IPropertyProvider;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;

/**
 * Responsible for applying and rendering various shader based
 * post processing effects.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultRenderingProcess implements IPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRenderingProcess.class);

    private static DefaultRenderingProcess _instance = null;

    private Property hdrExposureDefault = new Property("hdrExposureDefault", 2.5f, 0.0f, 10.0f);
    private Property hdrMaxExposure = new Property("hdrMaxExposure", 8.0f, 0.0f, 10.0f);
    private Property hdrMaxExposureNight = new Property("hdrMaxExposureNight", 1.0f, 0.0f, 10.0f);
    private Property hdrMinExposure = new Property("hdrMinExposure", 0.5f, 0.0f, 10.0f);
    private Property hdrTargetLuminance = new Property("hdrTargetLuminance", 0.5f, 0.0f, 4.0f);
    private Property hdrExposureAdjustmentSpeed = new Property("hdrExposureAdjustmentSpeed", 0.05f, 0.0f, 0.5f);

    private Property bloomHighPassThreshold = new Property("bloomHighPassThreshold", 1.05f, 0.0f, 5.0f);

    private Property ssaoBlurRadius = new Property("ssaoBlurRadius", 8.0f, 0.0f, 64.0f);

    private Property overallBlurFactor = new Property("overallBlurFactor", 1.0f, 0.0f, 16.0f);

    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;

    private int displayListQuad = -1;

    private PBO readBackPBOFront, readBackPBOBack, readBackPBOCurrent;

    private Config config = CoreRegistry.get(Config.class);
    
	private static boolean tainted = false;
	private static Collection<String> taintedReasons = new HashSet<String>();

    public enum FBOType {
        FBOT_DEFAULT,
        FBOT_HDR,
        FBOT_NO_COLOR
    }

    public enum StereoRenderState {
        SRS_MONO,
        SRS_OCULUS_LEFT_EYE,
        SRS_OCULUS_RIGHT_EYE
    }

    public class PBO {
        public int pboId = 0;
        public int width, height;
        ByteBuffer cachedBuffer = null;

        public PBO() {
            pboId = EXTPixelBufferObject.glGenBuffersARB();
        }

        public void bind() {
            EXTPixelBufferObject.glBindBufferARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT, pboId);
        }

        public void unbind() {
            EXTPixelBufferObject.glBindBufferARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT, 0);
        }

        public void init(int width, int height) {
            this.width = width;
            this.height = height;

            int byteSize = width * height * 4;
            cachedBuffer = BufferUtils.createByteBuffer(byteSize);

            bind();
            EXTPixelBufferObject.glBufferDataARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT, byteSize, EXTPixelBufferObject.GL_STREAM_READ_ARB);
            unbind();
        }

        public void copyFromFBO(int fboId, int width, int height, int format, int type) {
            bind();
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
            glReadPixels(0, 0, width, height, format, type, 0);
            unbind();
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }

        public ByteBuffer readBackPixels() {
            bind();
            cachedBuffer = EXTPixelBufferObject.glMapBufferARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT, GL_READ_ONLY, cachedBuffer);
            EXTPixelBufferObject.glUnmapBufferARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT);
            unbind();

            return cachedBuffer;
        }
    }

    public class FBO {
        public int fboId = 0;
        public int textureId = 0;
        public int depthTextureId = 0;
        public int depthRboId = 0;
        public int normalsTextureId = 0;

        public int width = 0;
        public int height = 0;

        public void bind() {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
        }

        public void unbind() {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }

        public void bindDepthTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTextureId);
        }

        public void bindTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        }

        public void bindNormalsTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalsTextureId);
        }

        public void unbindTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    private HashMap<String, FBO> _FBOs = new HashMap<String, FBO>();

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
    public static DefaultRenderingProcess getInstance() {
        if (_instance == null) {
            _instance = new DefaultRenderingProcess();
        }

        return _instance;
    }

    public DefaultRenderingProcess() {
        initialize();
    }

    public void initialize() {
        createOrUpdateFullscreenFbos();

        createFBO("scene16", 16, 16, FBOType.FBOT_DEFAULT, false, false);
        createFBO("scene8", 8, 8, FBOType.FBOT_DEFAULT, false, false);
        createFBO("scene4", 4, 4, FBOType.FBOT_DEFAULT, false, false);
        createFBO("scene2", 2, 2, FBOType.FBOT_DEFAULT, false, false);
        createFBO("scene1", 1, 1, FBOType.FBOT_DEFAULT, false, false);

        readBackPBOFront = new PBO();
        readBackPBOBack = new PBO();
        readBackPBOFront.init(1,1);
        readBackPBOBack.init(1,1);

        readBackPBOCurrent = readBackPBOFront;
    }
    
    private class Tainted extends RuntimeException {
    	public Tainted(Object object, String reason)
    	{
    		super(object.toString() + " tainted! (Reason: " + reason + ")");
    	}
    }
    
    private void taint(String passedReason)
    {
    	String reason = passedReason;
    	tainted = true;
    	
    	if ( (reason == null) || (reason == "") )
    		reason = "Tainted by " + Thread.currentThread().getStackTrace()[2].toString() + ", no reason given.";
    	
    	Tainted taintException = new Tainted(this, reason);
    	
    	logger.error(taintException.getLocalizedMessage());
    	taintException.printStackTrace();
    	
    	taintedReasons.add(reason);
    }


    /**
     * Initially creates the scene FBO and updates it according to the size of the viewport.
     */
    private void createOrUpdateFullscreenFbos() {
        FBO scene = getFBO("sceneOpaque");
        boolean recreate = scene == null || (scene.width != Display.getWidth() || scene.height != Display.getHeight());

        if (!recreate)
            return;

        int fullWidth = Display.getWidth();
        int fullHeight = Display.getHeight();
        final int halfWidth = fullWidth / 2;
        final int halfHeight = fullHeight / 2;
        final int quarterWidth = halfWidth / 2;
        final int quarterHeight = halfHeight / 2;
        final int halfQuarterWidth = quarterWidth / 2;
        final int halfQuarterHeight = quarterHeight / 2;

        createFBO("sceneOpaque", fullWidth, fullHeight, FBOType.FBOT_HDR, true, true);
        createFBO("sceneTransparent", fullWidth, fullHeight, FBOType.FBOT_HDR, true, true);

        createFBO("sceneShadowMap", 1024, 1024, FBOType.FBOT_NO_COLOR, true, false);

        createFBO("sceneCombined", fullWidth, fullHeight, FBOType.FBOT_HDR, true, true);

        createFBO("scenePrePost", fullWidth, fullHeight, FBOType.FBOT_HDR, false, false);
        createFBO("sceneToneMapped", fullWidth, fullHeight, FBOType.FBOT_HDR, false, false);
        createFBO("sceneFinal", fullWidth, fullHeight, FBOType.FBOT_DEFAULT, false, false);

        createFBO("sobel", fullWidth, fullHeight, FBOType.FBOT_DEFAULT, false, false);

        createFBO("ssao", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);
        createFBO("ssaoBlurred0", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);
        createFBO("ssaoBlurred1", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);

        createFBO("lightShafts", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);

        createFBO("sceneReflected", halfWidth, halfHeight, FBOType.FBOT_HDR, true, false);

        createFBO("sceneHighPass", halfQuarterWidth, halfQuarterHeight, FBOType.FBOT_DEFAULT, false, false);
        createFBO("sceneBloom0", halfQuarterWidth, halfQuarterHeight, FBOType.FBOT_DEFAULT, false, false);
        createFBO("sceneBloom1", halfQuarterWidth, halfQuarterHeight, FBOType.FBOT_DEFAULT, false, false);

        createFBO("sceneBlur0", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);
        createFBO("sceneBlur1", halfWidth, halfHeight, FBOType.FBOT_DEFAULT, false, false);
    }

    public void deleteFBO(String title) {
        if (_FBOs.containsKey(title)) {
            FBO fbo = _FBOs.get(title);

            EXTFramebufferObject.glDeleteFramebuffersEXT(fbo.fboId);
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthRboId);
            GL11.glDeleteTextures(fbo.normalsTextureId);
            GL11.glDeleteTextures(fbo.depthTextureId);
            GL11.glDeleteTextures(fbo.textureId);
        }
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depth, boolean normals) {
        // Make sure to delete the existing FBO before creating a new one
        deleteFBO(title);

        // Create a new FBO object
        FBO fbo = new FBO();
        fbo.width = width;
        fbo.height = height;

        // Create the color target texture
        fbo.textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureId);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        if (type != FBOType.FBOT_NO_COLOR) {
            if (type == FBOType.FBOT_HDR) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
            }
        }

        if (depth) {
            // Generate the depth texture
            fbo.depthTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.depthTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.ByteBuffer) null);

            // Create depth render buffer object
            fbo.depthRboId = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthRboId);
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);
        }

        if (normals) {
            // Generate the normals texture
            fbo.normalsTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.normalsTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Create the FBO
        fbo.fboId = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo.fboId);

        if (type != FBOType.FBOT_NO_COLOR) {
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, fbo.textureId, 0);
        }

        if (depth) {
            // Generate the depth render buffer and depth map texture
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthRboId);
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthTextureId, 0);
        }

        if (normals) {
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT, GL11.GL_TEXTURE_2D, fbo.normalsTextureId, 0);
        }

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        if (type != FBOType.FBOT_NO_COLOR) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        }
        if (normals) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT);
        }
        bufferIds.flip();
        GL20.glDrawBuffers(bufferIds);

        int checkFB = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        switch (checkFB) {
            case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
                
                /*
                 * On some graphics cards, FBOType.FBOT_NO_COLOR can cause a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT.
                 * Attempt to continue without this FBO.
                 */
                if (type == FBOType.FBOT_NO_COLOR) {
                	logger.error("FrameBuffer: " + title
                            + ", ...but the FBOType was FBOT_NO_COLOR, ignoring this error and continuing without this FBO.");
                	
                	taint("Got a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT because of FBOType.FBOT_NO_COLOR.");
                	return null;
                }
            default:
                throw new RuntimeException("Unexpected reply from glCheckFramebufferStatusEXT: " + checkFB);
        }

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        _FBOs.put(title, fbo);
        return fbo;
    }

    private void updateExposure() {
        if (config.getRendering().isEyeAdaptation()) {
            FBO scene = DefaultRenderingProcess.getInstance().getFBO("scene1");

            readBackPBOCurrent.copyFromFBO(scene.fboId, 1, 1, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE);

            if (readBackPBOCurrent == readBackPBOFront) {
                readBackPBOCurrent = readBackPBOBack;
            } else {
                readBackPBOCurrent = readBackPBOFront;
            }

            ByteBuffer pixels = readBackPBOCurrent.readBackPixels();

            currentSceneLuminance = 0.2126f * (pixels.get(2)& 0xFF) / 255.f + 0.7152f * (pixels.get(1) & 0xFF) / 255.f + 0.0722f * (pixels.get(0) & 0xFF) / 255.f;

            float targetExposure = (Float) hdrMaxExposure.getValue();

            if (currentSceneLuminance > 0) {
                targetExposure = (Float) hdrTargetLuminance.getValue() / currentSceneLuminance;
            }

            float maxExposure = (Float) hdrMaxExposure.getValue();

            if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
                maxExposure = (Float) hdrMaxExposureNight.getValue();
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < (Float) hdrMinExposure.getValue()) {
                targetExposure = (Float) hdrMinExposure.getValue();
            }

            currentExposure = (float) TeraMath.lerp(currentExposure, targetExposure, (Float) hdrExposureAdjustmentSpeed.getValue());

        } else {
            if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
                currentExposure = (Float) hdrMaxExposureNight.getValue();
            } else {
                currentExposure = (Float) hdrExposureDefault.getValue();
            }
        }
    }

    public void beginRenderSceneOpaque(boolean clear) {
        getFBO("sceneOpaque").bind();

        if (clear) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
    }

    public void endRenderSceneOpaque() {
        getFBO("sceneOpaque").unbind();
    }

    public void beginRenderSceneTransparent(boolean clear) {
        getFBO("sceneTransparent").bind();

        if (clear) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }
    }

    public void endRenderSceneTransparent() {
        getFBO("sceneTransparent").unbind();
    }

    public void beginRenderReflectedScene() {
        FBO reflected = getFBO("sceneReflected");
        reflected.bind();

        glViewport(0, 0, reflected.width, reflected.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderReflectedScene() {
        getFBO("sceneReflected").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void beginRenderSceneShadowMap() {
        FBO shadowMap = getFBO("sceneShadowMap");
        shadowMap.bind();

        glViewport(0, 0, shadowMap.width, shadowMap.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderSceneShadowMap() {
        getFBO("sceneShadowMap").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void renderScene() {
        renderScene(StereoRenderState.SRS_MONO);
    }

    /**
     * Renders the final scene to a quad and displays it. The FBO gets automatically rescaled if the size
     * of the view port changes.
     */
    public void renderScene(StereoRenderState stereoRenderState) {
        createOrUpdateFullscreenFbos();

        if (config.getRendering().isOutline()) {
            generateSobel();
        }

        if (config.getRendering().isSsao()) {
            generateSSAO();
            for (int i = 0; i < 2; i++) {
                generateBlurredSSAO(i);
            }
        }

        generateCombinedScene();

        if (config.getRendering().isLightShafts()) {
            generateLightShafts();
        }

        generatePrePost();

        if (config.getRendering().isEyeAdaptation()) {
            generateDownsampledScene();
        }

        updateExposure();

        generateToneMappedScene();

        if (config.getRendering().isBloom()) {
            generateHighPass();
        }

        for (int i = 0; i < 2; i++) {
            if (config.getRendering().isBloom()) {
                generateBloom(i);
            }
            if (config.getRendering().getBlurIntensity() != 0) {
                generateBlur(i);
            }
        }

        if (stereoRenderState == StereoRenderState.SRS_OCULUS_LEFT_EYE
                || stereoRenderState == StereoRenderState.SRS_OCULUS_RIGHT_EYE) {
            renderFinalSceneToRT(stereoRenderState);
        }

        if (stereoRenderState == StereoRenderState.SRS_MONO
                || stereoRenderState == StereoRenderState.SRS_OCULUS_RIGHT_EYE) {
            renderFinalScene();
        }
    }

    private void renderFinalSceneToRT(StereoRenderState stereoRenderState) {
        ShaderProgram shader;

        if (config.getSystem().isDebugRenderingEnabled()) {
            shader = ShaderManager.getInstance().getShaderProgram("debug");
        } else {
            shader = ShaderManager.getInstance().getShaderProgram("post");
        }

        shader.enable();

        DefaultRenderingProcess.getInstance().getFBO("sceneFinal").bind();

        if (stereoRenderState == StereoRenderState.SRS_MONO || stereoRenderState == StereoRenderState.SRS_OCULUS_LEFT_EYE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        switch (stereoRenderState) {
            case SRS_MONO:
                renderFullscreenQuad();
                break;
            case SRS_OCULUS_LEFT_EYE:
                renderFullscreenQuad(0,0, Display.getWidth() / 2, Display.getHeight());
                break;
            case SRS_OCULUS_RIGHT_EYE:
                renderFullscreenQuad(Display.getWidth() / 2, 0, Display.getWidth() / 2, Display.getHeight());
                break;
        }

        DefaultRenderingProcess.getInstance().getFBO("sceneFinal").unbind();
    }

    private void updateOcShaderParametersForVP(ShaderProgram program, int vpX, int vpY, int vpWidth, int vpHeight, StereoRenderState stereoRenderState) {
        float w = (float) vpWidth / Display.getWidth();
        float h = (float) vpHeight / Display.getHeight();
        float x = (float) vpX / Display.getWidth();
        float y = (float) vpY / Display.getHeight();

        float as = (float) vpWidth / vpHeight;

        program.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1], OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3]);

        float ocLensCenter = (stereoRenderState == StereoRenderState.SRS_OCULUS_RIGHT_EYE) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        program.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f);
        program.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        program.setFloat2("ocScale", (w/2) * scaleFactor, (h/2) * scaleFactor * as);
        program.setFloat2("ocScaleIn", (2/w), (2/h) / as);
    }

    private void renderFinalScene() {

        ShaderProgram shader = null;

        if (config.getRendering().isOculusVrSupport()) {
            shader = ShaderManager.getInstance().getShaderProgram("ocDistortion");
            shader.enable();

            updateOcShaderParametersForVP(shader, 0, 0, Display.getWidth() / 2, Display.getHeight(), StereoRenderState.SRS_OCULUS_LEFT_EYE);
        } else {
            if (config.getSystem().isDebugRenderingEnabled()) {
                shader = ShaderManager.getInstance().getShaderProgram("debug");
            } else {
                shader = ShaderManager.getInstance().getShaderProgram("post");
            }

            shader.enable();
        }

        renderFullscreenQuad();

        if (config.getRendering().isOculusVrSupport()) {
            updateOcShaderParametersForVP(shader, Display.getWidth() / 2, 0, Display.getWidth() / 2, Display.getHeight(), StereoRenderState.SRS_OCULUS_RIGHT_EYE);

            renderFullscreenQuad();
        }
    }

    private void generateCombinedScene() {
        ShaderManager.getInstance().enableShader("combine");

        DefaultRenderingProcess.getInstance().getFBO("sceneCombined").bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sceneCombined").unbind();
    }


    private void generateToneMappedScene() {
        ShaderManager.getInstance().enableShader("hdr");

        DefaultRenderingProcess.getInstance().getFBO("sceneToneMapped").bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sceneToneMapped").unbind();
    }

    private void generateLightShafts() {
        ShaderManager.getInstance().enableShader("lightshaft");

        FBO lightshaft = DefaultRenderingProcess.getInstance().getFBO("lightShafts");
        lightshaft.bind();

        glViewport(0, 0, lightshaft.width, lightshaft.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("lightShafts").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateSSAO() {
        ShaderManager.getInstance().enableShader("ssao");

        FBO ssao = DefaultRenderingProcess.getInstance().getFBO("ssao");
        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("ssao").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateSobel() {
        ShaderManager.getInstance().enableShader("sobel");

        FBO sobel = DefaultRenderingProcess.getInstance().getFBO("sobel");
        sobel.bind();

        glViewport(0, 0, sobel.width, sobel.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sobel").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlurredSSAO(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", (Float) ssaoBlurRadius.getValue());

        FBO ssao = DefaultRenderingProcess.getInstance().getFBO("ssaoBlurred" + id);
        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            DefaultRenderingProcess.getInstance().getFBO("ssao").bindTexture();
        } else {
            DefaultRenderingProcess.getInstance().getFBO("ssaoBlurred" + (id - 1)).bindTexture();
        }

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("ssaoBlurred" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generatePrePost() {
        ShaderManager.getInstance().enableShader("prePost");

        DefaultRenderingProcess.getInstance().getFBO("scenePrePost").bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("scenePrePost").unbind();
    }

    private void generateHighPass() {
        ShaderProgram program = ShaderManager.getInstance().getShaderProgram("highp");
        program.setFloat("highPassThreshold", (Float) bloomHighPassThreshold.getValue());
        program.enable();

        FBO highPass = DefaultRenderingProcess.getInstance().getFBO("sceneHighPass");
        highPass.bind();

        glViewport(0, 0, highPass.width, highPass.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        DefaultRenderingProcess.getInstance().getFBO("sceneToneMapped").bindTexture();

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sceneHighPass").unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlur(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();

        shader.setFloat("radius", (Float) overallBlurFactor.getValue() * config.getRendering().getBlurRadius());

        FBO blur = DefaultRenderingProcess.getInstance().getFBO("sceneBlur" + id);
        blur.bind();

        glViewport(0, 0, blur.width, blur.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            DefaultRenderingProcess.getInstance().getFBO("sceneToneMapped").bindTexture();
        } else {
            DefaultRenderingProcess.getInstance().getFBO("sceneBlur" + (id - 1)).bindTexture();
        }

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sceneBlur" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBloom(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", 32.0f);

        FBO bloom = DefaultRenderingProcess.getInstance().getFBO("sceneBloom" + id);
        bloom.bind();

        glViewport(0, 0, bloom.width, bloom.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            DefaultRenderingProcess.getInstance().getFBO("sceneHighPass").bindTexture();
        } else {
            DefaultRenderingProcess.getInstance().getFBO("sceneBloom" + (id - 1)).bindTexture();
        }

        renderFullscreenQuad();

        DefaultRenderingProcess.getInstance().getFBO("sceneBloom" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateDownsampledScene() {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("down");
        shader.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = (int) java.lang.Math.pow(2, i + 1);

            int size = (int) java.lang.Math.pow(2, i);
            shader.setFloat("size", size);

            DefaultRenderingProcess.getInstance().getFBO("scene" + size).bind();
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 4) {
                DefaultRenderingProcess.getInstance().getFBO("scenePrePost").bindTexture();
            } else {
                DefaultRenderingProcess.getInstance().getFBO("scene" + sizePrev).bindTexture();
            }

            renderFullscreenQuad();

            DefaultRenderingProcess.getInstance().getFBO("scene" + size).unbind();

        }

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
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

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
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

    public float getExposure() {
        return currentExposure;
    }

    public FBO getFBO(String title) {
        return _FBOs.get(title);
    }


    @Override
    public void addPropertiesToList(List<Property> properties) {
        properties.add(hdrMaxExposure);
        properties.add(hdrExposureAdjustmentSpeed);
        properties.add(hdrExposureDefault);
        properties.add(hdrMaxExposureNight);
        properties.add(hdrMinExposure);
        properties.add(hdrTargetLuminance);
        properties.add(ssaoBlurRadius);
        properties.add(overallBlurFactor);
        properties.add(bloomHighPassThreshold);
    }
}
