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
package com.github.begla.blockmania.rendering.helper;

import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector4f;

import static org.lwjgl.opengl.GL11.glGenLists;

/**
 * A collection of some basic primitives.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Primitives {

    public static int generateColoredBlock(Vector4f color, float size) {
        int id = glGenLists(1);

        GL11.glNewList(id, GL11.GL_COMPILE);
        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(color.x, color.y, color.z, color.w);

        float sHalf = size / 2;

        // TOP
        GL11.glVertex3f(-sHalf, sHalf, sHalf);
        GL11.glVertex3f(sHalf, sHalf, sHalf);
        GL11.glVertex3f(sHalf, sHalf, -sHalf);
        GL11.glVertex3f(-sHalf, sHalf, -sHalf);

        // LEFT
        GL11.glVertex3f(-sHalf, -sHalf, -sHalf);
        GL11.glVertex3f(-sHalf, -sHalf, sHalf);
        GL11.glVertex3f(-sHalf, sHalf, sHalf);
        GL11.glVertex3f(-sHalf, sHalf, -sHalf);

        // RIGHT
        GL11.glVertex3f(sHalf, sHalf, -sHalf);
        GL11.glVertex3f(sHalf, sHalf, sHalf);
        GL11.glVertex3f(sHalf, -sHalf, sHalf);
        GL11.glVertex3f(sHalf, -sHalf, -sHalf);

        GL11.glColor4f(0.85f * color.x, 0.85f * color.y, 0.85f * color.z, color.w);

        // FRONT
        GL11.glVertex3f(-sHalf, sHalf, -sHalf);
        GL11.glVertex3f(sHalf, sHalf, -sHalf);
        GL11.glVertex3f(sHalf, -sHalf, -sHalf);
        GL11.glVertex3f(-sHalf, -sHalf, -sHalf);

        // BACK
        GL11.glVertex3f(-sHalf, -sHalf, sHalf);
        GL11.glVertex3f(sHalf, -sHalf, sHalf);
        GL11.glVertex3f(sHalf, sHalf, sHalf);
        GL11.glVertex3f(-sHalf, sHalf, sHalf);

        // BOTTOM
        GL11.glVertex3f(-sHalf, -sHalf, -sHalf);
        GL11.glVertex3f(sHalf, -sHalf, -sHalf);
        GL11.glVertex3f(sHalf, -sHalf, sHalf);
        GL11.glVertex3f(-sHalf, -sHalf, sHalf);

        GL11.glEnd();
        GL11.glEndList();

        return id;
    }
}
