/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.rendering.cameras;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.terasology.math.TeraMath;

import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * Simple default camera.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultCamera extends Camera {

    private float _bobbingRotationOffsetFactor, _bobbingVerticalOffsetFactor = 0.0f;

    public void loadProjectionMatrix(float fov) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        float fovy = (float) (2 * Math.atan2(Math.tan(0.5 * fov * TeraMath.DEG_TO_RAD), aspectRatio)) * TeraMath.RAD_TO_DEG;
        gluPerspective(fovy, aspectRatio, 0.1f, 512f);
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();
        Vector3f right = new Vector3f();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);
        GLU.gluLookAt(0f, _bobbingVerticalOffsetFactor * 2.0f, 0f, _viewingDirection.x, _viewingDirection.y + _bobbingVerticalOffsetFactor * 2.0f, _viewingDirection.z, _up.x + right.x, _up.y + right.y, _up.z + right.z);
        _viewFrustum.updateFrustum();
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();
        Vector3f right = new Vector3f();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);
        GLU.gluLookAt(0f, 0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x + right.x, _up.y + right.y, _up.z + right.z);
        _viewFrustum.updateFrustum();
    }

    public void setBobbingRotationOffsetFactor(float f) {
        _bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(float f) {
        _bobbingVerticalOffsetFactor = f;
    }
}
