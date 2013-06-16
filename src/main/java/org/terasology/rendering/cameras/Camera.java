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

import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.model.structures.ViewFrustum;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Camera base class.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Camera {

    /* CAMERA PARAMETERS */
    protected final Vector3f position = new Vector3f(0, 0, 0);
    protected final Vector3f up = new Vector3f(0, 1, 0);
    protected final Vector3f viewingDirection = new Vector3f(1, 0, 0);

    protected float targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    protected float activeFov = targetFov / 4f;

    /* VIEW FRUSTUM */
    protected final ViewFrustum viewFrustum = new ViewFrustum();

    /* MATRICES */
    protected Matrix4f projectionMatrix = new Matrix4f();
    protected Matrix4f normViewMatrix = new Matrix4f();
    protected Matrix4f viewMatrix = new Matrix4f();
    protected Matrix4f viewProjectionMatrix = new Matrix4f();
    protected Matrix4f inverseViewProjectionMatrix = new Matrix4f();
    protected Matrix4f prevViewProjectionMatrix = new Matrix4f();

    /* USED FOR DIRTY CHECKS */
    protected Vector3f previousPosition = new Vector3f();
    protected Vector3f previousViewingDirection = new Vector3f();
    protected float lastFov = 0.0f;

    /* ETC */
    protected boolean reflected = false;

    /**
     * Applies the projection and modelview matrix.
     */
    public void lookThrough() {
        loadProjectionMatrix();
        loadModelViewMatrix();

        if (reflected) {
            glTranslatef(0.0f, 2f * ((float) -position.y + 32f), 0.0f);
            glScalef(1.0f, -1.0f, 1.0f);
        }
    }


    /**
     * Applies the projection and the normalized modelview matrix (positioned at the origin without any offset like bobbing) .
     */
    public void lookThroughNormalized() {
        loadProjectionMatrix();
        loadNormalizedModelViewMatrix();

        if (reflected) {
            glScalef(1.0f, -1.0f, 1.0f);
        }
    }

    public void updateFrustum() {
        if (getViewMatrix() == null || getProjectionMatrix() == null) {
            return;
        }

        viewFrustum.updateFrustum(TeraMath.matrixToBuffer(getViewMatrix()), TeraMath.matrixToBuffer(getProjectionMatrix()));
    }

    public abstract void loadProjectionMatrix();

    public abstract void loadModelViewMatrix();

    public abstract void loadNormalizedModelViewMatrix();

    public abstract void updateMatrices();

    public abstract void updateMatrices(float fov);

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getViewingDirection() {
        return viewingDirection;
    }

    public Vector3f getUp() {
        return up;
    }

    public ViewFrustum getViewFrustum() {
        return viewFrustum;
    }

    public void update(float delta) {
        double diff = Math.abs(activeFov - targetFov);
        if (diff < 1.0) {
            activeFov = targetFov;
            return;
        }
        if (activeFov < targetFov) {
            activeFov += 50.0 * delta;
            if (activeFov >= targetFov) {
                activeFov = targetFov;
            }
        } else if (activeFov > targetFov) {
            activeFov -= 50.0 * delta;
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

    public float getClipHeight() {
        return 31.5f;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getViewProjectionMatrix() {
        return viewProjectionMatrix;
    }

    public Matrix4f getInverseViewProjectionMatrix() {
        return inverseViewProjectionMatrix;
    }

    public Matrix4f getPrevViewProjectionMatrix() {
        return prevViewProjectionMatrix;
    }
}
