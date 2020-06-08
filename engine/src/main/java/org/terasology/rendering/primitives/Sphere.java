/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.rendering.primitives;

import static org.lwjgl.opengl.GL11.GL_QUAD_STRIP;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex3f;

/**
 * Optimized Sphere from LWJGL 2 glu package. Removed unused code in Terasology. Used for skysphere and it effects
 * (normals - inside)
 */
public class Sphere {

    private boolean textureFlag = false;

    /**
     * draws a sphere of the given	radius centered	around the origin. The sphere is subdivided around the z axis into
     * slices and along the z axis into stacks (similar to lines of longitude and latitude).
     * <p>
     * If the orientation is set to GLU.OUTSIDE (with glu.quadricOrientation), then any normals generated point away
     * from the center of the sphere. Otherwise, they point toward the center of the sphere.
     * <p>
     * If texturing is turned on (with glu.quadricTexture), then texture coordinates are generated so that t ranges from
     * 0.0 at z=-radius to 1.0 at z=radius (t increases linearly along longitudinal lines), and s ranges from 0.0 at the
     * +y axis, to 0.25 at the +x axis, to 0.5 at the -y axis, to 0.75 at the -x axis, and back to 1.0 at the +y axis.
     */
    public void draw(float radius, int slices, int stacks) {
        float rho, drho, theta, dtheta;
        float x, y, z;
        float s, t, ds, dt;
        int i, j, imin, imax;
        float nsign;

        nsign = 1.0f;

        drho = (float) Math.PI / stacks;
        dtheta = 2.0f * (float) Math.PI / slices;

        if (!textureFlag) {
            // draw +Z end as a triangle fan
            glBegin(GL_TRIANGLE_FAN);
            glNormal3f(0.0f, 0.0f, 1.0f);
            glVertex3f(0.0f, 0.0f, nsign * radius);
            for (j = 0; j <= slices; j++) {
                theta = (j == slices) ? 0.0f : j * dtheta;
                x = -sin(theta) * sin(drho);
                y = cos(theta) * sin(drho);
                z = nsign * cos(drho);
                glNormal3f(x * nsign, y * nsign, z * nsign);
                glVertex3f(x * radius, y * radius, z * radius);
            }
            glEnd();
        }

        ds = 1.0f / slices;
        dt = 1.0f / stacks;
        t = 1.0f; // because loop now runs from 0
        if (textureFlag) {
            imin = 0;
            imax = stacks;
        } else {
            imin = 1;
            imax = stacks - 1;
        }

        // draw intermediate stacks as quad strips
        for (i = imin; i < imax; i++) {
            rho = i * drho;
            glBegin(GL_QUAD_STRIP);
            s = 0.0f;
            for (j = 0; j <= slices; j++) {
                theta = (j == slices) ? 0.0f : j * dtheta;
                x = -sin(theta) * sin(rho);
                y = cos(theta) * sin(rho);
                z = nsign * cos(rho);
                glNormal3f(x * nsign, y * nsign, z * nsign);
                TXTR_COORD(s, t);
                glVertex3f(x * radius, y * radius, z * radius);
                x = -sin(theta) * sin(rho + drho);
                y = cos(theta) * sin(rho + drho);
                z = nsign * cos(rho + drho);
                glNormal3f(x * nsign, y * nsign, z * nsign);
                TXTR_COORD(s, t - dt);
                s += ds;
                glVertex3f(x * radius, y * radius, z * radius);
            }
            glEnd();
            t -= dt;
        }

        if (!textureFlag) {
            // draw -Z end as a triangle fan
            glBegin(GL_TRIANGLE_FAN);
            glNormal3f(0.0f, 0.0f, -1.0f);
            glVertex3f(0.0f, 0.0f, -radius * nsign);
            rho = (float) Math.PI - drho;
            s = 1.0f;
            for (j = slices; j >= 0; j--) {
                theta = (j == slices) ? 0.0f : j * dtheta;
                x = -sin(theta) * sin(rho);
                y = cos(theta) * sin(rho);
                z = nsign * cos(rho);
                glNormal3f(x * nsign, y * nsign, z * nsign);
                s -= ds;
                glVertex3f(x * radius, y * radius, z * radius);
            }
            glEnd();
        }
    }

    public boolean getTextureFlag() {
        return textureFlag;
    }

    public void setTextureFlag(boolean textureFlag) {
        this.textureFlag = textureFlag;
    }

    private void TXTR_COORD(float x, float y) {
        if (textureFlag) glTexCoord2f(x, y);
    }

    private float sin(float r) {
        return (float) Math.sin(r);
    }

    private float cos(float r) {
        return (float) Math.cos(r);
    }
}
