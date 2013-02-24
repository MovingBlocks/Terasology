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

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import org.lwjgl.opengl.GL11;
import org.terasology.math.TeraMath;

/**
 * Simple default camera.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class PerspectiveCamera extends Camera {

    private float _bobbingRotationOffsetFactor, _bobbingVerticalOffsetFactor = 0.0f;

    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(_projectionMatrix));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(_viewMatrix));
        _viewFrustum.updateFrustum();
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(_normViewMatrix));
        _viewFrustum.updateFrustum();
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(_activeFov);
    }

    public void updateMatrices(float overrideFov) {
        Vector3f right = new Vector3f();
        right.cross(_viewingDirection, _up);
        right.scale(_bobbingRotationOffsetFactor);

        _projectionMatrix = TeraMath.createPerspectiveProjectionMatrix(overrideFov, 0.1f, 5000.0f);

        _viewMatrix = TeraMath.createViewMatrix(0f, _bobbingVerticalOffsetFactor * 2.0f, 0f, _viewingDirection.x, _viewingDirection.y + _bobbingVerticalOffsetFactor * 2.0f,
                _viewingDirection.z, _up.x + right.x, _up.y + right.y, _up.z + right.z);
        _normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x + right.x, _up.y + right.y, _up.z + right.z);

        _prevViewProjectionMatrix = new Matrix4f(_viewProjectionMatrix);
        _viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(_viewMatrix, _projectionMatrix);
        _inverseViewProjectionMatrix.invert(_viewProjectionMatrix);
    }

    public void setBobbingRotationOffsetFactor(float f) {
        _bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(float f) {
        _bobbingVerticalOffsetFactor = f;
    }
}
