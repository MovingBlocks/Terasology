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
    protected final Vector3f _position = new Vector3f(0, 0, 0);
    protected final Vector3f _up = new Vector3f(0, 1, 0);
    protected final Vector3f _viewingDirection = new Vector3f(1, 0, 0);

    protected float _targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    protected float _activeFov = _targetFov / 4f;

    /* VIEW FRUSTUM */
    protected final ViewFrustum _viewFrustum = new ViewFrustum();

    /* MATRICES */
    protected Matrix4f _projectionMatrix = new Matrix4f();
    protected Matrix4f _normViewMatrix = new Matrix4f();
    protected Matrix4f _viewMatrix = new Matrix4f();
    protected Matrix4f _viewProjectionMatrix = new Matrix4f();
    protected Matrix4f _inverseViewProjectionMatrix = new Matrix4f();
    protected Matrix4f _prevViewProjectionMatrix = new Matrix4f();

    /* ETC */
    protected boolean _reflected = false;

    /**
     * Applies the projection and modelview matrix.
     */
    public void lookThrough() {
        loadProjectionMatrix();
        loadModelViewMatrix();

        if (_reflected) {
            glTranslatef(0.0f, 2f * ((float) -_position.y + 32f), 0.0f);
            glScalef(1.0f, -1.0f, 1.0f);
        }
    }


    /**
     * Applies the projection and the normalized modelview matrix (positioned at the origin without any offset like bobbing) .
     */
    public void lookThroughNormalized() {
        loadProjectionMatrix();
        loadNormalizedModelViewMatrix();

        if (_reflected) {
            glScalef(1.0f, -1.0f, 1.0f);
        }
    }

    public abstract void loadProjectionMatrix();

    public abstract void loadModelViewMatrix();

    public abstract void loadNormalizedModelViewMatrix();

    public abstract void updateMatrices();

    public abstract void updateMatrices(float overrideFov);

    public Vector3f getPosition() {
        return _position;
    }

    public Vector3f getViewingDirection() {
        return _viewingDirection;
    }

    public Vector3f getUp() {
        return _up;
    }

    public ViewFrustum getViewFrustum() {
        return _viewFrustum;
    }

    public void update(float delta) {
        double diff = Math.abs(_activeFov - _targetFov);
        if (diff < 1.0) {
            _activeFov = _targetFov;
            return;
        }
        if (_activeFov < _targetFov) {
            _activeFov += 50.0 * delta;
            if (_activeFov >= _targetFov) {
                _activeFov = _targetFov;
            }
        } else if (_activeFov > _targetFov) {
            _activeFov -= 50.0 * delta;
            if (_activeFov <= _targetFov) {
                _activeFov = _targetFov;
            }
        }
    }

    public void extendFov(float fov) {
        _targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView() + fov;
    }

    public void resetFov() {
        _targetFov = CoreRegistry.get(Config.class).getRendering().getFieldOfView();
    }

    public void setReflected(boolean reflected) {
        _reflected = reflected;
    }

    public float getClipHeight() {
        return 31.5f;
    }

    public Matrix4f getViewMatrix() {
        return _viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return _projectionMatrix;
    }

    public Matrix4f getViewProjectionMatrix() {
        return _viewProjectionMatrix;
    }

    public Matrix4f getInverseViewProjectionMatrix() {
        return _inverseViewProjectionMatrix;
    }

    public Matrix4f getPrevViewProjectionMatrix() {
        return _prevViewProjectionMatrix;
    }
}
