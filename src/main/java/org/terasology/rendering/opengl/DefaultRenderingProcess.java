/*
 * Copyright 2013 Moving Blocks
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
import org.lwjgl.opengl.ARBHalfFloatPixel;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.EXTPackedDepthStencil;
import org.lwjgl.opengl.EXTPixelBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.editor.EditorRange;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.paths.PathManager;
import org.terasology.math.TeraMath;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.oculusVr.OculusVrHelper;
import org.terasology.rendering.world.WorldRenderer;

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
import java.util.Calendar;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_BACK;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DECR;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_INCR;
import static org.lwjgl.opengl.GL11.GL_KEEP;
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
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3i;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_READ_ONLY;
import static org.lwjgl.opengl.GL20.glStencilOpSeparate;

/**
 * The Default Rendering Process class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultRenderingProcess {

    private static final Logger logger = LoggerFactory.getLogger(DefaultRenderingProcess.class);

    private static DefaultRenderingProcess instance = null;

    /* PROPERTIES */
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrExposureDefault = 2.5f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMaxExposure = 8.0f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMaxExposureNight = 1.0f;
    @EditorRange(min = 0.0f, max = 10.0f)
    private float hdrMinExposure = 1.0f;
    @EditorRange(min = 0.0f, max = 4.0f)
    private float hdrTargetLuminance = 1.0f;
    @EditorRange(min = 0.0f, max = 0.5f)
    private float hdrExposureAdjustmentSpeed = 0.05f;

    @EditorRange(min = 0.0f, max = 5.0f)
    private float bloomHighPassThreshold = 0.5f;
    @EditorRange(min = 0.0f, max = 32.0f)
    private float bloomBlurRadius = 12.0f;

    @EditorRange(min = 0.0f, max = 16.0f)
    private float overallBlurRadiusFactor = 0.8f;

    /* HDR */
    private float currentExposure = 2.0f;
    private float currentSceneLuminance = 1.0f;
    private PBO readBackPBOFront, readBackPBOBack, readBackPBOCurrent;

    /* RTs */
    private int rtFullWidth;
    private int rtFullHeight;
    private int rtWidth2, rtHeight2;
    private int rtWidth4, rtHeight4;
    private int rtWidth8, rtHeight8;
    private int rtWidth16, rtHeight16;
    private int rtWidth32, rtHeight32;

    private int overwriteRtWidth = 0;
    private int overwriteRtHeight = 0;

    private String currentlyBoundFboName = "";
    private FBO currentlyBoundFbo = null;
    //private int currentlyBoundTextureId = -1;

    public enum FBOType {
        DEFAULT,
        HDR,
        NO_COLOR
    }

    /* VARIOUS */
    private boolean takeScreenshot = false;
    private int displayListQuad = -1;
    private Config config = CoreRegistry.get(Config.class);

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
        public int depthStencilTextureId = 0;
        public int depthStencilRboId = 0;
        public int normalsTextureId = 0;
        public int lightBufferTextureId = 0;

        public int width = 0;
        public int height = 0;

        public void bind() {
            if (this != currentlyBoundFbo) {
                EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fboId);
                currentlyBoundFbo = this;
            }
        }

        public void unbind() {
            if (currentlyBoundFbo != null) {
                EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
                currentlyBoundFbo = null;
            }
        }

        public void bindDepthTexture() {
            //if (currentlyBoundTextureId != depthStencilTextureId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthStencilTextureId);
            //currentlyBoundTextureId = depthStencilTextureId;
            //}
        }

        public void bindTexture() {
            //if (currentlyBoundTextureId != textureId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
            //currentlyBoundTextureId = textureId;
            //}
        }

        public void bindNormalsTexture() {
            //if (currentlyBoundTextureId != normalsTextureId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalsTextureId);
            //currentlyBoundTextureId = normalsTextureId;
            //}
        }

        public void bindLightBufferTexture() {
            //if (currentlyBoundTextureId != lightBufferTextureId) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, lightBufferTextureId);
            //currentlyBoundTextureId = lightBufferTextureId;
            //}
        }

        public void unbindTexture() {
            //if (currentlyBoundTextureId != 0) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            //currentlyBoundTextureId = 0;
            //}
        }
    }

    private Map<String, FBO> fboLookup = Maps.newHashMap();

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
        readBackPBOFront.init(1, 1);
        readBackPBOBack.init(1, 1);

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
        rtWidth32 = rtHeight16 / 2;
        rtHeight32 = rtWidth16 / 2;

        FBO scene = fboLookup.get("sceneOpaque");
        final boolean recreate = scene == null || (scene.width != rtFullWidth || scene.height != rtFullHeight);

        if (!recreate) {
            return;
        }

        createFBO("sceneOpaque", rtFullWidth, rtFullHeight, FBOType.HDR, true, true, true, true);
        createFBO("sceneOpaquePingPong", rtFullWidth, rtFullHeight, FBOType.HDR, true, true, true, true);

        createFBO("sceneTransparent", rtFullWidth, rtFullHeight, FBOType.HDR);
        attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");

        createFBO("sceneReflected", rtWidth2, rtHeight2, FBOType.DEFAULT, true);

        createFBO("sceneShadowMap", config.getRendering().getShadowMapResolution(), config.getRendering().getShadowMapResolution(), FBOType.NO_COLOR, true, false);

        createFBO("scenePrePost", rtFullWidth, rtFullHeight, FBOType.HDR);
        createFBO("sceneToneMapped", rtFullWidth, rtFullHeight, FBOType.HDR);
        createFBO("sceneFinal", rtFullWidth, rtFullHeight, FBOType.DEFAULT);

        createFBO("sobel", rtFullWidth, rtFullHeight, FBOType.DEFAULT);

        createFBO("ssao", rtFullWidth, rtFullHeight, FBOType.DEFAULT);
        createFBO("ssaoBlurred", rtFullWidth, rtFullHeight, FBOType.DEFAULT);

        createFBO("lightShafts", rtWidth2, rtHeight2, FBOType.DEFAULT);

        createFBO("sceneHighPass", rtFullWidth, rtFullHeight, FBOType.DEFAULT);
        createFBO("sceneBloom0", rtWidth2, rtHeight2, FBOType.DEFAULT);
        createFBO("sceneBloom1", rtWidth4, rtHeight4, FBOType.DEFAULT);
        createFBO("sceneBloom2", rtWidth8, rtHeight8, FBOType.DEFAULT);

        createFBO("sceneBlur0", rtWidth2, rtHeight2, FBOType.DEFAULT);
        createFBO("sceneBlur1", rtWidth2, rtHeight2, FBOType.DEFAULT);

        createFBO("sceneSkyBand0", rtWidth16, rtHeight16, FBOType.DEFAULT);
        createFBO("sceneSkyBand1", rtWidth32, rtHeight32, FBOType.DEFAULT);
    }

    public void deleteFBO(String title) {
        if (fboLookup.containsKey(title)) {
            FBO fbo = fboLookup.get(title);

            EXTFramebufferObject.glDeleteFramebuffersEXT(fbo.fboId);
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthStencilRboId);
            GL11.glDeleteTextures(fbo.normalsTextureId);
            GL11.glDeleteTextures(fbo.depthStencilTextureId);
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

        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, source.depthStencilRboId);
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, source.depthStencilTextureId, 0);

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
        return createFBO(title, width, height, type, depth, normals, lightBuffer, false);
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depthBuffer, boolean normalBuffer, boolean lightBuffer, boolean stencilBuffer) {
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

        if (normalBuffer) {
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

        if (depthBuffer) {
            fbo.depthStencilTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            if (!stencilBuffer) {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_UNSIGNED_INT, (java.nio.ByteBuffer) null);
            } else {
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height, 0, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, EXTPackedDepthStencil.GL_UNSIGNED_INT_24_8_EXT, (java.nio.ByteBuffer) null);
            }

            fbo.depthStencilRboId = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthStencilRboId);

            if (!stencilBuffer) {
                EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
            } else {
                EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH24_STENCIL8_EXT, width, height);
            }

            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);

            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo.depthStencilRboId);
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);

            if (stencilBuffer) {
                EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo.depthStencilTextureId, 0);
            }
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        if (type != FBOType.NO_COLOR) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        }
        if (normalBuffer) {
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

        fboLookup.put(title, fbo);
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

            currentSceneLuminance = 0.2126f * (pixels.get(2) & 0xFF) / 255.f + 0.7152f * (pixels.get(1) & 0xFF) / 255.f + 0.0722f * (pixels.get(0) & 0xFF) / 255.f;

            float targetExposure = hdrMaxExposure;

            if (currentSceneLuminance > 0) {
                targetExposure = hdrTargetLuminance / currentSceneLuminance;
            }

            float maxExposure = hdrMaxExposure;

            if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
                maxExposure = hdrMaxExposureNight;
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < hdrMinExposure) {
                targetExposure = hdrMinExposure;
            }

            currentExposure = (float) TeraMath.lerp(currentExposure, targetExposure, hdrExposureAdjustmentSpeed);

        } else {
            if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
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
        bindFbo("sceneTransparent");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        unbindFbo("sceneTransparent");
    }

    public void beginRenderSceneOpaque() {
        bindFbo("sceneOpaque");
        setRenderBufferMask(true, true, true);
    }

    public void endRenderSceneOpaque() {
        setRenderBufferMask(true, true, true);
        unbindFbo("sceneOpaque");
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
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneShadowMap() {
        FBO shadowMap = getFBO("sceneShadowMap");

        if (shadowMap == null) {
            return;
        }

        shadowMap.bind();

        glViewport(0, 0, shadowMap.width, shadowMap.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderSceneShadowMap() {
        unbindFbo("sceneShadowMap");
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    public void beginRenderSceneSky() {
        setRenderBufferMask(true, false, false);
    }

    public void endRenderSceneSky() {
        setRenderBufferMask(true, true, true);

        generateSkyBand(0);
        generateSkyBand(1);

        bindFbo("sceneOpaque");
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
            generateBlurredSSAO();
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

        for (int i = 0; i < 3; i++) {
            if (config.getRendering().isBloom()) {
                generateBloom(i);
            }
        }

        for (int i = 0; i < 2; i++) {
            if (config.getRendering().getBlurIntensity() != 0) {
                generateBlur(i);
            }
        }

        if (stereoRenderState == StereoRenderState.OCULUS_LEFT_EYE
                || stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE
                || (stereoRenderState == StereoRenderState.MONO && takeScreenshot)) {

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
        Material material;

        if (config.getRendering().getDebug().isEnabled()) {
            material = Assets.getMaterial("engine:debug");
        } else {
            material = Assets.getMaterial("engine:post");
        }

        material.enable();

        bindFbo("sceneFinal");

        if (stereoRenderState == StereoRenderState.MONO || stereoRenderState == StereoRenderState.OCULUS_LEFT_EYE) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        switch (stereoRenderState) {
            case MONO:
                renderFullscreenQuad(0, 0, rtFullWidth, rtFullHeight);
                break;
            case OCULUS_LEFT_EYE:
                renderFullscreenQuad(0, 0, rtFullWidth / 2, rtFullHeight);
                break;
            case OCULUS_RIGHT_EYE:
                renderFullscreenQuad(rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight);
                break;
        }

        unbindFbo("sceneFinal");
    }

    private void updateOcShaderParametersForVP(Material program, int vpX, int vpY, int vpWidth, int vpHeight, StereoRenderState stereoRenderState) {
        float w = (float) vpWidth / rtFullWidth;
        float h = (float) vpHeight / rtFullHeight;
        float x = (float) vpX / rtFullWidth;
        float y = (float) vpY / rtFullHeight;

        float as = (float) vpWidth / vpHeight;

        program.setFloat4("ocHmdWarpParam", OculusVrHelper.getDistortionParams()[0], OculusVrHelper.getDistortionParams()[1], OculusVrHelper.getDistortionParams()[2], OculusVrHelper.getDistortionParams()[3], true);

        float ocLensCenter = (stereoRenderState == StereoRenderState.OCULUS_RIGHT_EYE) ? -1.0f * OculusVrHelper.getLensViewportShift() : OculusVrHelper.getLensViewportShift();

        program.setFloat2("ocLensCenter", x + (w + ocLensCenter * 0.5f) * 0.5f, y + h * 0.5f, true);
        program.setFloat2("ocScreenCenter", x + w * 0.5f, y + h * 0.5f, true);

        float scaleFactor = 1.0f / OculusVrHelper.getScaleFactor();

        program.setFloat2("ocScale", (w / 2) * scaleFactor, (h / 2) * scaleFactor * as, true);
        program.setFloat2("ocScaleIn", (2 / w), (2 / h) / as, true);
    }

    private void renderFinalScene() {

        Material material;

        if (config.getRendering().isOculusVrSupport()) {
            material = Assets.getMaterial("engine:ocDistortion");
            material.enable();

            updateOcShaderParametersForVP(material, 0, 0, rtFullWidth / 2, rtFullHeight, StereoRenderState.OCULUS_LEFT_EYE);
        } else {
            if (config.getRendering().getDebug().isEnabled()) {
                material = Assets.getMaterial("engine:debug");
            } else {
                material = Assets.getMaterial("engine:post");
            }

            material.enable();
        }

        renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), org.lwjgl.opengl.Display.getHeight());

        if (config.getRendering().isOculusVrSupport()) {
            updateOcShaderParametersForVP(material, rtFullWidth / 2, 0, rtFullWidth / 2, rtFullHeight, StereoRenderState.OCULUS_RIGHT_EYE);

            renderFullscreenQuad(0, 0, org.lwjgl.opengl.Display.getWidth(), Display.getHeight());
        }
    }

    private void generateCombinedScene() {
        Assets.getMaterial("engine:combine").enable();

        bindFbo("sceneOpaquePingPong");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneOpaquePingPong");

        flipPingPongFbo("sceneOpaque");
        attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");
    }

    private void applyLightBufferPass(String target) {
        Material program = Assets.getMaterial("engine:lightBufferPass");
        program.enable();

        DefaultRenderingProcess.FBO targetFbo = getFBO(target);

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
            attachDepthBufferToFbo("sceneOpaque", "sceneTransparent");
        }
    }

    private void generateSkyBand(int id) {
        FBO skyBand = getFBO("sceneSkyBand" + id);

        if (skyBand == null) {
            return;
        }

        skyBand.bind();
        setRenderBufferMask(true, false, false);

        Material material = Assets.getMaterial("engine:blur");

        material.enable();
        material.setFloat("radius", 8.0f, true);
        material.setFloat2("texelSize", 1.0f / skyBand.width, 1.0f / skyBand.height, true);

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
        Assets.getMaterial("engine:hdr").enable();

        bindFbo("sceneToneMapped");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("sceneToneMapped");
    }

    private void generateLightShafts() {
        Assets.getMaterial("engine:lightshaft").enable();

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
        Material ssaoShader = Assets.getMaterial("engine:ssao");
        ssaoShader.enable();

        FBO ssao = getFBO("ssao");

        if (ssao == null) {
            return;
        }

        ssaoShader.setFloat2("texelSize", 1.0f / ssao.width, 1.0f / ssao.height, true);
        ssaoShader.setFloat2("noiseTexelSize", 1.0f / 4.0f, 1.0f / 4.0f, true);

        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        ssao.unbind();
        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateSobel() {
        Assets.getMaterial("engine:sobel").enable();

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

    private void generateBlurredSSAO() {
        Material shader = Assets.getMaterial("engine:ssaoBlur");
        shader.enable();

        FBO ssao = getFBO("ssaoBlurred");

        if (ssao == null) {
            return;
        }

        shader.setFloat2("texelSize", 1.0f / ssao.width, 1.0f / ssao.height, true);


        ssao.bind();

        glViewport(0, 0, ssao.width, ssao.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        bindFboTexture("ssao");

        renderFullscreenQuad();

        ssao.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generatePrePost() {
        Assets.getMaterial("engine:prePost").enable();

        bindFbo("scenePrePost");

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        unbindFbo("scenePrePost");
    }

    private void generateHighPass() {
        Material program = Assets.getMaterial("engine:highp");
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

        GL13.glActiveTexture(GL13.GL_TEXTURE0 + texId);
        sceneOpaque.bindDepthTexture();
        program.setInt("texDepth", texId++);

        glViewport(0, 0, highPass.width, highPass.height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullscreenQuad();

        highPass.unbind();

        glViewport(0, 0, rtFullWidth, rtFullHeight);
    }

    private void generateBlur(int id) {
        Material material = Assets.getMaterial("engine:blur");
        material.enable();

        material.setFloat("radius", overallBlurRadiusFactor * config.getRendering().getBlurRadius(), true);

        FBO blur = getFBO("sceneBlur" + id);

        if (blur == null) {
            return;
        }

        material.setFloat2("texelSize", 1.0f / blur.width, 1.0f / blur.height, true);

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
        Material shader = Assets.getMaterial("engine:blur");

        shader.enable();
        shader.setFloat("radius", bloomBlurRadius, true);

        FBO bloom = getFBO("sceneBloom" + id);

        if (bloom == null) {
            return;
        }

        shader.setFloat2("texelSize", 1.0f / bloom.width, 1.0f / bloom.height, true);

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
        Material shader = Assets.getMaterial("engine:down");
        shader.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = (int) java.lang.Math.pow(2, i + 1);

            int size = (int) java.lang.Math.pow(2, i);
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

        overwriteRtWidth = 1920 * 2;
        overwriteRtHeight = 1080 * 2;
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
                Path path = PathManager.getInstance().getScreenshotPath().resolve(fileName);
                BufferedImage image = new BufferedImage(fboSceneFinal.width, fboSceneFinal.height, BufferedImage.TYPE_INT_RGB);

                for (int x = 0; x < fboSceneFinal.width; x++) {
                    for (int y = 0; y < fboSceneFinal.height; y++) {
                        int i = (x + fboSceneFinal.width * y) * 4;
                        int r = buffer.get(i) & 0xFF;
                        int g = buffer.get(i + 1) & 0xFF;
                        int b = buffer.get(i + 2) & 0xFF;
                        image.setRGB(x, fboSceneFinal.height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
                    }
                }

                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
                    ImageIO.write(image, "png", out);
                    logger.info("Screenshot '" + fileName + "' saved! ");
                } catch (IOException e) {
                    logger.warn("Failed to save screenshot!", e);
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
        FBO fbo = fboLookup.get(title);

        if (fbo == null) {
            logger.error("Failed to retrieve FBO '" + title + "'!");
        }

        return fbo;
    }

    public boolean bindFbo(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
            fbo.bind();
            currentlyBoundFboName = title;
            return true;
        }

        logger.error("Failed to bind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean unbindFbo(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
            fbo.unbind();
            currentlyBoundFboName = "";
            return true;
        }

        logger.error("Failed to unbind FBO since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboTexture(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
            fbo.bindTexture();
            return true;
        }

        logger.error("Failed to bind FBO texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboDepthTexture(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
            fbo.bindDepthTexture();
            return true;
        }

        logger.error("Failed to bind FBO depth texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboNormalsTexture(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
            fbo.bindNormalsTexture();
            return true;
        }

        logger.error("Failed to bind FBO normals texture since the requested FBO could not be found!");
        return false;
    }

    public boolean bindFboLightBufferTexture(String title) {
        FBO fbo;

        if ((fbo = fboLookup.get(title)) != null) {
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

}
