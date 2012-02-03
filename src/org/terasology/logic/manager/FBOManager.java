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
import org.terasology.utilities.MathHelper;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class FBOManager {

    private static FBOManager _instance = null;
    private float _exposure;

    public class FBO {
        public int _fboId = 0;
        public int _textureId = 0;
        public int _depthRboId = 0;

        public void bind() {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, _fboId);
        }

        public void unbind() {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
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
    public static FBOManager getInstance() {
        if (_instance == null) {
            _instance = new FBOManager();
        }

        return _instance;
    }

    public FBO createFBO(String title, int width, int height, boolean hdr, boolean depth) {
        FBO fbo = new FBO();

        // Create the texture
        fbo._textureId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo._textureId);

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        if (hdr)
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA16F, width, height, 0, GL11.GL_RGBA, GL30.GL_HALF_FLOAT, (java.nio.ByteBuffer) null);
        else
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (java.nio.ByteBuffer) null);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        // Create depth render buffer object
        if (depth) {
            fbo._depthRboId = GL30.glGenRenderbuffers();
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, fbo._depthRboId);
            GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, width, height);
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        }

        // Create the FBO
        fbo._fboId = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo._fboId);

        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fbo._textureId, 0);

        if (depth)
            GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, fbo._depthRboId);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);

        _FBOs.put(title, fbo);
        return fbo;
    }

    public FBO getFBO(String title) {
        return _FBOs.get(title);
    }

    private void updateExposure() {
        ByteBuffer pixels = BufferUtils.createByteBuffer(4);
        FBOManager.getInstance().getFBO("scene1").bindTexture();
        glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        FBOManager.getInstance().getFBO("scene1").unbindTexture();

        float lum = 0.2126f * ((pixels.get(0) & 0xff) / 255f) + 0.7152f * ((pixels.get(1) & 0xff) / 255f) + 0.0722f * ((pixels.get(2) & 0xff) / 255f);
        _exposure = (float) MathHelper.lerp(_exposure, 0.5f / lum, 0.008);

        if (_exposure > 4.0f)
            _exposure = 4.0f;
    }

    public void renderScene() {
        generateDownsampledFBOs();
        updateExposure();

        generateHighPassFBO();
        generateBloomFBO();

        ShaderManager.getInstance().enableShader("fbo");
        FBOManager.FBO scene = FBOManager.getInstance().getFBO("scene");

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        FBOManager.getInstance().getFBO("sceneBloom").bindTexture();
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        scene.bindTexture();

        int texScene = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("fbo"), "texScene");
        GL20.glUniform1i(texScene, 0);
        int texBloom = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("fbo"), "texBloom");
        GL20.glUniform1i(texBloom, 1);

        int expos = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("fbo"), "exposure");
        GL20.glUniform1f(expos, _exposure);

        renderFullQuad();

        scene.unbindTexture();
        ShaderManager.getInstance().enableShader(null);
    }

    private void generateHighPassFBO() {
        ShaderManager.getInstance().enableShader("highp");

        FBOManager.getInstance().getFBO("sceneHighPass").bind();
        glViewport(0, 0, 1024, 1024);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        FBOManager.getInstance().getFBO("scene").bindTexture();

        renderFullQuad();

        FBOManager.getInstance().getFBO("sceneHighPass").unbind();

        ShaderManager.getInstance().enableShader(null);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateBloomFBO() {
        ShaderManager.getInstance().enableShader("blur");

        FBOManager.getInstance().getFBO("sceneBloom").bind();
        glViewport(0, 0, 1024, 1024);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        FBOManager.getInstance().getFBO("sceneHighPass").bindTexture();

        renderFullQuad();

        FBOManager.getInstance().getFBO("sceneBloom").unbind();

        ShaderManager.getInstance().enableShader(null);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void generateDownsampledFBOs() {
        ShaderManager.getInstance().enableShader("down");

        for (int i = 8; i >= 0; i--) {
            int sizePrev = (int) Math.pow(2, i + 1);
            int size = (int) Math.pow(2, i);

            int textureSize = GL20.glGetUniformLocation(ShaderManager.getInstance().getShader("down"), "size");
            GL20.glUniform1f(textureSize, size);

            FBOManager.getInstance().getFBO("scene" + size).bind();
            glViewport(0, 0, size, size);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            if (i == 8)
                FBOManager.getInstance().getFBO("scene").bindTexture();
            else
                FBOManager.getInstance().getFBO("scene" + sizePrev).bindTexture();

            renderFullQuad();

            FBOManager.getInstance().getFBO("scene" + size).unbind();

        }

        ShaderManager.getInstance().enableShader(null);
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
    }

    private void renderFullQuad() {
        glDisable(GL11.GL_DEPTH_TEST);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glBegin(GL_QUADS);
        glTexCoord2d(0.0, 0.0);
        glVertex3i(-1, -1, -1);
        glTexCoord2d(1.0, 0.0);
        glVertex3i(1, -1, -1);
        glTexCoord2d(1.0, 1.0);
        glVertex3i(1, 1, -1);
        glTexCoord2d(0.0, 1.0);
        glVertex3i(-1, 1, -1);
        glEnd();
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();

        glEnable(GL11.GL_DEPTH_TEST);
    }
}
