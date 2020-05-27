/*
 * Copyright 2017 MovingBlocks
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

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.subsystem.DisplayDevice;
import org.terasology.math.MatrixUtils;
import org.terasology.math.TeraMath;
import org.terasology.rendering.nui.layers.mainMenu.videoSettings.CameraSetting;
import org.terasology.world.WorldProvider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.LinkedList;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.terasology.engine.subsystem.lwjgl.LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE;

/**
 * Simple default camera.
 */
public class PerspectiveCamera extends SubmersibleCamera implements PropertyChangeListener {
    // Values used for smoothing
    private Deque<Vector3f> previousPositions = new LinkedList<>();
    private Deque<Vector3f> previousViewingDirections = new LinkedList<>();

    private float multiplier = 0.9f;

    private PerspectiveCameraSettings cameraSettings;

    private float bobbingRotationOffsetFactor;
    private float bobbingVerticalOffsetFactor;
    private float cachedBobbingRotationOffsetFactor;
    private float cachedBobbingVerticalOffsetFactor;
    private DisplayDevice displayDevice;

    private Vector3f tempRightVector = new Vector3f();

    public PerspectiveCamera(WorldProvider worldProvider, RenderingConfig renderingConfig, DisplayDevice displayDevice) {
        super(worldProvider, renderingConfig);
        this.displayDevice = displayDevice;
        this.cameraSettings = renderingConfig.getCameraSettings();

        displayDevice.subscribe(DISPLAY_RESOLUTION_CHANGE, this);
    }

    @Override
    public boolean isBobbingAllowed() {
        return true;
    }

    @Override
    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(getProjectionMatrix()));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(getViewMatrix()));
    }

    @Override
    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(getNormViewMatrix()));
    }

    @Override
    public void update(float deltaT) {
        applyCinematicEffect();

        super.update(deltaT);
        updateMatrices();
    }

    private void applyCinematicEffect() {
        previousPositions.addFirst(new Vector3f(position));
        previousViewingDirections.addFirst(new Vector3f(viewingDirection));

        CameraSetting cameraSetting = cameraSettings.getCameraSetting();
        while (previousPositions.size() > cameraSetting.getSmoothingFrames()) {
            previousPositions.removeLast();
            previousViewingDirections.removeLast();
        }

        position.set(calculateVector(previousPositions));
        viewingDirection.set(calculateVector(previousViewingDirections));
    }

    private Vector3f calculateVector(Deque<Vector3f> vectors) {
        int i = 0;
        float x = 0;
        float y = 0;
        float z = 0;
        float factorMult = 0;

        for (Vector3f vector : vectors) {
            float factor = (float) Math.pow(multiplier, i);
            factorMult += factor;
            x += vector.x * factor;
            y += vector.y * factor;
            z += vector.z * factor;
            i++;
        }

        return new Vector3f(x / factorMult, y / factorMult, z / factorMult);
    }

    @Override
    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    @Override
    public void updateMatrices(float fov) {
        // Nothing to do...
        if (cachedPosition.equals(getPosition()) && cachedViewigDirection.equals(viewingDirection)
                && cachedBobbingRotationOffsetFactor == bobbingRotationOffsetFactor && cachedBobbingVerticalOffsetFactor == bobbingVerticalOffsetFactor
                && cachedFov == fov
                && cachedZFar == getzFar() && cachedZNear == getzNear()
                && cachedReflectionHeight == getReflectionHeight()) {
            return;
        }

        viewingDirection.cross(up, tempRightVector);
        tempRightVector.mul(bobbingRotationOffsetFactor);

        projectionMatrix = createPerspectiveProjectionMatrix(fov, getzNear(), getzFar(),this.displayDevice);

        viewMatrix = MatrixUtils.createViewMatrix(0f, bobbingVerticalOffsetFactor * 2.0f, 0f, viewingDirection.x, viewingDirection.y + bobbingVerticalOffsetFactor * 2.0f,
                viewingDirection.z, up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        normViewMatrix = MatrixUtils.createViewMatrix(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z,
                up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        reflectionMatrix.setColumn(0, new Vector4f(1.0f, 0.0f, 0.0f, 0.0f));
        reflectionMatrix.setColumn(1, new Vector4f(0.0f, -1.0f, 0.0f, 2f * (-position.y + getReflectionHeight())));
        reflectionMatrix.setColumn(2, new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
        reflectionMatrix.setColumn(3, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        reflectionMatrix.mul(viewMatrix, viewMatrixReflected);

        reflectionMatrix.setColumn(1, new Vector4f(0.0f, -1.0f, 0.0f, 0.0f));
        reflectionMatrix.mul(normViewMatrix,normViewMatrixReflected);

//        viewProjectionMatrix = MatrixUtils.calcViewProjectionMatrix(viewMatrix, projectionMatrix);
        viewProjectionMatrix = new Matrix4f(viewMatrix).mul(projectionMatrix);

        projectionMatrix.invert(inverseProjectionMatrix);
        viewProjectionMatrix.invert(inverseViewProjectionMatrix);

        // Used for dirty checks
        cachedPosition.set(getPosition());
        cachedViewigDirection.set(viewingDirection);
        cachedBobbingVerticalOffsetFactor = bobbingVerticalOffsetFactor;
        cachedBobbingRotationOffsetFactor = bobbingRotationOffsetFactor;
        cachedFov = fov;
        cachedZNear = getzNear();
        cachedZFar = getzFar();
        cachedReflectionHeight = getReflectionHeight();

        updateFrustum();
    }

    public void setBobbingRotationOffsetFactor(float f) {
        bobbingRotationOffsetFactor = f;
    }

    public void setBobbingVerticalOffsetFactor(float f) {
        bobbingVerticalOffsetFactor = f;
    }

    // TODO: Move the dependency on LWJGL (Display) elsewhere
    private static Matrix4f createPerspectiveProjectionMatrix(float fov, float zNear, float zFar, DisplayDevice displayDevice) {
        float aspectRatio = (float) displayDevice.getDisplayWidth()/ displayDevice.getDisplayHeight();
        float fovY = (float) (2 * Math.atan2(Math.tan(0.5 * fov * TeraMath.DEG_TO_RAD), aspectRatio));

        return new Matrix4f().perspective(fovY,aspectRatio,zNear,zFar).transpose();
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        if (propertyChangeEvent.getPropertyName().equals(DISPLAY_RESOLUTION_CHANGE)) {
            cachedFov = -1; // Invalidate the cache, so that matrices get regenerated.
            updateMatrices();
        }
    }
}
