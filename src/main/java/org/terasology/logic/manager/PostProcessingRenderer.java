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
import org.lwjgl.opengl.ARBHalfFloatPixel;
import org.lwjgl.opengl.ARBTextureFloat;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.editor.properties.IPropertyProvider;
import org.terasology.editor.properties.Property;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3i;
import static org.lwjgl.opengl.GL11.glViewport;

/**
 * Responsible for applying and rendering various shader based
 * post processing effects.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PostProcessingRenderer implements IPropertyProvider {

    private static final Logger logger = LoggerFactory.getLogger(PostProcessingRenderer.class);

    private static PostProcessingRenderer _instance = null;

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

    private Config config = CoreRegistry.get(Config.class);

    public enum FBOType {
        DEFAULT,
        HDR
    }

    public class FBO {
        public int _fboId = 0;
        public int _textureId = 0;
        public int _depthTextureId = 0;
        public int _depthRboId = 0;
        public int _normalsTextureId = 0;

        public int _width = 0;
        public int _height = 0;

        public void bind() {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, _fboId);
        }

        public void unbind() {
            EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        }

        public void bindDepthTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, _depthTextureId);
        }

        public void bindTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, _textureId);
        }

        public void bindNormalsTexture() {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, _normalsTextureId);
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
    public static PostProcessingRenderer getInstance() {
        if (_instance == null) {
            _instance = new PostProcessingRenderer();
        }

        return _instance;
    }

    public PostProcessingRenderer() {
        initialize();
    }

    public void initialize() {
        createOrUpdateFullscreenFbos();

        createFBO("scene16", 16, 16, FBOType.DEFAULT, false, false);
        createFBO("scene8", 8, 8, FBOType.DEFAULT, false, false);
        createFBO("scene4", 4, 4, FBOType.DEFAULT, false, false);
        createFBO("scene2", 2, 2, FBOType.DEFAULT, false, false);
        createFBO("scene1", 1, 1, FBOType.DEFAULT, false, false);
    }


    /**
     * Initially creates the scene FBO and updates it according to the size of the viewport.
     */
    private void createOrUpdateFullscreenFbos() {
        FBO scene = getFBO("sceneOpaque");
        boolean recreate = scene == null || (scene._width != Display.getWidth() || scene._height != Display.getHeight());

        if (!recreate)
            return;

        final int halfWidth = Display.getWidth() / 2;
        final int halfHeight = Display.getHeight() / 2;
        final int quarterWidth = halfWidth / 2;
        final int quarterHeight = halfHeight / 2;
        final int halfQuarterWidth = quarterWidth / 2;
        final int halfQuarterHeight = quarterHeight / 2;

        createFBO("sceneOpaque", Display.getWidth(), Display.getHeight(), FBOType.HDR, true, true);
        createFBO("sceneTransparent", Display.getWidth(), Display.getHeight(), FBOType.HDR, true, true);

        createFBO("scenePrePost", Display.getWidth(), Display.getHeight(), FBOType.HDR, false, false);
        createFBO("sceneToneMapped", Display.getWidth(), Display.getHeight(), FBOType.HDR, false, false);

        createFBO("sobel", Display.getWidth(), Display.getHeight(), FBOType.DEFAULT, false, false);

        createFBO("ssao", halfWidth, halfHeight, FBOType.DEFAULT, false, false);
        createFBO("ssaoBlurred0", halfWidth, halfHeight, FBOType.DEFAULT, false, false);
        createFBO("ssaoBlurred1", halfWidth, halfHeight, FBOType.DEFAULT, false, false);

        createFBO("lightShafts", halfWidth, halfHeight, FBOType.DEFAULT, false, false);

        createFBO("sceneReflected", halfWidth, halfHeight, FBOType.HDR, true, false);

        createFBO("sceneHighPass", halfQuarterWidth, halfQuarterHeight, FBOType.DEFAULT, false, false);
        createFBO("sceneBloom0", halfQuarterWidth, halfQuarterHeight, FBOType.DEFAULT, false, false);
        createFBO("sceneBloom1", halfQuarterWidth, halfQuarterHeight, FBOType.DEFAULT, false, false);

        createFBO("sceneBlur0", halfWidth, halfHeight, FBOType.DEFAULT, false, false);
        createFBO("sceneBlur1", halfWidth, halfHeight, FBOType.DEFAULT, false, false);
    }

    public void deleteFBO(String title) {
        if (_FBOs.containsKey(title)) {
            FBO fbo = _FBOs.get(title);

            EXTFramebufferObject.glDeleteFramebuffersEXT(fbo._fboId);
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo._depthRboId);
            GL11.glDeleteTextures(fbo._normalsTextureId);
            GL11.glDeleteTextures(fbo._depthTextureId);
            GL11.glDeleteTextures(fbo._textureId);
        }
    }

    public FBO createFBO(String title, int width, int height, FBOType type, boolean depth, boolean normals) {
        // Make sure to delete the existing FBO before creating a new one
        deleteFBO(title);

        // Create a new FBO object
        FBO fbo = new FBO();
        fbo._width = width;
        fbo._height = height;

        // Create the color target texture
        fbo._textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo._textureId);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        if (type == FBOType.HDR) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        }

        if (depth) {
            // Generate the depth texture
            fbo._depthTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo._depthTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL14.GL_DEPTH_COMPONENT24, width, height, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (java.nio.ByteBuffer) null);

            // Create depth render buffer object
            fbo._depthRboId = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo._depthRboId);
            EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, width, height);
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);
        }

        if (normals) {
            // Generate the normals texture
            fbo._normalsTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo._normalsTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Create the FBO
        fbo._fboId = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo._fboId);

        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, fbo._textureId, 0);

        if (depth) {
            // Generate the depth render buffer and depth map texture
            EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, fbo._depthRboId);
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, GL11.GL_TEXTURE_2D, fbo._depthTextureId, 0);
        }

        if (normals) {
            EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT, GL11.GL_TEXTURE_2D, fbo._normalsTextureId, 0);
        }

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);
        bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT);
        if (normals) {
            bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT1_EXT);
        }
        bufferIds.flip();
        GL20.glDrawBuffers(bufferIds);

        int framebuffer = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        switch (framebuffer) {
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
            default:
                throw new RuntimeException("Unexpected reply from glCheckFramebufferStatusEXT: " + framebuffer);
        }

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        _FBOs.put(title, fbo);
        return fbo;
    }

    private void updateExposure() {
        if (config.getRendering().isEyeAdapting()) {
            ByteBuffer pixels = BufferUtils.createByteBuffer(4);
            FBO scene = PostProcessingRenderer.getInstance().getFBO("scene1");

            scene.bind();
            glReadPixels(0, 0, 1, 1, GL12.GL_BGRA, GL11.GL_BYTE, pixels);
            scene.unbind();

            currentSceneLuminance = 0.2126f * pixels.get(2) / 255.f + 0.7152f * pixels.get(1) / 255.f + 0.0722f * pixels.get(0) / 255.f;

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

        glViewport(0, 0, reflected._width, reflected._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderReflectedScene() {
        getFBO("sceneReflected").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    /**
     * Renders the final scene to a quad and displays it. The FBO gets automatically rescaled if the size
     * of the view port changes.
     */
    public void renderScene() {
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

        if (config.getRendering().isLightShafts()) {
            generateLightShafts();
        }

        generatePrePost();

        if (config.getRendering().isEyeAdapting()) {
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

        renderFinalScene();
    }

    private void renderFinalScene() {
        ShaderProgram shaderPost = ShaderManager.getInstance().getShaderProgram("post");
        shaderPost.enable();

        renderFullQuad();
    }

    private void generateToneMappedScene() {
        ShaderManager.getInstance().enableShader("hdr");

        PostProcessingRenderer.getInstance().getFBO("sceneToneMapped").bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneToneMapped").unbind();
    }

    private void generateLightShafts() {
        ShaderManager.getInstance().enableShader("lightshaft");

        FBO lightshaft = PostProcessingRenderer.getInstance().getFBO("lightShafts");
        lightshaft.bind();

        glViewport(0, 0, lightshaft._width, lightshaft._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("lightShafts").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateSSAO() {
        ShaderManager.getInstance().enableShader("ssao");

        FBO ssao = PostProcessingRenderer.getInstance().getFBO("ssao");
        ssao.bind();

        glViewport(0, 0, ssao._width, ssao._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("ssao").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateSobel() {
        ShaderManager.getInstance().enableShader("sobel");

        FBO sobel = PostProcessingRenderer.getInstance().getFBO("sobel");
        sobel.bind();

        glViewport(0, 0, sobel._width, sobel._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sobel").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlurredSSAO(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", (Float) ssaoBlurRadius.getValue());

        FBO ssao = PostProcessingRenderer.getInstance().getFBO("ssaoBlurred" + id);
        ssao.bind();

        glViewport(0, 0, ssao._width, ssao._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            PostProcessingRenderer.getInstance().getFBO("ssao").bindTexture();
        } else {
            PostProcessingRenderer.getInstance().getFBO("ssaoBlurred" + (id - 1)).bindTexture();
        }

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("ssaoBlurred" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generatePrePost() {
        ShaderManager.getInstance().enableShader("prePost");

        PostProcessingRenderer.getInstance().getFBO("scenePrePost").bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("scenePrePost").unbind();
    }

    private void generateHighPass() {
        ShaderProgram program = ShaderManager.getInstance().getShaderProgram("highp");
        program.setFloat("highPassThreshold", (Float) bloomHighPassThreshold.getValue());
        program.enable();

        FBO highPass = PostProcessingRenderer.getInstance().getFBO("sceneHighPass");
        highPass.bind();

        glViewport(0, 0, highPass._width, highPass._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        PostProcessingRenderer.getInstance().getFBO("sceneToneMapped").bindTexture();

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneHighPass").unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlur(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();

        shader.setFloat("radius", (Float) overallBlurFactor.getValue() * config.getRendering().getBlurRadius());

        FBO blur = PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id);
        blur.bind();

        glViewport(0, 0, blur._width, blur._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            PostProcessingRenderer.getInstance().getFBO("sceneToneMapped").bindTexture();
        } else {
            PostProcessingRenderer.getInstance().getFBO("sceneBlur" + (id - 1)).bindTexture();
        }

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBloom(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", 32.0f);

        FBO bloom = PostProcessingRenderer.getInstance().getFBO("sceneBloom" + id);
        bloom.bind();

        glViewport(0, 0, bloom._width, bloom._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            PostProcessingRenderer.getInstance().getFBO("sceneHighPass").bindTexture();
        } else {
            PostProcessingRenderer.getInstance().getFBO("sceneBloom" + (id - 1)).bindTexture();
        }

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneBloom" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateDownsampledScene() {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("down");
        shader.enable();

        for (int i = 4; i >= 0; i--) {
            int sizePrev = (int) java.lang.Math.pow(2, i + 1);

            int size = (int) java.lang.Math.pow(2, i);
            shader.setFloat("size", size);

            PostProcessingRenderer.getInstance().getFBO("scene" + size).bind();
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 4) {
                PostProcessingRenderer.getInstance().getFBO("scenePrePost").bindTexture();
            } else {
                PostProcessingRenderer.getInstance().getFBO("scene" + sizePrev).bindTexture();
            }

            renderFullQuad();

            PostProcessingRenderer.getInstance().getFBO("scene" + size).unbind();

        }

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void renderFullQuad() {
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
