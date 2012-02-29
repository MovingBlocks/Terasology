/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
import org.terasology.game.Terasology;
import org.terasology.math.TeraMath;
import org.terasology.rendering.shader.ShaderProgram;

import java.nio.FloatBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PostProcessingRenderer {

    public static final boolean EFFECTS_ENABLED = Config.getInstance().isEnablePostProcessingEffects();
    public static final float MAX_EXPOSURE = 4.0f;
    public static final float MAX_EXPOSURE_NIGHT = 2.0f;
    public static final float MIN_EXPOSURE = 1.0f;
    public static final float TARGET_LUMINANCE = 0.5f;
    public static final float ADJUSTMENT_SPEED = 0.025f;

    private static PostProcessingRenderer _instance = null;
    private float _exposure;
    private int _displayListQuad = -1;

    private boolean _extensionsAvailable = false;

    public class FBO {
        public int _fboId = 0;
        public int _textureId = 0;
        public int _depthTextureId = 0;
        public int _depthRboId = 0;

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
        _extensionsAvailable = GLContext.getCapabilities().GL_ARB_framebuffer_object;

        if (_extensionsAvailable) {
            createOrUpdateFullscreenFbos();

            if (EFFECTS_ENABLED) {
                createFBO("sceneHighPass", 256, 256, false, false);
                createFBO("sceneBloom0", 256, 256, false, false);
                createFBO("sceneBloom1", 256, 256, false, false);
                createFBO("sceneBloom2", 256, 256, false, false);

                createFBO("sceneBlur0", 1024, 1024, false, false);
                createFBO("sceneBlur1", 1024, 1024, false, false);
                createFBO("sceneBlur2", 1024, 1024, false, false);

                createFBO("scene32", 32, 32, false, false);
                createFBO("scene16", 16, 16, false, false);
                createFBO("scene8", 8, 8, false, false);
                createFBO("scene4", 4, 4, false, false);
                createFBO("scene2", 2, 2, false, false);
                createFBO("scene1", 1, 1, false, false);
            }
        }
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

    public FBO createFBO(String title, int width, int height, boolean hdr, boolean depth) {
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

        if (hdr)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, ARBTextureFloat.GL_RGBA16F_ARB, width, height, 0, GL11.GL_RGBA, ARBHalfFloatPixel.GL_HALF_FLOAT_ARB, (java.nio.ByteBuffer) null);
        else
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

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

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        _FBOs.put(title, fbo);
        return fbo;
    }

    private void updateExposure() {
        FloatBuffer pixels = BufferUtils.createFloatBuffer(4);
        FBO scene = PostProcessingRenderer.getInstance().getFBO("scene1");

        scene.bindTexture();
        glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_FLOAT, pixels);
        scene.unbindTexture();

        float lum = 0.2126f * pixels.get(0) + 0.7152f * pixels.get(1) + 0.0722f * pixels.get(2);

        if (lum > 0.0f) // No division by zero
            _exposure = (float) TeraMath.lerp(_exposure, TARGET_LUMINANCE / lum, ADJUSTMENT_SPEED);

        float maxExposure = MAX_EXPOSURE;

        if (Terasology.getInstance().getActiveWorldRenderer().getSkysphere().getDaylight() == 0.0)
            maxExposure = MAX_EXPOSURE_NIGHT;

        if (_exposure > maxExposure)
            _exposure = maxExposure;
        if (_exposure < MIN_EXPOSURE)
            _exposure = MIN_EXPOSURE;
    }

    public void beginRenderScene() {
        if (!_extensionsAvailable)
            return;

        getFBO("scene").bind();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void endRenderScene() {
        if (!_extensionsAvailable)
            return;

        getFBO("scene").unbind();
    }

    /**
     * Renders the final scene to a quad and displays it. The FBO gets automatically rescaled if the size
     * of the viewport changes.
     */
    public void renderScene() {
        if (!_extensionsAvailable)
            return;

        if (EFFECTS_ENABLED) {
            generateDownsampledScene();
            updateExposure();

            generateTonemappedScene();

            for (int i = 0; i < 3; i++) {
                generateBloom(i);
                generateBlur(i);
            }

            generateHighPass();
            renderFinalScene();
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

    /**
     * Initially creates the scene FBO and updates it according to the size of the viewport.
     */
    private void createOrUpdateFullscreenFbos() {
        if (!_FBOs.containsKey("scene")) {
            createFBO("scene", Display.getWidth(), Display.getHeight(), true, true);
            createFBO("sceneTonemapped", Display.getWidth(), Display.getHeight(), true, false);
        } else {
            FBO scene = getFBO("scene");

            if (scene._width != Display.getWidth() || scene._height != Display.getHeight()) {
                createFBO("scene", Display.getWidth(), Display.getHeight(), true, true);
                createFBO("sceneTonemapped", Display.getWidth(), Display.getHeight(), true, false);
            }
        }
    }

    private void generateHighPass() {
        ShaderManager.getInstance().enableShader("highp");

        PostProcessingRenderer.getInstance().getFBO("sceneHighPass").bind();
        glViewport(0, 0, 256, 256);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneHighPass").unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBlur(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", 3.0f);

        PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id).bind();
        glViewport(0, 0, 1024, 1024);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0)
            PostProcessingRenderer.getInstance().getFBO("sceneTonemapped").bindTexture();
        else
            PostProcessingRenderer.getInstance().getFBO("sceneBlur" + (id - 1)).bindTexture();

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneBlur" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBloom(int id) {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("blur");

        shader.enable();
        shader.setFloat("radius", 16.0f);

        PostProcessingRenderer.getInstance().getFBO("sceneBloom" + id).bind();
        glViewport(0, 0, 256, 256);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        if (id == 0)
            PostProcessingRenderer.getInstance().getFBO("sceneHighPass").bindTexture();
        else
            PostProcessingRenderer.getInstance().getFBO("sceneBloom" + (id - 1)).bindTexture();

        renderFullQuad();

        PostProcessingRenderer.getInstance().getFBO("sceneBloom" + id).unbind();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateDownsampledScene() {
        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("down");
        shader.enable();

        for (int i = 5; i >= 0; i--) {
            int sizePrev = (int) java.lang.Math.pow(2, i + 1);

            int size = (int) java.lang.Math.pow(2, i);
            shader.setFloat("size", size);

            PostProcessingRenderer.getInstance().getFBO("scene" + size).bind();
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 5)
                PostProcessingRenderer.getInstance().getFBO("scene").bindTexture();
            else
                PostProcessingRenderer.getInstance().getFBO("scene" + sizePrev).bindTexture();

            renderFullQuad();

            PostProcessingRenderer.getInstance().getFBO("scene" + size).unbind();

        }

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void renderFullQuad() {
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

    public boolean areExtensionsAvailable() {
        return _extensionsAvailable;
    }
}
