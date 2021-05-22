// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glCallList;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glEndList;
import static org.lwjgl.opengl.GL11.glGenLists;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glNewList;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTexCoord2d;
import static org.lwjgl.opengl.GL11.glVertex3i;
import static org.lwjgl.opengl.GL11.glViewport;

public final class OpenGLUtils {
    private static int displayListQuad = -1;

    private OpenGLUtils() {
        // Utility class, no instance required
    }

    /**
     * Removes the rotation and scale part of the current OpenGL matrix.
     * Can be used to render billboards like particles.
     */
    @Deprecated
    public static void applyBillboardOrientation() {
        // Fetch the current modelview matrix
        final FloatBuffer model = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloatv(GL11.GL_MODELVIEW_MATRIX, model);

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

        GL11.glLoadMatrixf(model);
    }

    /**
     * Renders a quad filling the whole currently set viewport.
     */
    @Deprecated
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
    @Deprecated
    public static void renderFullscreenQuad(int x, int y, int viewportWidth, int viewportHeight) {
        glViewport(x, y, viewportWidth, viewportHeight);
        renderFullscreenQuad();
    }


    // TODO: replace with a proper resident buffer with interleaved vertex and uv coordinates
    @Deprecated
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
