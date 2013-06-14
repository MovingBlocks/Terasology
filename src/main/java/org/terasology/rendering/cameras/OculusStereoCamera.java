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
        viewFrustum.updateFrustum();
    }

    public void loadNormalizedModelViewMatrix() {
        glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadMatrix(TeraMath.matrixToBuffer(normViewMatrix));
        viewFrustum.updateFrustum();
    }

    public void update(float deltaT) {
        super.update(deltaT);
        updateMatrices();
    }

    public void updateMatrices() {
        updateMatrices(activeFov);
    }

    public void updateMatrices(float overrideFov) {

        projectionMatrix = TeraMath.createPerspectiveProjectionMatrix(OculusVrHelper.getyFov(), OculusVrHelper.getAspectRatio(), 0.1f, 5000.0f);

        Matrix4f projTranslationLeftEye = new Matrix4f();
        projTranslationLeftEye.setIdentity();

        projTranslationLeftEye.setTranslation(new Vector3f(OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f));

        Matrix4f projTranslationRightEye = new Matrix4f();
        projTranslationRightEye.setIdentity();

        projTranslationRightEye.setTranslation(new Vector3f(-OculusVrHelper.getProjectionCenterOffset(), 0.0f, 0.0f));

        projectionMatrixLeftEye.mul(projTranslationLeftEye, projectionMatrix);
        projectionMatrixRightEye.mul(projTranslationRightEye, projectionMatrix);

        viewMatrix = TeraMath.createViewMatrix(0f, 0.0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);
        normViewMatrix = TeraMath.createViewMatrix(0f, 0f, 0f, viewingDirection.x, viewingDirection.y, viewingDirection.z, up.x, up.y, up.z);

        final float halfIPD = OculusVrHelper.getInterpupillaryDistance() * 0.5f;

        Matrix4f viewTranslationLeftEye = new Matrix4f();
        viewTranslationLeftEye.setIdentity();

        viewTranslationLeftEye.setTranslation(new Vector3f(halfIPD, 0.0f, 0.0f));

        Matrix4f viewTranslationRightEye = new Matrix4f();
        viewTranslationRightEye.setIdentity();

        viewTranslationRightEye.setTranslation(new Vector3f(-halfIPD, 0.0f, 0.0f));

        viewMatrixLeftEye.mul(viewMatrix, viewTranslationLeftEye);
        viewMatrixRightEye.mul(viewMatrix, viewTranslationRightEye);

        viewProjectionMatrixLeftEye = TeraMath.calcViewProjectionMatrix(viewMatrixLeftEye, projectionMatrixLeftEye);
        viewProjectionMatrixRightEye = TeraMath.calcViewProjectionMatrix(viewMatrixRightEye, projectionMatrixRightEye);

        prevViewProjectionMatrix = new Matrix4f(viewProjectionMatrix);
        viewProjectionMatrix = TeraMath.calcViewProjectionMatrix(viewMatrix, projectionMatrix);
        inverseViewProjectionMatrix.invert(viewProjectionMatrix);
    }
}
