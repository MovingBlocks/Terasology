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

import org.lwjgl.opengl.GL11;
import org.terasology.math.TeraMath;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Simple default camera.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class OrthographicCamera extends Camera {

    float top, bottom, left, right;

    public OrthographicCamera(float left, float right, float top, float bottom) {
        super();

        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
    }

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
        _projectionMatrix = TeraMath.createOrthogonalProjectionMatrix(left, right, top, bottom, -1000.0f, 1000.0f);

        _viewMatrix = TeraMath.createViewMatrix(0f, 0.0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x, _up.y, _up.z);
        _normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x, _up.y, _up.z);

        _prevViewProjectionMatrix = new Matrix4f(_viewProjectionMatrix);
        _viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(_viewMatrix, _projectionMatrix);
    }
}
