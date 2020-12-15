// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.rendering.cameras;

import org.joml.AABBf;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.BufferUtils;
import org.terasology.config.Config;
import org.terasology.math.AABB;
import org.terasology.math.Direction;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Quat4f;
import org.terasology.registry.CoreRegistry;

/**
 * Camera base class.
 *
 */
public abstract class Camera {

    protected static final Vector3fc FORWARD = JomlUtil.from(Direction.FORWARD.getVector3f());

    /* CAMERA PARAMETERS */
    protected final Vector3f position = new Vector3f(0, 0, 0);
    protected final Vector3f up = JomlUtil.from(Direction.UP.getVector3f());
    protected final Vector3f viewingDirection = new Vector3f(FORWARD);
    protected final Vector3f viewingAxis = JomlUtil.from(Direction.LEFT.getVector3f());
    protected float viewingAngle;

    protected float zNear = 0.1f;
    // TODO: This is too large, but many properties have to be adjusted if it changes
    protected float zFar = 5000.0f;

    protected float targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    protected float activeFov = targetFov / 4f;

    /* VIEW FRUSTUM */
    protected final ViewFrustum viewFrustum = new ViewFrustum();
    protected final ViewFrustum viewFrustumReflected = new ViewFrustum();

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

        viewFrustum.updateFrustum(viewMatrix.get(BufferUtils.createFloatBuffer(16)), projectionMatrix.get(BufferUtils.createFloatBuffer(16)));
        viewFrustumReflected.updateFrustum(viewMatrixReflected.get(BufferUtils.createFloatBuffer(16)), projectionMatrix.get(BufferUtils.createFloatBuffer(16)));
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

    /**
     * Get the orientation of the camera.
     * @return the orientation
     */
    public Quat4f getOrientation() {
        return new Quat4f(JomlUtil.from(viewingAxis), viewingAngle);
    }

    public Quaternionf getOrientation(Quaternionf orientation) {
        return orientation.set(new AxisAngle4f(viewingAngle, viewingAxis));
    }

    public void setOrientation(Quat4f orientation) {
        Quaternionf newOrientation = JomlUtil.from(orientation);
        newOrientation.transform(FORWARD, viewingDirection);
        AxisAngle4f axisAngle = new AxisAngle4f(newOrientation);
        viewingAxis.set(axisAngle.x, axisAngle.y, axisAngle.z);
        viewingAngle = axisAngle.angle;
    }

    public void setOrientation(Quaternionfc orientation) {
        orientation.transform(FORWARD, viewingDirection);
        AxisAngle4f axisAngle = new AxisAngle4f(orientation);
        viewingAxis.set(axisAngle.x, axisAngle.y, axisAngle.z);
        viewingAngle = axisAngle.angle;
    }

    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    public ViewFrustum getViewFrustumReflected() {
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

    /**
     *
     * @param aabb
     * @return
     * @deprecated This method is scheduled for removal in an upcoming version. Use the JOML implementation instead:
     *       {@link #hasInSight(AABBf)}.
     */
    @Deprecated
    public boolean hasInSight(AABB aabb) {
        return viewFrustum.intersects(aabb);
    }

    public boolean hasInSight(AABBf aabb) {
        return viewFrustum.intersects(aabb);
    }
}
