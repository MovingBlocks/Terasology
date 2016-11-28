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

import org.terasology.rendering.openvrprovider.OpenVRProvider;
import org.lwjgl.opengl.GL11;
import org.terasology.math.MatrixUtils;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRenderer.RenderingStage;

import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glMatrixMode;

/**
 * Camera which can be used to render stereoscopic images of the scene for VR.
 *
 */
public class OpenVRStereoCamera extends Camera {

    private Matrix4f projectionMatrixLeftEye = new Matrix4f();
    private Matrix4f projectionMatrixRightEye = new Matrix4f();

    private Matrix4f inverseProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f inverseProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f inverseViewProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f inverseViewProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f viewMatrixLeftEye = new Matrix4f();
    private Matrix4f viewMatrixRightEye = new Matrix4f();

    private Matrix4f viewMatrixReflectedLeftEye = new Matrix4f();
    private Matrix4f viewMatrixReflectedRightEye = new Matrix4f();

    private ViewFrustum viewFrustumLeftEye = new ViewFrustum();
    private ViewFrustum viewFrustumRightEye = new ViewFrustum();
    private ViewFrustum viewFrustumReflectedLeftEye = new ViewFrustum();
    private ViewFrustum viewFrustumReflectedRightEye = new ViewFrustum();

    private Matrix4f viewProjectionMatrixLeftEye = new Matrix4f();
    private Matrix4f viewProjectionMatrixRightEye = new Matrix4f();

    private Matrix4f viewTranslationLeftEye = new Matrix4f();
    private Matrix4f viewTranslationRightEye = new Matrix4f();
    private OpenVRProvider vrProvider;

    public OpenVRStereoCamera(OpenVRProvider provider) {
        vrProvider = provider;
        // OpenVR's projection matrix is such that this is approximately true.
        zFar = 400.0f;
    }

    @Override
    public void updateFrustum() {
        super.updateFrustum();

        viewFrustumLeftEye.updateFrustum(MatrixUtils.matrixToFloatBuffer(viewMatrixLeftEye), MatrixUtils.matrixToFloatBuffer(projectionMatrixLeftEye));
        viewFrustumRightEye.updateFrustum(MatrixUtils.matrixToFloatBuffer(viewMatrixRightEye), MatrixUtils.matrixToFloatBuffer(projectionMatrixRightEye));
        viewFrustumReflectedLeftEye.updateFrustum(MatrixUtils.matrixToFloatBuffer(viewMatrixReflectedLeftEye), MatrixUtils.matrixToFloatBuffer(projectionMatrixLeftEye));
        viewFrustumReflectedRightEye.updateFrustum(MatrixUtils.matrixToFloatBuffer(viewMatrixReflectedRightEye), MatrixUtils.matrixToFloatBuffer(projectionMatrixRightEye));
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }

