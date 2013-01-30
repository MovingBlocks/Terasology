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

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.shader.ShaderProgram;
import org.terasology.rendering.world.WorldRenderer;

import javax.swing.*;

import static org.lwjgl.opengl.GL11.*;

/**
 * Responsible for applying and rendering various shader based
 * post processing effects.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PostProcessingRenderer {

    public static final float DEFAULT_EXPOSURE = 2.5f;
    public static final float MAX_EXPOSURE = 6.0f;
    public static final float MAX_EXPOSURE_NIGHT = 0.5f;
    public static final float MIN_EXPOSURE = 0.5f;
    public static final float TARGET_LUMINANCE = 1.5f;
    public static final float ADJUSTMENT_SPEED = 0.0075f;

    private static final Logger logger = LoggerFactory.getLogger(PostProcessingRenderer.class);

    private static PostProcessingRenderer _instance = null;
    private float _exposure = 2.0f;
    private float _sceneLuminance = 1.0f;
    private int _displayListQuad = -1;

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

            int bufferCount = 0;

            if (_textureId > 0) {
                bufferCount++;
            }

            if (_normalsTextureId > 0) {
                bufferCount++;
            }

            IntBuffer bufferIds = BufferUtils.createIntBuffer(bufferCount);
            for (int i=0; i<bufferCount; ++i) {
                bufferIds.put(EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT+i);
            }
            bufferIds.flip();

            GL20.glDrawBuffers(bufferIds);
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

        createFBO("scene16", 16, 16, false, false, false);
        createFBO("scene8", 8, 8, false, false, false);
        createFBO("scene4", 4, 4, false, false, false);
        createFBO("scene2", 2, 2, false, false, false);
        createFBO("scene1", 1, 1, false, false, false);
    }

    public void deleteFBO(String title) {
        if (_FBOs.containsKey(title)) {
            FBO fbo = _FBOs.get(title);

            EXTFramebufferObject.glDeleteFramebuffersEXT(fbo._fboId);
            EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo._depthRboId);
            GL11.glDeleteTextures(fbo._depthTextureId);
            GL11.glDeleteTextures(fbo._textureId);
        }
    }

    public FBO createFBO(String title, int width, int height, boolean hdr, boolean depth, boolean normals) {
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
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_NEAREST );
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        if (hdr) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
        }
        else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);
        }

        if (depth) {
            // Generate the depth texture
            fbo._depthTextureId = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo._depthTextureId);

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
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

            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
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

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        _FBOs.put(title, fbo);
        return fbo;
    }

    private void updateExposure() {
        if (Config.getInstance().isEyeAdaption()) {

            ByteBuffer pixels = BufferUtils.createByteBuffer(4);
            FBO scene = PostProcessingRenderer.getInstance().getFBO("scene1");

            scene.bind();
            glReadPixels(0, 0, 1, 1, GL12.GL_BGRA, GL11.GL_BYTE, pixels);
            scene.unbind();

            _sceneLuminance = 0.2126f * pixels.get(2) / 255.f + 0.7152f * pixels.get(1) / 255.f + 0.0722f * pixels.get(0) / 255.f;

            float targetExposure = MIN_EXPOSURE;

            if (_sceneLuminance > 0) {
                targetExposure = TARGET_LUMINANCE / _sceneLuminance;
            }

            float maxExposure = MAX_EXPOSURE;

            if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
                maxExposure = MAX_EXPOSURE_NIGHT;
            }

            if (targetExposure > maxExposure) {
                targetExposure = maxExposure;
            } else if (targetExposure < MIN_EXPOSURE) {
                targetExposure = MIN_EXPOSURE;
            }

            _exposure = (float) TeraMath.lerp(_exposure, targetExposure, ADJUSTMENT_SPEED);

      } else {
        if (CoreRegistry.get(WorldRenderer.class).getSkysphere().getDaylight() == 0.0) {
            _exposure = MAX_EXPOSURE_NIGHT;
        } else {
            _exposure = DEFAULT_EXPOSURE;
        }
      }
    }

    public void beginRenderScene() {
        getFBO("scene").bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void beginRenderReflectedScene() {
        FBO reflected = getFBO("sceneReflected");
        reflected.bind();

        glViewport(0, 0, reflected._width, reflected._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderScene() {
        getFBO("scene").unbind();
    }

    public void endRenderReflectedScene() {
        getFBO("sceneReflected").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    /**
     * Renders the final scene to a quad and displays it. The FBO gets automatically rescaled if the size
     * of the viewport changes.
     */
    public void renderScene() {
        if (Config.getInstance().isEnablePostProcessingEffects()) {
            generateDownsampledScene();

            if (Config.getInstance().isSSAO()) {
                generateSSAO();
                generateBlurredSSAO();
            }

            screenSpaceCombine();

            generateTonemappedScene();

            generateHighPass();

            for (int i = 0; i < 2; i++) {
                if (Config.getInstance().isBloom()) {
                    generateBloom(i);
                }
                if (Config.getInstance().getBlurIntensity() != 0) {
                    generateBlur(i);
                }
            }

            renderFinalScene();
            updateExposure();
        } else {
            PostProcessingRenderer.FBO scene = PostProcessingRenderer.getInstance().getFBO("scene");

            ShaderManager.getInstance().enableDefaultTextured();
            scene.bindTexture();

            renderFullQuad();
        }

        createOrUpdateFullscreenFbos();
    }

    private void renderFinalScene() {
        ShaderProgram shaderPost = ShaderManager.getInstance().getShaderProgram("post");
        shaderPost.enable();

        renderFullQuad();
    }

    private void generateTonemappedScene() {
        ShaderManager.getInstance().enableShader("hdr");

        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").unbind();
    }

    public void generateSSAO() {
        ShaderManager.getInstance().enableShader("ssao");

        FBO ssao = PostProcessingRenderer.getInstance().getFBO("ssao");
        ssao.bind();

        glViewport(0, 0, ssao._width, ssao._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("ssao").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void generateBlurredSSAO() {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");
        shader.enable();

        shader.setFloat("radius", 4.0f);

        FBO ssaoBlurred = PostProcessingRenderer.getInstance().getFBO("ssaoBlurred");
        ssaoBlurred.bind();

        glViewport(0, 0, ssaoBlurred._width, ssaoBlurred._height);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        PostProcessingRenderer.getInstance().getFBO("ssao").bindTexture();
        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("ssaoBlurred").unbind();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    public void screenSpaceCombine() {
        ShaderManager.getInstance().enableShader("screenCombine");

        PostProcessingRenderer.getInstance().getFBO("sceneCombined").bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneCombined").unbind();
    }

    /**
     * Initially creates the scene FBO and updates it according to the size of the viewport.
     */
    private void createOrUpdateFullscreenFbos() {
        FBO scene = getFBO("scene");
        boolean recreate = scene == null || (scene != null && (scene._width != Display.getWidth() || scene._height != Display.getHeight()));

        if (!recreate)
            return;

        createFBO("scene", Display.getWidth(), Display.getHeight(), true, true, true);
        createFBO("sceneCombined", Display.getWidth(), Display.getHeight(), true, false, false);
        createFBO("sceneTonemapped", Display.getWidth(), Display.getHeight(), true, false, false);

        final int halfWidth = Display.getWidth() / 2;
        final int halfHeight = Display.getHeight() / 2;
        final int quarterWidth = halfWidth / 2;
        final int quarterHeight = halfHeight / 2;
        final int halfQuarterWidth = quarterWidth / 2;
        final int halfQuarterHeight = quarterHeight / 2;

        createFBO("sceneReflected", halfWidth, halfHeight, true, true, false);

        createFBO("sceneHighPass", halfQuarterWidth, halfQuarterHeight, false, false, false);
        createFBO("sceneBloom0", halfQuarterWidth, halfQuarterHeight, false, false, false);
        createFBO("sceneBloom1", halfQuarterWidth, halfQuarterHeight, false, false, false);

        createFBO("sceneBlur0", quarterWidth, quarterHeight, false, false, false);
        createFBO("sceneBlur1", quarterWidth, quarterHeight, false, false, false);

        createFBO("ssao", halfWidth, halfHeight, false, false, false);
        createFBO("ssaoBlurred", halfWidth, halfHeight, false, false, false);
    }

    private void generateHighPass() {
        ShaderProgram program = ShaderManager.getInstance().getShaderProgram("highp");
        program.setFloat("highPassThreshold", 1.05f);
        program.enable();

        FBO highPass = PostProcessingRenderer.getInstance().getFBO("sceneHighPass");
        highPass.bind();

        glViewport(0, 0, highPass._width, highPass._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneHighPass").unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlur(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();

        shader.setFloat("radius", 1.5f * Config.getInstance().getBlurIntensity());

        FBO blur = PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id);
        blur.bind();

        glViewport(0, 0, blur._width, blur._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();
        }
        else {
            PostProcessingRenderer.getInstance().getFBO("sceneBlur" + (id - 1)).bindTexture();
        }

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBloom(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", 16.0f);

        FBO bloom = PostProcessingRenderer.getInstance().getFBO("sceneBloom" + id);
        bloom.bind();

        glViewport(0, 0, bloom._width, bloom._height);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0) {
            PostProcessingRenderer.getInstance().getFBO("sceneHighPass").bindTexture();
        }
        else {
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
                PostProcessingRenderer.getInstance().getFBO("scene").bindTexture();
            }
            else {
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
        if (_displayListQuad == -1) {
            _displayListQuad = glGenLists(1);

            glNewList(_displayListQuad, GL11.GL_COMPILE);

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

        glCallList(_displayListQuad);
    }

    public float getExposure() {
        return _exposure;
    }

    public FBO getFBO(String title) {
        return _FBOs.get(title);
    }
}
