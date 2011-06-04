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
package com.github.begla.blockmania.world;

import org.lwjgl.opengl.GL11;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Primitives {

    public static void drawCloud(float scaleX, float scaleY, float scaleZ, float x, float y, float z, float brightness) {
        // Front face
        GL11.glColor3f(0.92f * brightness, 0.92f  * brightness, 0.92f  * brightness);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);

        // Back Face
        GL11.glColor3f(0.92f  * brightness, 0.92f  * brightness, 0.92f  * brightness);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);

        // Top Face
        GL11.glColor3f(0.99f  * brightness, 0.99f  * brightness, 0.99f  * brightness);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);

        // Bottom Face
        GL11.glColor3f(0.99f  * brightness, 0.99f  * brightness, 0.99f  * brightness);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);

        // Right face
        GL11.glColor3f(0.92f  * brightness, 0.92f  * brightness, 0.92f  * brightness);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);

        // Left Face
        GL11.glColor3f(0.92f  * brightness, 0.92f  * brightness, 0.92f  * brightness);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, -1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 0.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, -1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(1.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, 1.0f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 1.0f);
        GL11.glVertex3f(-1.0f * scaleX + x, 1.0f * scaleY + y, -1.0f * scaleZ + z);
    }
}
