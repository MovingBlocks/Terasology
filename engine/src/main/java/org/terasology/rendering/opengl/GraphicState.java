/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.rendering.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.opengl.FBO.Dimensions;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.GL_FRAMEBUFFER_EXT;
import static org.lwjgl.opengl.EXTFramebufferObject.glBindFramebufferEXT;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL20.glStencilOpSeparate;

/**
 * Created by manu on 18.04.2015.
 */
public class GraphicState {

    private LwjglRenderingProcess renderingProcess;
    private Dimensions fullScale;
    private Buffers buffers  = new Buffers();

    public GraphicState(LwjglRenderingProcess renderingProcess) {
        // a reference to the renderingProcess is not strictly necessary, as it is used only
        // in refreshDynamicFBOs() and it could be passed as argument to it. We do it this
        // way however to maintain similarity with the way the PostProcessor will work.
        // TODO: update the comment above when the PostProcessor is in place.
        this.renderingProcess = renderingProcess;
    }

    public void dispose() {
        renderingProcess = null;
        fullScale = null;
        buffers = null;
    }

    public void refreshDynamicFBOs() {
        buffers.sceneOpaque               = renderingProcess.getFBO("sceneOpaque");
        buffers.sceneReflectiveRefractive = renderingProcess.getFBO("sceneReflectiveRefractive");
        buffers.sceneReflected            = renderingProcess.getFBO("sceneReflected");
        fullScale = buffers.sceneOpaque.dimensions();
    }

    public void setSceneShadowMap(FBO newShadowMap) {
        buffers.sceneShadowMap = newShadowMap;
    }

    public void initialClearing() {
        buffers.sceneOpaque.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        buffers.sceneReflectiveRefractive.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        bindDisplay();
    }

    public void preRenderSetupSceneOpaque() {
        buffers.sceneOpaque.bind();
        setRenderBufferMask(buffers.sceneOpaque, true, true, true);
    }

    public void postRenderCleanupSceneOpaque() {
        setRenderBufferMask(buffers.sceneOpaque, true, true, true);
        bindDisplay();
    }

    public void enableWireframeIf(boolean wireframeIsEnabledInRenderingDebugConfig) {
        if (wireframeIsEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    public void disableWireframeIf(boolean wireframeIsEnabledInRenderingDebugConfig) {
        if (wireframeIsEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    public void preRenderSetupSceneShadowMap() {
        buffers.sceneShadowMap.bind();

        setViewportToSizeOf(buffers.sceneShadowMap);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL_CULL_FACE);
    }

    public void postRenderCleanupSceneShadowMap() {
        GL11.glEnable(GL_CULL_FACE);
        bindDisplay();
        setViewportToWholeDisplay();
    }

    public void preRenderSetupReflectedScene() {
        buffers.sceneReflected.bind();

        setViewportToSizeOf(buffers.sceneReflected);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glCullFace(GL11.GL_FRONT);
    }

    public void postRenderCleanupReflectedScene() {
        GL11.glCullFace(GL11.GL_BACK);
        bindDisplay();
        setViewportToWholeDisplay();
    }

    public void preRenderSetupBackdrop() {
        setRenderBufferMask(buffers.sceneOpaque, true, false, false);
    }

    public void midRenderChangesBackdrop() {
        setRenderBufferMask(buffers.sceneOpaque, true, true, true);
    }

    public void postRenderCleanupBackdrop() {
        buffers.sceneOpaque.bind();
    }

    public void preRenderSetupFirstPerson() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
    }

    public void postRenderClenaupFirstPerson() {
        GL11.glDepthFunc(GL_LEQUAL);
        GL11.glPopMatrix();
    }

    // TODO: figure how lighting works and what this does
    public void preRenderSetupLightGeometryStencil() {
        buffers.sceneOpaque.bind();
        setRenderBufferMask(buffers.sceneOpaque, false, false, false);
        glDepthMask(false);

        glClear(GL_STENCIL_BUFFER_BIT);

        glCullFace(GL_FRONT);
        glDisable(GL_CULL_FACE);

        glEnable(GL_STENCIL_TEST);
        glStencilFunc(GL_ALWAYS, 0, 0);

        glStencilOpSeparate(GL_BACK, GL_KEEP, GL_INCR, GL_KEEP);
        glStencilOpSeparate(GL_FRONT, GL_KEEP, GL_DECR, GL_KEEP);
    }

    // TODO: figure how lighting works and what this does
    public void postRenderCleanupLightGeometryStencil() {
        setRenderBufferMask(buffers.sceneOpaque, true, true, true);
        bindDisplay();
    }

    public void preRenderSetupLightGeometry() {
        buffers.sceneOpaque.bind();

        // Only write to the light buffer
        setRenderBufferMask(buffers.sceneOpaque, false, false, true);

        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);

        glDepthMask(true);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
    }

    public void postRenderCleanupLightGeometry() {
        glDisable(GL_STENCIL_TEST);
        glCullFace(GL_BACK);

        bindDisplay();
    }

    public void preRenderSetupDirectionalLights() {
        buffers.sceneOpaque.bind();
    }

    public void postRenderCleanupDirectionalLights() {
        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_DEPTH_TEST);

        setRenderBufferMask(buffers.sceneOpaque, true, true, true);
        bindDisplay();
    }

    public void preRenderSetupSceneReflectiveRefractive(boolean isHeadUnderWater) {
        buffers.sceneReflectiveRefractive.bind();

        // Make sure the water surface is rendered if the player is underwater.
        if (isHeadUnderWater) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public void postRenderCleanupSceneReflectiveRefractive(boolean isHeadUnderWater) {
        if (isHeadUnderWater) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        bindDisplay();
    }

    public void preRenderSetupSimpleBlendMaterials() {
        preRenderSetupSceneOpaque();
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
    }

    public void postRenderCleanupSimpleBlendMaterials() {
        GL11.glDisable(GL_BLEND);
        GL11.glDepthMask(true);
        postRenderCleanupSceneOpaque();
    }

    public void preRenderSetupChunk(Vector3f chunkPositionRelativeToCamera) {
        GL11.glPushMatrix();
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);
    }

    public void postRenderCleanupChunk() {
        GL11.glPopMatrix();
    }

    public void setRenderBufferMask(FBO fbo, boolean color, boolean normal, boolean lightBuffer) {
        if (fbo == null) {
            return;
        }

        int attachmentId = 0;

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);

        if (fbo.colorBufferTextureId != 0) {
            if (color) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId++);
            }
        }
        if (fbo.normalsBufferTextureId != 0) {
            if (normal) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId++);
            }
        }
        if (fbo.lightBufferTextureId != 0) {
            if (lightBuffer) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    public void setViewportToWholeDisplay() {
        glViewport(0, 0, fullScale.width(), fullScale.height());
    }

    public void setViewportToSizeOf(FBO fbo) {
        glViewport(0, 0, fbo.width(), fbo.height());
    }

    public void bindDisplay() {
        glBindFramebufferEXT(GL_FRAMEBUFFER_EXT, 0);
    }

    private class Buffers {
        public FBO sceneOpaque;
        public FBO sceneReflectiveRefractive;
        public FBO sceneReflected;
        public FBO sceneShadowMap;
    }
}
