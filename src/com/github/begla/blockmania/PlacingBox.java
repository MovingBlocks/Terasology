/*
 *  Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package com.github.begla.blockmania;

import static org.lwjgl.opengl.GL11.*;

/**
 * A wireframe box which can be used to highlight blocks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class PlacingBox extends RenderableObject {

    @Override
    public void render() {
        glLineWidth(2f);
        glColor4f(0.25f, 0.25f, 0.25f, 1.0f);

        // FRONT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(+0.5f, -0.5f, -0.5f);
        glVertex3f(+0.5f, +0.5f, -0.5f);
        glVertex3f(-0.5f, +0.5f, -0.5f);
        glEnd();

        // BACK
        glBegin(GL_LINE_LOOP);
        glVertex3f(-0.5f, -0.5f, +0.5f);
        glVertex3f(+0.5f, -0.5f, +0.5f);
        glVertex3f(+0.5f, +0.5f, +0.5f);
        glVertex3f(-0.5f, +0.5f, +0.5f);
        glEnd();

        // TOP
        glBegin(GL_LINE_LOOP);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(+0.5f, -0.5f, -0.5f);
        glVertex3f(+0.5f, -0.5f, +0.5f);
        glVertex3f(-0.5f, -0.5f, +0.5f);
        glEnd();

        // BOTTOM
        glBegin(GL_LINE_LOOP);
        glVertex3f(-0.5f, +0.5f, -0.5f);
        glVertex3f(+0.5f, +0.5f, -0.5f);
        glVertex3f(+0.5f, +0.5f, +0.5f);
        glVertex3f(-0.5f, +0.5f, +0.5f);
        glEnd();

        // LEFT
        glBegin(GL_LINE_LOOP);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, +0.5f);
        glVertex3f(-0.5f, +0.5f, +0.5f);
        glVertex3f(-0.5f, +0.5f, -0.5f);
        glEnd();

        // RIGHT
        glBegin(GL_LINE_LOOP);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, +0.5f);
        glVertex3f(0.5f, +0.5f, +0.5f);
        glVertex3f(0.5f, +0.5f, -0.5f);
        glEnd();
    }
}
