/*
 * Copyright 2013 MovingBlocks
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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.terasology.math.MatrixUtils;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Simple default camera.
 *
 */
public class OrthographicCamera extends Camera {

    private float top;
    private float bottom;
    private float left;
    private float right;

    public OrthographicCamera(float left, float right, float top, float bottom) {
        super();

        this.top = top;
        this.bottom = bottom;
        this.left = left;
        this.right = right;
        this.zNear = -1000.0f;
        this.zFar = 1000.0f;
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }

    @Override
    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrixf(getProjectionMatrix().get(BufferUtils.createFloatBuffer(16)));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(getViewMatrix().get(BufferUtils.createFloatBuffer(16)));
    }

    @Override
    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrixf(getNormViewMatrix().get(BufferUtils.createFloatBuffer(16)));
    }

    @Override
    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    @Override
    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    @Override
    public void updateMatrices(float fov) {
        prevViewProjectionMatrix.set(viewProjectionMatrix);

        // Nothing to do...
        if (cachedPosition.equals(getPosition()) && cachedViewigDirection.equals(viewingDirection)
            && cachedZFar == zFar && cachedZNear == zNear) {
            return;
        }

        projectionMatrix.setOrtho(left, right, bottom, top, zNear, zFar);
        viewMatrix.setLookAt(0f, 0.0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y,
            up.z);
        normViewMatrix.setLookAt(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y,
            up.z);

        projectionMatrix.mul(viewMatrix, viewProjectionMatrix);
        viewProjectionMatrix.invert(inverseViewProjectionMatrix);

        // Used for dirty checks
        cachedPosition.set(getPosition());
        cachedViewigDirection.set(viewingDirection);
        cachedZFar = zFar;
        cachedZNear = zNear;

        updateFrustum();
    }

    @Override
    public ViewFrustum getViewFrustumReflected() {
        throw new RuntimeException("Not yet implemented!");
    }
}
