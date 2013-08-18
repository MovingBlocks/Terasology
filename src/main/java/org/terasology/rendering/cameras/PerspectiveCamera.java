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

import org.lwjgl.opengl.Display;
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
public class PerspectiveCamera extends Camera {

    private float bobbingRotationOffsetFactor;
    private float bobbingVerticalOffsetFactor;
    private float cachedBobbingRotationOffsetFactor;
    private float cachedBobbingVerticalOffsetFactor;

    private Vector3f tempRightVector = new Vector3f();

    @Override
    public boolean isBobbingAllowed() {
        return true;
    }

    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(getProjectionMatrix()));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(getViewMatrix()));
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(getNormViewMatrix()));
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    public void updateMatrices(float fov) {
        // Nothing to do...
        if (cachedPosition.equals(getPosition()) && cachedViewigDirection.equals(getViewingDirection())
                && cachedBobbingRotationOffsetFactor == bobbingRotationOffsetFactor && cachedBobbingVerticalOffsetFactor == bobbingVerticalOffsetFactor
                && cachedFov == fov
                && cachedZFar == getzFar() && cachedZNear == getzNear()) {
            return;
        }

        tempRightVector.cross(viewingDirection, up);
        tempRightVector.scale(bobbingRotationOffsetFactor);

        projectionMatrix = createPerspectiveProjectionMatrix(fov, getzNear(), getzFar());

        viewMatrix = TeraMath.createViewMatrix(0f, bobbingVerticalOffsetFactor * 2.0f, 0f, viewingDirection.x, viewingDirection.y + bobbingVerticalOffsetFactor * 2.0f,
                viewingDirection.z, up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z,
                up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        reflectionMatrix.setRow(0, 1.0f, 0.0f, 0.0f, 0.0f);
        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 2f * (-position.y + 32f));
        reflectionMatrix.setRow(2, 0.0f, 0.0f, 1.0f, 0.0f);
        reflectionMatrix.setRow(3, 0.0f, 0.0f, 0.0f, 1.0f);
        viewMatrixReflected.mul(viewMatrix, reflectionMatrix);

        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 0.0f);
        normViewMatrixReflected.mul(normViewMatrix, reflectionMatrix);

        viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(viewMatrix, projectionMatrix);

        inverseProjectionMatrix.invert(projectionMatrix);
        inverseViewProjectionMatrix.invert(viewProjectionMatrix);

        // Used for dirty checks
        cachedPosition.set(getPosition());
        cachedViewigDirection.set(getViewingDirection());
        cachedBobbingVerticalOffsetFactor = bobbingVerticalOffsetFactor;
        cachedBobbingRotationOffsetFactor = bobbingRotationOffsetFactor;
        cachedFov = fov;
        cachedZNear = getzNear();
        cachedZFar = getzFar();

        updateFrustum();
    }

    public void setBobbingRotationOffsetFactor(float f) {
        bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(float f) {
        bobbingVerticalOffsetFactor = f;
    }

    // TODO: Move the dependency on LWJGL (Display) elsewhere
    public Matrix4f createPerspectiveProjectionMatrix(float fov, float zNear, float zFar) {
        float aspectRatio = (float) Display.getWidth() / Display.getHeight();
        float fovY = (float) (2 * Math.atan2(Math.tan(0.5 * fov * TeraMath.DEG_TO_RAD), aspectRatio));

        return TeraMath.createPerspectiveProjectionMatrix(fovY, aspectRatio, zNear, zFar);
    }
}
