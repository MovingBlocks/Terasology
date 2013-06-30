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
package org.terasology.rendering.renderingProcesses;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.editor.properties.IPropertyProvider;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.paths.PathManager;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.assets.GLSLShaderProgramInstance;
import org.terasology.rendering.world.WorldRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;

/**
 * The default rendering process.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultRenderingProcess implements IPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRenderingProcess.class);

    private static DefaultRenderingProcess instance = null;

    /* PROPERTIES */
    private Property hdrExposureDefault = new Property("hdrExposureDefault", 2.5f, 0.0f, 10.0f);
    private Property hdrMaxExposure = new Property("hdrMaxExposure", 8.0f, 0.0f, 10.0f);
    private Property hdrMaxExposureNight = new Property("hdrMaxExposureNight", 1.0f, 0.0f, 10.0f);
    private Property hdrMinExposure = new Property("hdrMinExposure", 1.0f, 0.0f, 10.0f);
    private Property hdrTargetLuminance = new Property("hdrTargetLuminance", 0.5f, 0.0f, 4.0f);
    private Property hdrExposureAdjustmentSpeed = new Property("hdrExposureAdjustmentSpeed", 0.05f, 0.0f, 0.5f);

    private Property bloomHighPassThreshold = new Property("bloomHighPassThreshold", 1.05f, 0.0f, 5.0f);

    private Property ssaoBlurRadius = new Property("ssaoBlurRadius", 8.0f, 0.0f, 64.0f);

    private Property overallBlurFactor = new Property("overallBlurFactor", 1.75f, 0.0f, 16.0f);

    /* HDR */
    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;

    /* RTs */
    private int rtFullWidth;
    private int rtFullHeight;
    private int rtWidth2, rtHeight2;
    private int rtWidth4, rtHeight4;
    private int rtWidth8, rtHeight8;
    private int rtWidth16, rtHeight16;

    private int overwriteRtWidth = 0;
    private int overwriteRtHeight = 0;

    private String currentlyBoundFboName = "";

    /* VARIOUS */
    private boolean takeScreenshot = false;

    private int displayListQuad = -1;

    private PBO readBackPBOFront, readBackPBOBack, readBackPBOCurrent;

    private Config config = CoreRegistry.get(Config.class);

    public enum FBOType {
        DEFAULT,
        HDR,
        NO_COLOR
    }

    public enum StereoRenderState {
        MONO,
        OCULUS_LEFT_EYE,
        OCULUS_RIGHT_EYE
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

            // Maybe fix for the issues appearing on some platforms where accessing the "cachedBuffer" causes a JVM exception and therefore a crash...
            ByteBuffer resultBuffer = BufferUtils.createByteBuffer(cachedBuffer.capacity());
            resultBuffer.put(cachedBuffer);
            cachedBuffer.rewind();
            resultBuffer.flip();

            EXTPixelBufferObject.glUnmapBufferARB(EXTPixelBufferObject.GL_PIXEL_PACK_BUFFER_EXT);
            unbind();

            return resultBuffer;
        }
    }

    public class FBO {
        public int fboId = 0;
        public int textureId = 0;
        public int depthTextureId = 0;
        public int depthRboId = 0;
        public int normalsTextureId = 0;
        public int lightBufferTextureId = 0;

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

        public void bindLightBufferTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightBufferTextureId);
        }

        public void unbindTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        }
    }

    private HashMap<String, FBO> FBOs = new HashMap<String, FBO>();

    /**
     * Returns (and creates – if necessary) the static instance
     * of this helper class.
     *
     * @return The instance
     */
    public static DefaultRenderingProcess getInstance() {
        if (instance == null) {
            instance = new DefaultRenderingProcess();
        }

        return instance;
    }

    public DefaultRenderingProcess() {
        initialize();
    }

    public void initialize() {
        createOrUpdateFullscreenFbos();

        createFBO("scene16", 16, 16, FBOType.DEFAULT, false, false);
        createFBO("scene8", 8, 8, FBOType.DEFAULT, false, false);
        createFBO("scene4", 4, 4, FBOType.DEFAULT, false, false);
        createFBO("scene2", 2, 2, FBOType.DEFAULT, false, false);
        createFBO("scene1", 1, 1, FBOType.DEFAULT, false, false);

        readBackPBOFront = new PBO();
        readBackPBOBack = new PBO();
        readBackPBOFront.init(1,1);
        readBackPBOBack.init(1,1);

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

        if (CoreRegistry.get(Config.class).getRendering().isOculusVrSupport()) {
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

        FBO scene = getFBO("sceneOpaque");
        final boolean recreate = scene == null || (scene.width != rtFullWidth || scene.height != rtFullHeight);

        if (!recreate) {
            return;
        }

        createFBO("sceneOpaque", rtFullWidth, rtFullHeight, FBOType.HDR, true, true, true);
        createFBO("sceneOpaquePingPong", rtFullWidth, rtFullHeight, FBOType.HDR, true, true, true);

        createFBO("sceneTransparent", rtFullWidth, rtFullHeight, FBOType.HDR, false, false);
        attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");

        createFBO("sceneReflected", rtWidth2, rtHeight2, FBOType.DEFAULT, true, true, true);
        createFBO("sceneReflectedPingPong", rtWidth2, rtHeight2, FBOType.DEFAULT, true, true, true);

        createFBO("sceneShadowMap", config.getRendering().getShadowMapResolution(), config.getRendering().getShadowMapResolution(), FBOType.NO_COLOR, true, false);

        createFBO("scenePrePost", rtFullWidth, rtFullHeight, FBOType.HDR, false, false);
        createFBO("sceneToneMapped", rtFullWidth, rtFullHeight, FBOType.HDR, false, false);
        createFBO("sceneFinal", rtFullWidth, rtFullHeight, FBOType.DEFAULT, false, false);

        createFBO("sobel", rtFullWidth, rtFullHeight, FBOType.DEFAULT, false, false);

        createFBO("ssao", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);
        createFBO("ssaoBlurred0", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);
        createFBO("ssaoBlurred1", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);

        createFBO("lightShafts", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);

        createFBO("sceneHighPass", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);
        createFBO("sceneBloom0", rtWidth16, rtHeight16, FBOType.DEFAULT, false, false);
        createFBO("sceneBloom1", rtWidth16, rtHeight16, FBOType.DEFAULT, false, false);

        createFBO("sceneBlur0", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);
        createFBO("sceneBlur1", rtWidth2, rtHeight2, FBOType.DEFAULT, false, false);

        createFBO("sceneSkyBand0", rtWidth16, rtHeight16, FBOType.DEFAULT, false, false);
        createFBO("sceneSkyBand1", rtWidth16, rtHeight16, FBOType.DEFAULT, false, false);
    }

    public void deleteFBO(String title) {
        if (FBOs.containsKey(title)) {
            FBO fbo = FBOs.get(title);

            EXTFramebufferObject.glDeleteFramebuffersEXT(fbo.fboId);
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthRboId);
            GL11.glDeleteTextures(fbo.normalsTextureId);
            GL11.glDeleteTextures(fbo.depthTextureId);
            GL11.glDeleteTextures(fbo.textureId);
        }
    }

    public boolean attachDepthBufferToFbo(String sourceFboName, String targetFboName) {
        FBO source = getFBO(sourceFboName);
        FBO target = getFBO(targetFboName);

        if (source == null || target == null) {
            return false;
        }

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, target.fboId);

        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, source.depthRboId);
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, source.depthTextureId, 0);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        return true;
    }

    public FBO createFBO(String title, int width, int height, FBOType type) {
        return createFBO(title, width, height, type, false, false, false);
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depth) {
        return createFBO(title, width, height, type, depth, false, false);
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depth, boolean normals) {
        return createFBO(title, width, height, type, depth, normals, false);
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depth, boolean normals, boolean lightBuffer) {
        // Make sure to delete the existing FBO before creating a new one
        deleteFBO(title);

        // Create a new FBO object
        FBO fbo = new FBO();
        fbo.width = width;
        fbo.height = height;

        // Create the FBO
        fbo.fboId = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo.fboId);

        if (type != FBOType.NO_COLOR) {
            fbo.textureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.textureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            if (type == FBOType.HDR) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
            }

            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, fbo.textureId, 0);
        }

        if (normals) {
            fbo.normalsTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.normalsTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT, GL11.GL_TEXTURE_2D, fbo.normalsTextureId, 0);
        }

        if (lightBuffer) {
            fbo.lightBufferTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.lightBufferTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            if (type == FBOType.HDR) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
            }

            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT2_EXT, GL11.GL_TEXTURE_2D, fbo.lightBufferTextureId, 0);
        }

        if (depth) {
            fbo.depthTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.depthTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.ByteBuffer) null);

            fbo.depthRboId = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthRboId);
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);

            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthRboId);
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthTextureId, 0);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        if (type != FBOType.NO_COLOR) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        }
        if (normals) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT);
        }
        if (lightBuffer) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT2_EXT);
        }
        bufferIds.flip();

        if (bufferIds.limit() == 0) {
            GL11.glReadBuffer(GL11.GL_NONE);
            GL20.glDrawBuffers(GL11.GL_NONE);
        } else {
            GL20.glDrawBuffers(bufferIds);
        }

        int checkFB = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        switch (checkFB) {
            case EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT:
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_UNSUPPORTED_EXT exception");
                break;
            case EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                logger.error("FrameBuffer: " + title
                        + ", has caused a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT exception");
                
                /*
                 * On some graphics cards, FBOType.NO_COLOR can cause a GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT.
                 * Attempt to continue without this FBO.
                 */
                if (type == FBOType.NO_COLOR) {
                	logger.error("FrameBuffer: " + title
                            + ", ...but the FBOType was NO_COLOR, ignoring this error and continuing without this FBO.");

                	return null;
                }

                break;
            default:
                logger.error("Unexpected reply from glCheckFramebufferStatusEXT: " + checkFB);
                break;
        }

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        FBOs.put(title, fbo);
        return fbo;
    }

    private void updateExposure() {
        if (config.getRendering().isEyeAdaptation()) {
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

    public void clear() {
        bindFbo("sceneOpaque");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        unbindFbo("sceneOpaque");
        bindFbo("sceneTransparent");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        unbindFbo("sceneTransparent");
    }

    public void beginRenderSceneOpaque() {
        bindFbo("sceneOpaque");
    }

    public void endRenderSceneOpaque() {
        unbindFbo("sceneOpaque");
    }

    public void beginRenderLightGeometry() {
        bindFbo("sceneOpaque");

        // TODO: Use stencil masking technique for lights instead of disabling the depth test
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        glCullFace(GL_FRONT);

        // Only write to the light buffer
        setRenderBufferMask(false, false, true);
    }

    public void endRenderLightGeometry() {
        setRenderBufferMask(true, true, true);
        unbindFbo("sceneOpaque");

        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_DEPTH_TEST);
        glCullFace(GL_BACK);

        applyLightBufferPass("sceneOpaque");
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

        if (fbo.textureId != 0) {
            if (color) {
                bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.normalsTextureId != 0) {
            if (normal) {
                bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.lightBufferTextureId != 0) {
            if (lightBuffer) {
                bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    public void beginRenderSceneTransparent() {
        bindFbo("sceneTransparent");
    }

    public void endRenderSceneTransparent() {
        unbindFbo("sceneTransparent");
    }

    public void beginRenderReflectedScene() {
        FBO reflected = getFBO("sceneReflected");

        if (reflected == null) {
            return;
        }

        reflected.bind();

        glViewport(0, 0, reflected.width, reflected.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderReflectedScene() {
        unbindFbo("sceneReflected");

        applyLightBufferPass("sceneReflected");
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneShadowMap() {
        FBO shadowMap = getFBO("sceneShadowMap");
        shadowMap.bind();

        if (shadowMap == null) {
            return;
        }

        glViewport(0, 0, shadowMap.width, shadowMap.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderSceneShadowMap() {
        unbindFbo("sceneShadowMap");
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneSkyBand() {
    }

    public void endRenderSceneSkyBand() {
        generateSkyBand(0);
        generateSkyBand(1);

        bindFbo("sceneOpaque");
    }

    public void renderScene() {
        renderScene(StereoRenderState.MONO);
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

        if (stereoRenderState == StereoRenderState.OCULUS_LEFT_EYE
                || stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE
                || (stereoRenderState == StereoRenderState.MONO && takeScreenshot)
                || (stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE && takeScreenshot)) {

            renderFinalSceneToRT(stereoRenderState);

            if (takeScreenshot) {
                saveScreenshot();
            }
        }

        if (stereoRenderState == StereoRenderState.MONO
                || stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE) {
            renderFinalScene();
        }
    }

    private void renderFinalSceneToRT(StereoRenderState stereoRenderState) {
        GLSLShaderProgramInstance shader;

        if (config.getSystem().isDebugRenderingEnabled()) {
            shader = ShaderManager.getInstance().getShaderProgramInstance("debug");
        } else {
            shader = ShaderManager.getInstance().getShaderProgramInstance("post");
        }

        shader.enable();

        bindFbo("sceneFinal");

        if (stereoRenderState == StereoRenderState.MONO || stereoRenderState == StereoRenderState.OCULUS_LEFT_EYE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        switch (stereoRenderState) {
            case MONO:
                renderFullscreenQuad(0,0, rtFullWidth, rtFullHeight);
                break;
            case OCULUS_LEFT_EYE:
                renderFullscreenQuad(0,0, rtFullWidth / 2, rtFullHeight);
                break;
            case OCULUS_RIGHT_EYE:
                renderFullscreenQuad(rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight);
                break;
        }

        unbindFbo("sceneFinal");
    }

    private void updateOcShaderParametersForVP(GLSLShaderProgramInstance program, int vpX, int vpY, int vpWidth, int vpHeight, StereoRenderState stereoRenderState) {
        float w = (float) vpWidth / rtFullWidth;
        float h = (float) vpHeight / rtFullHeight;
        float x = (float) vpX / rtFullWidth;
        float y = (float) vpY / rtFullHeight;

        float as = (float) vpWidth / vpHeight;

        program.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1], OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3]);

        float ocLensCenter = (stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        program.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f);
        program.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        program.setFloat2("ocScale", (w/2) * scaleFactor, (h/2) * scaleFactor * as);
        program.setFloat2("ocScaleIn", (2/w), (2/h) / as);
    }

    private void renderFinalScene() {

        GLSLShaderProgramInstance shader;

        if (config.getRendering().isOculusVrSupport()) {
            shader = ShaderManager.getInstance().getShaderProgramInstance("ocDistortion");
            shader.enable();

            updateOcShaderParametersForVP(shader, 0, 0, rtFullWidth / 2, rtFullHeight, StereoRenderState.OCULUS_LEFT_EYE);
        } else {
            if (config.getSystem().isDebugRenderingEnabled()) {
                shader = ShaderManager.getInstance().getShaderProgramInstance("debug");
            } else {
                shader = ShaderManager.getInstance().getShaderProgramInstance("post");
            }

            shader.enable();
        }

        renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), org.lwjgl.opengl.Display.getHeight());

        if (config.getRendering().isOculusVrSupport()) {
            updateOcShaderParametersForVP(shader, rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight, StereoRenderState.OCULUS_RIGHT_EYE);

            renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), Display.getHeight());
        }
    }

    private void generateCombinedScene() {
        ShaderManager.getInstance().enableShader("combine");

        bindFbo("sceneOpaquePingPong");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneOpaquePingPong");

        flipPingPongFbo("sceneOpaque");
        attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");
    }

    private void applyLightBufferPass(String target) {
        GLSLShaderProgramInstance program = ShaderManager.getInstance().getShaderProgramInstance("lightBufferPass");
        program.enable();

        DefaultRenderingProcess.FBO targetFbo = DefaultRenderingProcess.getInstance().getFBO(target);

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
            program.setInt("texSceneOpaqueLightBuffer", texId++);
        }

        bindFbo(target+"PingPong");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo(target + "PingPong");

        flipPingPongFbo(target);

        if (target.equals("sceneOpaque")) {
            attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");
        }
    }

    private void generateSkyBand(int id) {
        FBO skyBand = getFBO("sceneSkyBand"+id);

        if (skyBand == null) {
            return;
        }

        skyBand.bind();

        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("blur");

        shader.enable();
        shader.setFloat("radius", 32.0f);

        if (id == 0) {
            bindFboTexture("sceneOpaque");
        } else {
            bindFboTexture("sceneSkyBand" + (id - 1));
        }

        glViewport(0, 0, skyBand.width, skyBand.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        skyBand.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateToneMappedScene() {
        ShaderManager.getInstance().enableShader("hdr");

        bindFbo("sceneToneMapped");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneToneMapped");
    }

    private void generateLightShafts() {
        ShaderManager.getInstance().enableShader("lightshaft");

        FBO lightshaft = getFBO("lightShafts");

        if (lightshaft == null) {
            return;
        }

        lightshaft.bind();

        glViewport(0, 0, lightshaft.width, lightshaft.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        lightshaft.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateSSAO() {
        ShaderManager.getInstance().enableShader("ssao");

        FBO ssao = getFBO("ssao");

        if (ssao == null) {
            return;
        }

        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        ssao.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateSobel() {
        ShaderManager.getInstance().enableShader("sobel");

        FBO sobel = getFBO("sobel");

        if (sobel == null) {
            return;
        }

        sobel.bind();

        glViewport(0, 0, sobel.width, sobel.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        sobel.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBlurredSSAO(int id) {
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("blur");

        shader.enable();
        shader.setFloat("radius", (Float) ssaoBlurRadius.getValue());

        FBO ssao = getFBO("ssaoBlurred" + id);

        if (ssao == null) {
            return;
        }

        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            bindFboTexture("ssao");
        } else {
            bindFboTexture("ssaoBlurred" + (id - 1));
        }

        renderFullscreenQuad();

        ssao.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generatePrePost() {
        ShaderManager.getInstance().enableShader("prePost");

        bindFbo("scenePrePost");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("scenePrePost");
    }

    private void generateHighPass() {
        GLSLShaderProgramInstance program = ShaderManager.getInstance().getShaderProgramInstance("highp");
        program.setFloat("highPassThreshold", (Float) bloomHighPassThreshold.getValue());
        program.enable();

        FBO highPass = getFBO("sceneHighPass");

        if (highPass == null) {
            return;
        }

        highPass.bind();

        glViewport(0, 0, highPass.width, highPass.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        bindFboTexture("sceneToneMapped");

        renderFullscreenQuad();

        highPass.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBlur(int id) {
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("blur");
        shader.enable();

        shader.setFloat("radius", (Float) overallBlurFactor.getValue() * config.getRendering().getBlurRadius());

        FBO blur = getFBO("sceneBlur" + id);

        if (blur == null) {
            return;
        }

        blur.bind();

        glViewport(0, 0, blur.width, blur.height);

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
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("blur");

        shader.enable();
        shader.setFloat("radius", 32.0f);

        FBO bloom = getFBO("sceneBloom" + id);

        if (bloom == null) {
            return;
        }

        bloom.bind();

        glViewport(0, 0, bloom.width, bloom.height);

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
        GLSLShaderProgramInstance shader = ShaderManager.getInstance().getShaderProgramInstance("down");
        shader.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = (int) java.lang.Math.pow(2, i + 1);

            int size = (int) java.lang.Math.pow(2, i);
            shader.setFloat("size", size);

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

        overwriteRtWidth = 1920*2;
        overwriteRtHeight = 1080*2;
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

        final ByteBuffer buffer = BufferUtils.createByteBuffer(fboSceneFinal.width * fboSceneFinal.height * 4);

        fboSceneFinal.bindTexture();
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        fboSceneFinal.unbindTexture();

        Runnable r = new Runnable() {
            @Override
            public void run() {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");

                final String fileName = "Terasology-" + sdf.format(cal.getTime()) + "-" + fboSceneFinal.width + "x" + fboSceneFinal.height + ".png";
                File file = new File( PathManager.getInstance().getScreenshotPath(), fileName);
                BufferedImage image = new BufferedImage(fboSceneFinal.width, fboSceneFinal.height, BufferedImage.TYPE_INT_RGB);

                for (int x = 0; x < fboSceneFinal.width; x++)
                    for (int y = 0; y < fboSceneFinal.height; y++) {
                        int i = (x + fboSceneFinal.width * y) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        image.setRGB(x, fboSceneFinal.height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }

                try {
                    ImageIO.write(image, "png", file);
                    logger.info("Screenshot '"+fileName+"' saved! ");
                } catch (IOException e) {
                    logger.warn("Could not save screenshot!", e);
                }
            }
        };

        CoreRegistry.get(GameEngine.class).submitTask("Write screenshot", r);

        takeScreenshot = false;
        overwriteRtWidth = 0;
        overwriteRtWidth = 0;

        createOrUpdateFullscreenFbos();
    }

    public float getExposure() {
        return currentExposure;
    }

    public FBO getFBO(String title) {
        FBO fbo = FBOs.get(title);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + title + "'!");
        }

        return fbo;
    }

    public boolean bindFbo(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
            fbo.bind();
            currentlyBoundFboName = title;
            return true;
        }

        logger.error("Failed to bind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean unbindFbo(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
            fbo.unbind();
            currentlyBoundFboName = "";
            return true;
        }

        logger.error("Failed to unbind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboTexture(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboDepthTexture(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboNormalsTexture(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboLightBufferTexture(String title) {
        FBO fbo = null;

        if ((fbo = FBOs.get(title)) != null) {
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

        FBOs.put(title, fbo2);
        FBOs.put(title+"PingPong", fbo1);
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
