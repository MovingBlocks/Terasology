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

import org.lwjgl.opengl.GL11;
import org.terasology.math.TeraMath;

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
        this.zNear = -1000.0f;
        this.zFar = 1000.0f;
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }

    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(projectionMatrix));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(viewMatrix));
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(normViewMatrix));
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    public void updateMatrices(float fov) {
        prevViewProjectionMatrix.set(viewProjectionMatrix);

        // Nothing to do...
        if (cachedPosition.equals(getPosition()) && cachedViewigDirection.equals(getViewingDirection())
                && cachedZFar == zFar && cachedZNear == zNear) {
            return;
        }

        projectionMatrix = TeraMath.createOrthogonalProjectionMatrix(left, right, top, bottom, zNear, zFar);
        viewMatrix = TeraMath.createViewMatrix(0f, 0.0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);
        normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);

        viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(viewMatrix, projectionMatrix);
        inverseViewProjectionMatrix.invert(viewProjectionMatrix);

        // Used for dirty checks
        cachedPosition.set(getPosition());
        cachedViewigDirection.set(getViewingDirection());
        cachedZFar = zFar;
        cachedZNear = zNear;

        updateFrustum();
    }

    public ViewFrustum getViewFrustumReflected() {
        throw new RuntimeException("Not yet implemented!");
    }
}
