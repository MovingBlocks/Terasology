/*
 * Copyright 2016 MovingBlocks
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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.glPopMatrix;

public final class OpenGLUtils {
    private static int displayListQuad = -1;

    private OpenGLUtils() {
        // Utility class, no instance required
    }

    /**
     * Removes the rotation and scale part of the current OpenGL matrix.
     * Can be used to render billboards like particles.
     */
    public static void applyBillboardOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, model);

        // And undo all rotations and scaling
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    model.put(i * 4 + j, 1.0f);
                } else {
                    model.put(i * 4 + j, 0.0f);
                }
            }
        }

        GL11.glLoadMatrix(model);
    }

    /**
     * Sets the viewport of the currently bound FBO to the dimensions of the FBO
     * given as parameter.
     *
     * @param fbo The FBO whose dimensions will be matched by the viewport of the currently bound FBO.
     */
    public static void setViewportToSizeOf(FBO fbo) {
        glViewport(0, 0, fbo.width(), fbo.height());
    }

    public static void setViewportToSizeOf(DefaultDynamicFBOs defaultDynamicFBO) {
        glViewport(0, 0, defaultDynamicFBO.width(), defaultDynamicFBO.height());
    }

    /**
     * Unbinds any currently bound FBO and binds the default Frame Buffer,
     * which is usually the Display (be it the full screen or a window).
     */
    public static void bindDisplay() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }


    /**
     * Once an FBO is bound, opengl commands will act on it, i.e. by drawing on it.
     * Meanwhile shaders might output not just colors but additional per-pixel data. This method establishes on which
     * of an FBOs attachments, subsequent opengl commands and shaders will draw on.
     *
     * @param fbo         The FBO holding the attachments to be set or unset for drawing.
     * @param color       If True the color buffer is set as drawable. If false subsequent commands and shaders won't be able to draw on it.
     * @param normal      If True the normal buffer is set as drawable. If false subsequent commands and shaders won't be able to draw on it.
     * @param lightBuffer If True the light buffer is set as drawable. If false subsequent commands and shaders won't be able to draw on it.
     */
    // TODO: verify if this can become part of the FBO.bind() method.
    public static void setRenderBufferMask(FBO fbo, boolean color, boolean normal, boolean lightBuffer) {
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
        if (fbo.lightBufferTextureId != 0 && lightBuffer) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    /**
     * Renders a quad filling the whole currently set viewport.
     */
    public static void renderFullscreenQuad() {
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
     * @param x              an integer representing the x coordinate (in pixels) of the origin of the viewport.
     * @param y              an integer representing the y coordinate (in pixels) of the origin of the viewport.
     * @param viewportWidth  an integer representing the width (in pixels) the viewport.
     * @param viewportHeight an integer representing the height (in pixels) the viewport.
     */
    // TODO: perhaps remove this method and make sure the viewport is set explicitly.
    // TODO: find a much more suitable name for this method
    public static void renderFullscreenQuad(int x, int y, int viewportWidth, int viewportHeight) {
        glViewport(x, y, viewportWidth, viewportHeight);
        renderFullscreenQuad();
    }


    // TODO: replace with a proper resident buffer with interleaved vertex and uv coordinates
    private static void renderQuad() {
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
}
