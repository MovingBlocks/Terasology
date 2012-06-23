/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import javax.vecmath.Vector3d;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

/**
 * Simple default camera.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class DefaultCamera extends Camera {

    private double _bobbingRotationOffsetFactor, _bobbingVerticalOffsetFactor = 0.0;

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
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);
        GLU.gluLookAt(0f, (float) _bobbingVerticalOffsetFactor * 2.0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y + (float) _bobbingVerticalOffsetFactor * 2.0f, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);
        _viewFrustum.updateFrustum();
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        glLoadIdentity();
        Vector3d right = new Vector3d();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);
        GLU.gluLookAt(0f, 0f, 0f, (float) _viewingDirection.x, (float) _viewingDirection.y, (float) _viewingDirection.z, (float) _up.x + (float) right.x, (float) _up.y + (float) right.y, (float) _up.z + (float) right.z);
        _viewFrustum.updateFrustum();
    }

    public void setBobbingRotationOffsetFactor(double f) {
        _bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(double f) {
        _bobbingVerticalOffsetFactor = f;
    }
}
