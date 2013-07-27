/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.CoreRegistry;
import org.terasology.math.TeraMath;
import org.terasology.rendering.oculusVr.OculusVrHelper;
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

    protected Matrix4f inverseProjectionMatrixLeftEye = new Matrix4f();
    protected Matrix4f inverseProjectionMatrixRightEye = new Matrix4f();

    protected Matrix4f inverseViewProjectionMatrixLeftEye = new Matrix4f();
    protected Matrix4f inverseViewProjectionMatrixRightEye = new Matrix4f();

    protected Matrix4f viewMatrixLeftEye = new Matrix4f();
    protected Matrix4f viewMatrixRightEye = new Matrix4f();

    protected Matrix4f viewMatrixReflectedLeftEye = new Matrix4f();
    protected Matrix4f viewMatrixReflectedRightEye = new Matrix4f();

    protected ViewFrustum viewFrustumLeftEye = new ViewFrustum();
    protected ViewFrustum viewFrustumRightEye = new ViewFrustum();
    protected ViewFrustum viewFrustumReflectedLeftEye = new ViewFrustum();
    protected ViewFrustum viewFrustumReflectedRightEye = new ViewFrustum();

    protected Matrix4f viewProjectionMatrixLeftEye = new Matrix4f();
    protected Matrix4f viewProjectionMatrixRightEye = new Matrix4f();

    protected Matrix4f projTranslationLeftEye = new Matrix4f();
    protected Matrix4f projTranslationRightEye = new Matrix4f();
    protected Matrix4f viewTranslationLeftEye = new Matrix4f();
    protected Matrix4f viewTranslationRightEye = new Matrix4f();

    public void updateFrustum() {
        super.updateFrustum();

        viewFrustumLeftEye.updateFrustum(TeraMath.matrixToFloatBuffer(viewMatrixLeftEye), TeraMath.matrixToFloatBuffer(projectionMatrixLeftEye));
        viewFrustumRightEye.updateFrustum(TeraMath.matrixToFloatBuffer(viewMatrixRightEye), TeraMath.matrixToFloatBuffer(projectionMatrixRightEye));
        viewFrustumReflectedLeftEye.updateFrustum(TeraMath.matrixToFloatBuffer(viewMatrixReflectedLeftEye), TeraMath.matrixToFloatBuffer(projectionMatrixLeftEye));
        viewFrustumReflectedRightEye.updateFrustum(TeraMath.matrixToFloatBuffer(viewMatrixReflectedRightEye), TeraMath.matrixToFloatBuffer(projectionMatrixRightEye));
    }

    @Override
    public boolean isBobbingAllowed() {
        return false;
    }

    public ViewFrustum getViewFrustum() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return viewFrustumLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return viewFrustumRightEye;
        }

        return null;
    }

    public ViewFrustum getViewFrustumReflected() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return viewFrustumReflectedLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return viewFrustumReflectedRightEye;
        }

        return null;
    }

    public Matrix4f getViewProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return viewProjectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return viewProjectionMatrixRightEye;
        }

        return null;
    }

    public Matrix4f getViewMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            if (!isReflected()) {
                return viewMatrixLeftEye;
            }
            return viewMatrixReflectedLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            if (!isReflected()) {
                return viewMatrixRightEye;
            }
            return viewMatrixReflectedRightEye;
        }

        return null;
    }

    public Matrix4f getProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return projectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return projectionMatrixRightEye;
        }

        return null;
    }

    public Matrix4f getInverseProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return inverseProjectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return inverseProjectionMatrixRightEye;
        }

        return null;
    }

    public Matrix4f getInverseViewProjectionMatrix() {
        WorldRenderer.WorldRenderingStage renderingStage = CoreRegistry.get(WorldRenderer.class).getCurrentRenderStage();

        if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_LEFT_EYE) {
            return inverseViewProjectionMatrixLeftEye;
        } else if (renderingStage == WorldRenderer.WorldRenderingStage.OCULUS_RIGHT_EYE) {
            return inverseViewProjectionMatrixRightEye;
        }

        return null;
    }

    @Deprecated
    public void loadProjectionMatrix() {
        glMatrixMode(GL_PROJECTION);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(getProjectionMatrix()));
        glMatrixMode(GL11.GL_MODELVIEW);
    }

    @Deprecated
    public void loadModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(getViewMatrix()));
    }

    @Deprecated
    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToFloatBuffer(normViewMatrix));
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    public void updateMatrices(float fov) {
        prevViewProjectionMatrix.set(viewProjectionMatrix);

        // Nothing to do...
        if (cachedPosition.equals(getPosition()) && cachedViewigDirection.equals(getViewingDirection())
                && cachedZFar == zFar && cachedZNear == zNear) {
            return;
        }

        projectionMatrix = TeraMath.createPerspectiveProjectionMatrix(OculusVrHelper.getyFov(), OculusVrHelper.getAspectRatio(), zNear, zFar);

        projTranslationLeftEye.setIdentity();
        projTranslationLeftEye.setTranslation(new Vector3f(OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f));

        projTranslationRightEye.setIdentity();
        projTranslationRightEye.setTranslation(new Vector3f(-OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f));

        projectionMatrixLeftEye.mul(projTranslationLeftEye, projectionMatrix);
        projectionMatrixRightEye.mul(projTranslationRightEye, projectionMatrix);

        viewMatrix = TeraMath.createViewMatrix(0f, 0.0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);
        normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);

        reflectionMatrix.setRow(0, 1.0f, 0.0f, 0.0f, 0.0f);
        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 2f * (-position.y + 32f));
        reflectionMatrix.setRow(2, 0.0f, 0.0f, 1.0f, 0.0f);
        reflectionMatrix.setRow(3, 0.0f, 0.0f, 0.0f, 1.0f);
        viewMatrixReflected.mul(viewMatrix, reflectionMatrix);

        reflectionMatrix.setRow(1, 0.0f, -1.0f, 0.0f, 0.0f);
        normViewMatrixReflected.mul(normViewMatrix, reflectionMatrix);

        final float halfIPD = OculusVrHelper.getInterpupillaryDistance() * 0.5f;

        viewTranslationLeftEye.setIdentity();
        viewTranslationLeftEye.setTranslation(new Vector3f(halfIPD, 0.0f, 0.0f));

        viewTranslationRightEye.setIdentity();
        viewTranslationRightEye.setTranslation(new Vector3f(-halfIPD, 0.0f, 0.0f));

        viewMatrixLeftEye.mul(viewMatrix, viewTranslationLeftEye);
        viewMatrixRightEye.mul(viewMatrix, viewTranslationRightEye);

        viewMatrixReflectedLeftEye.mul(viewMatrixReflected, viewTranslationLeftEye);
        viewMatrixReflectedRightEye.mul(viewMatrixReflected, viewTranslationRightEye);

        viewProjectionMatrixLeftEye = TeraMath.calcViewProjectionMatrix(viewMatrixLeftEye, projectionMatrixLeftEye);
        viewProjectionMatrixRightEye = TeraMath.calcViewProjectionMatrix(viewMatrixRightEye, projectionMatrixRightEye);

        inverseViewProjectionMatrixLeftEye.invert(viewProjectionMatrixLeftEye);
        inverseViewProjectionMatrixRightEye.invert(viewProjectionMatrixRightEye);

        inverseProjectionMatrixLeftEye.invert(projectionMatrixLeftEye);
        inverseProjectionMatrixRightEye.invert(projectionMatrixRightEye);

        // Used for dirty checks
        cachedPosition.set(getPosition());
        cachedViewigDirection.set(getViewingDirection());
        cachedZFar = zFar;
        cachedZNear = zNear;

        updateFrustum();
    }
}
