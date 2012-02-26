/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

import org.terasology.logic.manager.SettingsManager;
import org.terasology.model.structures.ViewFrustum;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3d;

/**
 * Provides global access to fonts.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class Camera {

    public static final float FOV = ((Double) SettingsManager.getInstance().getUserSetting("Game.Player.fov")).floatValue();

    /* CAMERA PARAMETERS */
    protected final Vector3d _position = new Vector3d();
    protected final Vector3d _up = new Vector3d(0, 1, 0);
    protected final Vector3d _viewingDirection = new Vector3d();
    protected final Matrix4f _viewMatrix = new Matrix4f();

    protected float _targetFov = FOV;
    protected float _activeFov = FOV - 20f;

    /* VIEW FRUSTUM */
    protected final ViewFrustum _viewFrustum = new ViewFrustum();
    protected boolean _frustumNeedsUpdate = false;

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

    public abstract void loadProjectionMatrix(float fov);

    public void loadProjectionMatrix() {
        loadProjectionMatrix(_activeFov);
    }

    public abstract void loadModelViewMatrix();

    public abstract void loadNormalizedModelViewMatrix();


    public Vector3d getPosition() {
        return _position;
    }

    public Vector3d getViewingDirection() {
        return _viewingDirection;
    }

    public Vector3d getUp() {
        return _up;
    }

    public ViewFrustum getViewFrustum() {
        if (_frustumNeedsUpdate) {
            _viewFrustum.updateFrustum();
            _frustumNeedsUpdate = false;
        }

        return _viewFrustum;
    }

    public void update() {
        if (_activeFov < _targetFov) {
            _activeFov += 0.5;
        } else if (_activeFov > _targetFov) {
            _activeFov -= 0.5;
        }
    }

    public void extendFov(float fov) {
        _targetFov = FOV + fov;
    }

    public void resetFov() {
        _targetFov = FOV;
    }

    public Matrix4f getViewMatrix() {
        return _viewMatrix;
    }
}