    @Override
    public ViewFrustum getViewFrustum() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewFrustumLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewFrustumRightEye;
        }

        return null;
    }

    @Override
    public ViewFrustum getViewFrustumReflected() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewFrustumReflectedLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewFrustumReflectedRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getViewProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return viewProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return viewProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getViewMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            if (!isReflected()) {
                return viewMatrixLeftEye;
            }
            return viewMatrixReflectedLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            if (!isReflected()) {
                return viewMatrixRightEye;
            }
            return viewMatrixReflectedRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return projectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return projectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getInverseProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return inverseProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return inverseProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    public Matrix4f getInverseViewProjectionMatrix() {
        RenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == RenderingStage.LEFT_EYE) {
            return inverseViewProjectionMatrixLeftEye;
        } else if (renderingStage == RenderingStage.RIGHT_EYE) {
            return inverseViewProjectionMatrixRightEye;
        }

        return null;
    }

    @Override
    @Deprecated
    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(getProjectionMatrix()));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    @Deprecated
    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(getViewMatrix()));
    }

    @Override
    @Deprecated
    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(MatrixUtils.matrixToFloatBuffer(normViewMatrix));
    }

    @Override
    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    @Override
    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    private void jomlMatrix4f(org.joml.Matrix4f matrixInput, org.terasology.math.geom.Matrix4f matrixOut) {
        matrixOut.set(0, 0, matrixInput.m00());
        matrixOut.set(0, 1, matrixInput.m10());
        matrixOut.set(0, 2, matrixInput.m20());
        matrixOut.set(0, 3, matrixInput.m30());
        matrixOut.set(1, 0, matrixInput.m01());
        matrixOut.set(1, 1, matrixInput.m11());
        matrixOut.set(1, 2, matrixInput.m21());
        matrixOut.set(1, 3, matrixInput.m31());
        matrixOut.set(2, 0, matrixInput.m02());
        matrixOut.set(2, 1, matrixInput.m12());
        matrixOut.set(2, 2, matrixInput.m22());
        matrixOut.set(2, 3, matrixInput.m32());
        matrixOut.set(3, 0, matrixInput.m03());
        matrixOut.set(3, 1, matrixInput.m13());
        matrixOut.set(3, 2, matrixInput.m23());
        matrixOut.set(3, 3, matrixInput.m33());
    }

    @Override
    public void updateMatrices(float fov) {
        prevViewProjectionMatrix.set(viewProjectionMatrix);

        org.joml.Matrix4f leftEyeProjection = vrProvider.vrState.getEyeProjectionMatrix(0);
        org.joml.Matrix4f rightEyeProjection = vrProvider.vrState.getEyeProjectionMatrix(1);
        org.joml.Matrix4f leftEyePose = vrProvider.vrState.getEyePose(0);
        org.joml.Matrix4f rightEyePose = vrProvider.vrState.getEyePose(1);
        float halfIPD = (float) Math.sqrt(Math.pow(leftEyePose.m30() - rightEyePose.m30(), 2)
                + Math.pow(leftEyePose.m31() - rightEyePose.m31(), 2)
                + Math.pow(leftEyePose.m32() - rightEyePose.m32(), 2)) / 2.0f;
        leftEyePose = leftEyePose.invert(); // view matrix is inverse of pose matrix
        rightEyePose = rightEyePose.invert();
        if (Math.sqrt(Math.pow(leftEyePose.m30(), 2) + Math.pow(leftEyePose.m31(), 2) + Math.pow(leftEyePose.m32(), 2))  < 0.25)  {
            return;
        }
        jomlMatrix4f(leftEyeProjection, projectionMatrixLeftEye);
        jomlMatrix4f(rightEyeProjection, projectionMatrixRightEye);
        projectionMatrix = projectionMatrixLeftEye;

        jomlMatrix4f(leftEyePose, viewMatrixLeftEye);
        jomlMatrix4f(rightEyePose, viewMatrixRightEye);

        viewMatrix = viewMatrixLeftEye;
        normViewMatrix = viewMatrixLeftEye;

        reflectionMatrix.setRow(0, 1.0f, 0.0f, 0.0f, 0.0f);
        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 2f * (-position.y + 32f));
        reflectionMatrix.setRow(2, 0.0f, 0.0f, 1.0f, 0.0f);
        reflectionMatrix.setRow(3, 0.0f, 0.0f, 0.0f, 1.0f);


        viewMatrixReflected.mul(viewMatrix, reflectionMatrix);

        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 0.0f);
        normViewMatrixReflected.mul(normViewMatrix, reflectionMatrix);

        viewTranslationLeftEye.setIdentity();
        viewTranslationLeftEye.setTranslation(new Vector3f(halfIPD, 0.0f, 0.0f));

        viewTranslationRightEye.setIdentity();
        viewTranslationRightEye.setTranslation(new Vector3f(-halfIPD, 0.0f, 0.0f));


        viewMatrixReflectedLeftEye.mul(viewMatrixReflected, viewTranslationLeftEye);
        viewMatrixReflectedRightEye.mul(viewMatrixReflected, viewTranslationRightEye);

        viewProjectionMatrixLeftEye = MatrixUtils.calcViewProjectionMatrix(viewMatrixLeftEye, projectionMatrixLeftEye);
        viewProjectionMatrixRightEye = MatrixUtils.calcViewProjectionMatrix(viewMatrixRightEye, projectionMatrixRightEye);

        inverseViewProjectionMatrixLeftEye.invert(viewProjectionMatrixLeftEye);
        inverseViewProjectionMatrixRightEye.invert(viewProjectionMatrixRightEye);

        inverseProjectionMatrixLeftEye.invert(projectionMatrixLeftEye);
        inverseProjectionMatrixRightEye.invert(projectionMatrixRightEye);

        updateFrustum();
    }
}
