// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.cameras;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.core.subsystem.DisplayDevice;
import org.terasology.engine.rendering.nui.layers.mainMenu.videoSettings.CameraSetting;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.math.TeraMath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.LinkedList;

import static org.terasology.engine.core.subsystem.lwjgl.LwjglDisplayDevice.DISPLAY_RESOLUTION_CHANGE;

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
    private final DisplayDevice displayDevice;

    private Vector3f tempRightVector = new Vector3f();

    public PerspectiveCamera(WorldProvider worldProvider, RenderingConfig renderingConfig,
                             DisplayDevice displayDevice) {
        super(worldProvider, renderingConfig);
        this.displayDevice = displayDevice;
        this.cameraSettings = renderingConfig.getCameraSettings();

        displayDevice.subscribe(DISPLAY_RESOLUTION_CHANGE, this);
        renderingConfig.subscribe(RenderingConfig.VIEW_DISTANCE, this);
        renderingConfig.subscribe(RenderingConfig.CHUNK_LODS, this);
        updateFarClippingDistance();
    }

    @Override
    public boolean isBobbingAllowed() {
        return true;
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

        float aspectRatio = (float) displayDevice.getWidth() / displayDevice.getHeight();
        float fovY = (float) (2 * Math.atan2(Math.tan(0.5 * fov * TeraMath.DEG_TO_RAD), aspectRatio));
        projectionMatrix.setPerspective(fovY, aspectRatio, getzNear(), getzFar());

        viewMatrix.setLookAt(0f, bobbingVerticalOffsetFactor * 2.0f, 0f, viewingDirection.x,
            viewingDirection.y + bobbingVerticalOffsetFactor * 2.0f,
            viewingDirection.z, up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        normViewMatrix.setLookAt(0f, bobbingVerticalOffsetFactor * 2.0f, 0f, viewingDirection.x,
            viewingDirection.y + bobbingVerticalOffsetFactor * 2.0f,
            viewingDirection.z, up.x + tempRightVector.x, up.y + tempRightVector.y, up.z + tempRightVector.z);

        reflectionMatrix.setRow(0, new Vector4f(1.0f, 0.0f, 0.0f, 0.0f));
        reflectionMatrix.setRow(1, new Vector4f(0.0f, -1.0f, 0.0f, 2f * (-position.y + getReflectionHeight())));
        reflectionMatrix.setRow(2, new Vector4f(0.0f, 0.0f, 1.0f, 0.0f));
        reflectionMatrix.setRow(3, new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        viewMatrix.mul(reflectionMatrix, viewMatrixReflected);

        reflectionMatrix.setRow(1, new Vector4f(0.0f, -1.0f, 0.0f, 0.0f));
        normViewMatrix.mul(reflectionMatrix, normViewMatrixReflected);

        projectionMatrix.mul(viewMatrix, viewProjectionMatrix);
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

    private void updateFarClippingDistance() {
        float distance = renderingConfig.getViewDistance().getChunkDistance().x() * Chunks.SIZE_X * (1 << (int) renderingConfig.getChunkLods());
        zFar = Math.max(distance, 600) * 2;
        // distance is an estimate of how far away the farthest chunks are, and the minimum bound is to ensure that the sky is visible.
    }

    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        switch (propertyChangeEvent.getPropertyName()) {
            case DISPLAY_RESOLUTION_CHANGE:
                cachedFov = -1; // Invalidate the cache, so that matrices get regenerated.
                updateMatrices();
                return;
            case RenderingConfig.VIEW_DISTANCE:
            case RenderingConfig.CHUNK_LODS:
                updateFarClippingDistance();
                return;
            default:
        }
    }
}
