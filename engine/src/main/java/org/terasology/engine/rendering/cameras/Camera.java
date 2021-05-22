// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.cameras;

import org.joml.AxisAngle4f;
import org.joml.FrustumIntersection;
import org.joml.FrustumRayBuilder;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.terasology.engine.config.Config;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.joml.geom.AABBfc;
import org.terasology.engine.math.Direction;
import org.terasology.engine.registry.CoreRegistry;

/**
 * Camera base class.
 *
 */
public abstract class Camera {

    protected static final Vector3fc FORWARD = Direction.FORWARD.asVector3f();

    /* CAMERA PARAMETERS */
    protected final Vector3f position = new Vector3f(0, 0, 0);
    protected final Vector3f up = new Vector3f(Direction.UP.asVector3f());
    protected final Vector3f viewingDirection = new Vector3f(FORWARD);
    protected final Vector3f viewingAxis = new Vector3f(Direction.LEFT.asVector3f());
    protected float viewingAngle;

    protected float zNear = 0.1f;
    // TODO: This is too large, but many properties have to be adjusted if it changes
    protected float zFar = 5000.0f;

    protected float targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    protected float activeFov = targetFov / 4f;

    /* VIEW FRUSTUM */
    protected final FrustumIntersection viewFrustum = new FrustumIntersection();
    protected final FrustumIntersection viewFrustumReflected = new FrustumIntersection();

    /* MATRICES */
    protected Matrix4f projectionMatrix = new Matrix4f();
    protected Matrix4f inverseProjectionMatrix = new Matrix4f();
    protected Matrix4f normViewMatrix = new Matrix4f();
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f viewProjectionMatrix = new Matrix4f();
    protected Matrix4f inverseViewProjectionMatrix = new Matrix4f();
    protected Matrix4f prevViewProjectionMatrix = new Matrix4f();
    protected Matrix4f reflectionMatrix = new Matrix4f();

    protected Matrix4f viewMatrixReflected = new Matrix4f();
    protected Matrix4f normViewMatrixReflected = new Matrix4f();

    /* USED FOR DIRTY CHECKS */
    protected Vector3f cachedPosition = new Vector3f();
    protected Vector3f cachedViewigDirection = new Vector3f();
    protected float cachedFov;
    protected float cachedZNear;
    protected float cachedZFar;
    protected float cachedReflectionHeight;

    /* (Water) Reflection */
    protected boolean reflected;
    private float reflectionHeight = 32;

    /**
     * Applies the projection and modelview matrix.
     */
    public void lookThrough() {
        loadProjectionMatrix();
        loadModelViewMatrix();
    }

    /**
     * Applies the projection and the normalized modelview matrix (positioned at the origin without any offset like bobbing) .
     */
    public void lookThroughNormalized() {
        loadProjectionMatrix();
        loadNormalizedModelViewMatrix();
    }

    public void updateFrustum() {
        if (getViewMatrix() == null || getProjectionMatrix() == null) {
            return;
        }
        viewFrustumReflected.set(projectionMatrix.mul(viewMatrixReflected, new Matrix4f()));
        viewFrustum.set(viewProjectionMatrix, true);
    }

    public abstract boolean isBobbingAllowed();

    public abstract void loadProjectionMatrix();

    public abstract void loadModelViewMatrix();

    public abstract void loadNormalizedModelViewMatrix();

    public abstract void updateMatrices();

    public abstract void updateMatrices(float fov);

    public void update(float delta) {
        double diff = Math.abs(activeFov - targetFov);
        if (diff < 1.0) {
            activeFov = targetFov;
            return;
        }
        if (activeFov < targetFov) {
            activeFov += 50.0f * delta;
            if (activeFov >= targetFov) {
                activeFov = targetFov;
            }
        } else if (activeFov > targetFov) {
            activeFov -= 50.0f * delta;
            if (activeFov <= targetFov) {
                activeFov = targetFov;
            }
        }
    }

    public void extendFov(float fov) {
        targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView() + fov;
    }

    public void resetFov() {
        targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    }

    public void setReflected(boolean reflected) {
        this.reflected = reflected;
    }

    public float getReflectionHeight() {
        return reflectionHeight;
    }

    public void setReflectionHeight(float reflectionHeight) {
        this.reflectionHeight = reflectionHeight;
    }

    public void updatePrevViewProjectionMatrix() {
        prevViewProjectionMatrix.set(viewProjectionMatrix);
    }

    public Matrix4f getViewMatrix() {
        if (!reflected) {
            return viewMatrix;
        }

        return viewMatrixReflected;
    }

    public Matrix4f getNormViewMatrix() {
        if (!reflected) {
            return normViewMatrix;
        }

        return normViewMatrixReflected;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    public Matrix4f getInverseProjectionMatrix() {
        return inverseProjectionMatrix;
    }

    public Matrix4f getInverseViewProjectionMatrix() {
        return inverseViewProjectionMatrix;
    }

    public Matrix4f getPrevViewProjectionMatrix() {
        return prevViewProjectionMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getViewingDirection() {
        return viewingDirection;
    }

    public Quaternionf getOrientation(Quaternionf orientation) {
        return orientation.set(new AxisAngle4f(viewingAngle, viewingAxis));
    }

    public void setOrientation(Quaternionfc orientation) {
        orientation.transform(FORWARD, viewingDirection);
        AxisAngle4f axisAngle = new AxisAngle4f(orientation);
        viewingAxis.set(axisAngle.x, axisAngle.y, axisAngle.z);
        viewingAngle = axisAngle.angle;
    }

    public FrustumIntersection getViewFrustum() {
        return viewFrustum;
    }

    public FrustumIntersection getViewFrustumReflected() {
        return viewFrustumReflected;
    }

    public float getzNear() {
        return zNear;
    }

    public void setzNear(float zNear) {
        this.zNear = zNear;
    }

    public float getzFar() {
        return zFar;
    }

    public void setzFar(float zFar) {
        this.zFar = zFar;
    }

    public boolean isReflected() {
        return reflected;
    }

    public boolean hasInSight(AABBfc aabb) {
        // TODO: strange offset to shift aabb by player view position
        Vector3f playerView = CoreRegistry.get(LocalPlayer.class).getViewPosition(new Vector3f());
        return viewFrustum.testAab(aabb.minX() - playerView.x, aabb.minY() - playerView.y, aabb.minZ() - playerView.z, aabb.maxX() - playerView.x, aabb.maxY() - playerView.y, aabb.maxZ() - playerView.z);
    }
}
