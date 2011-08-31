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
package com.github.begla.blockmania.utilities;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * A collection of some basic primitives.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class Primitives {

    private static final Vector3f skyColor = new Vector3f(0.72f, 0.78f, 1.0f);
    private static final Vector3f skyColor2 = new Vector3f(0.84f, 0.88f, 1f);

    /**
     * @param scaleX Scale along the x-axis
     * @param scaleY Scale along the y-axis
     * @param scaleZ Scale along the z-axis
     * @param x      Position on the x-axis
     * @param y      Position on the y-axis
     * @param z      Position on the z-axis
     */
    public static void drawCloud(float scaleX, float scaleY, float scaleZ, float x, float y, float z) {
        // Front face
        GL11.glColor3f(0.99f, 0.99f, 0.99f);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);

        // Back Face
        GL11.glColor3f(0.99f, 0.99f, 0.99f);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);

        // Top Face
        GL11.glColor3f(1f, 1f, 1f);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);

        // Bottom Face
        GL11.glColor3f(0.91f, 0.91f, 0.91f);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);

        // Right face
        GL11.glColor3f(0.99f, 0.99f, 0.99f);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);

        // Left Face
        GL11.glColor3f(0.99f, 0.99f, 0.99f);
        GL11.glTexCoord2f(0.0f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, -0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.0f);
        GL11.glVertex3f(-0.5f * scaleX + x, -0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.5f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, 0.5f * scaleZ + z);
        GL11.glTexCoord2f(0.0f, 0.5f);
        GL11.glVertex3f(-0.5f * scaleX + x, 0.5f * scaleY + y, -0.5f * scaleZ + z);
    }

    /**
     * Draws a simple gradient skybox.
     *
     * @param brightness The brightness of the skybox.
     */
    public static void drawSkyBox(float brightness) {
        // Front face
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, 0.0f, 0.5f);
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(0.5f, 0.0f, 0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);

        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.0f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.0f, 0.5f);

        // Back Face
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, 0.0f, -0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(0.5f, 0f, -0.5f);

        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, 0.0f, -0.5f);
        GL11.glVertex3f(0.5f, 0.0f, -0.5f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);

        // Right face
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(0.5f, 0f, -0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(0.5f, 0f, 0.5f);

        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(0.5f, 0.0f, -0.5f);
        GL11.glVertex3f(0.5f, 0.0f, 0.5f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);


        // Left Face
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, 0f, -0.5f);
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, 0f, 0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);

        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.0f, 0.5f);
        GL11.glVertex3f(-0.5f, 0.0f, -0.5f);

        // Top Face
        GL11.glColor3f(skyColor.x *brightness, skyColor.y *brightness, skyColor.z *brightness);
        GL11.glVertex3f(-0.5f, 0.5f, -0.5f);
        GL11.glVertex3f(-0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.5f, 0.5f);
        GL11.glVertex3f(0.5f, 0.5f, -0.5f);

        // Bottom Face
        GL11.glColor3f(skyColor2.x *brightness, skyColor2.y *brightness, skyColor2.z *brightness);
        GL11.glVertex3f(-0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(0.5f, -0.5f, -0.5f);
        GL11.glVertex3f(0.5f, -0.5f, 0.5f);
        GL11.glVertex3f(-0.5f, -0.5f, 0.5f);
    }
}
