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

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.world.WorldRenderer;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Camera which can be used to render stereoscopic images of the scene for the Oculus Rift.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class OculusStereoCamera extends Camera {

    protected Matrix4f projectionMatrixLeftEye = new Matrix4f();
    protected Matrix4f projectionMatrixRightEye = new Matrix4f();

    protected Matrix4f viewMatrixLeftEye = new Matrix4f();
    protected Matrix4f viewMatrixRightEye = new Matrix4f();

    protected Matrix4f viewProjectionMatrixLeftEye = new Matrix4f();
    protected Matrix4f viewProjectionMatrixRightEye = new Matrix4f();

    public Matrix4f getViewProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_LEFT_EYE) {
            return viewProjectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_RIGHT_EYE) {
            return viewProjectionMatrixRightEye;
        }

        return null;
    }

    public Matrix4f getViewMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_LEFT_EYE) {
            return viewMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_RIGHT_EYE) {
            return viewMatrixRightEye;
        }

        return null;
    }

    public Matrix4f getProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_LEFT_EYE) {
            return projectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.WRS_OCULUS_RIGHT_EYE) {
            return projectionMatrixRightEye;
        }

        return null;
    }

    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(getProjectionMatrix()));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(getViewMatrix()));
        _viewFrustum.updateFrustum();
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(_normViewMatrix));
        _viewFrustum.updateFrustum();
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(_activeFov);
    }

    public void updateMatrices(float overrideFov) {
        final float ocVerticalRes = 800.0f;
        final float ocHorizontalRes = 1280.0f;
        final float ocVerticalScreenSize = 0.0935f;
        final float ocHorizontalScreenSize = 0.14976f;
        final float ocEyeToScreenDistance = 0.041f;
        final float ocLensSeparationDistance = 0.0635f;
        final float ocInterpupillaryDistance = 0.064f;

        final float ocHalfScreenDistance = ocHorizontalScreenSize * 0.5f;
        final float ocYFov = 2.0f * (float) Math.atan(ocHalfScreenDistance/ocEyeToScreenDistance);

        final float ocAspectRatio = (ocHorizontalRes * 0.5f) / ocVerticalRes;

        float ocViewCenter = ocHorizontalScreenSize * 0.25f;
        float ocEyeProjectionShift = ocViewCenter - ocLensSeparationDistance * 0.5f;
        float ocProjectionCenterOffset = 4.0f * ocEyeProjectionShift / ocHorizontalScreenSize;

        _projectionMatrix = TeraMath.createPerspectiveProjectionMatrix(ocYFov, ocAspectRatio, 0.1f, 5000.0f);

        Matrix4f projTranslationLeftEye = new Matrix4f();
        projTranslationLeftEye.setIdentity();

        projTranslationLeftEye.setTranslation(new Vector3f(ocProjectionCenterOffset, 0.0f, 0.0f));

        Matrix4f projTranslationRightEye = new Matrix4f();
        projTranslationRightEye.setIdentity();

        projTranslationRightEye.setTranslation(new Vector3f(-ocProjectionCenterOffset, 0.0f, 0.0f));

        projectionMatrixLeftEye.mul(projTranslationLeftEye, _projectionMatrix);
        projectionMatrixRightEye.mul(projTranslationRightEye, _projectionMatrix);

        _viewMatrix = TeraMath.createViewMatrix(0f, 0.0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x, _up.y, _up.z);
        _normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, _viewingDirection.x, _viewingDirection.y, _viewingDirection.z, _up.x, _up.y, _up.z);

        final float halfIPD = ocInterpupillaryDistance * 0.5f;

        Matrix4f viewTranslationLeftEye = new Matrix4f();
        viewTranslationLeftEye.setIdentity();

        viewTranslationLeftEye.setTranslation(new Vector3f(halfIPD, 0.0f, 0.0f));

        Matrix4f viewTranslationRightEye = new Matrix4f();
        viewTranslationRightEye.setIdentity();

        viewTranslationRightEye.setTranslation(new Vector3f(-halfIPD, 0.0f, 0.0f));

        viewMatrixLeftEye.mul(_viewMatrix, viewTranslationLeftEye);
        viewMatrixRightEye.mul(_viewMatrix, viewTranslationRightEye);

        viewProjectionMatrixLeftEye = TeraMath.calcViewProjectionMatrix(viewMatrixLeftEye, projectionMatrixLeftEye);
        viewProjectionMatrixRightEye = TeraMath.calcViewProjectionMatrix(viewMatrixRightEye, projectionMatrixRightEye);

        _prevViewProjectionMatrix = new Matrix4f(_viewProjectionMatrix);
        _viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(_viewMatrix, _projectionMatrix);
        _inverseViewProjectionMatrix.invert(_viewProjectionMatrix);
    }
}
