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

    public GraphicState(LwjglRenderingProcess renderingProcess) {
        this.renderingProcess = renderingProcess;
    }

    public void dispose() {
        renderingProcess = null;
        fullScale = null;
    }

    public void clear() {
        renderingProcess.bindFbo("sceneOpaque");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
        renderingProcess.unbindFbo("sceneOpaque");
        renderingProcess.bindFbo("sceneReflectiveRefractive");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderingProcess.unbindFbo("sceneReflectiveRefractive");
    }

    public void beginRenderSceneOpaque() {
        renderingProcess.bindFbo("sceneOpaque");
        setRenderBufferMask("sceneOpaque", true, true, true);
    }

    public void endRenderSceneOpaque() {
        setRenderBufferMask("sceneOpaque", true, true, true);
        renderingProcess.unbindFbo("sceneOpaque");
    }

    public void enableWireframeIf(boolean isWireframeEnabledInRenderingDebugConfig) {
        if (isWireframeEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        }
    }

    public void disableWireframeIf(boolean isWireframeEnabledInRenderingDebugConfig) {
        if (isWireframeEnabledInRenderingDebugConfig) {
            GL11.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }
    }

    public void beginRenderSceneShadowMap() {
        FBO shadowMap = renderingProcess.getFBO("sceneShadowMap");

        if (shadowMap == null) {
            return;
        }

        shadowMap.bind();

        setViewportTo(shadowMap.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL_CULL_FACE);
    }

    public void endRenderSceneShadowMap() {
        GL11.glEnable(GL_CULL_FACE);
        renderingProcess.unbindFbo("sceneShadowMap");
        setViewportToFullSize();
    }

    public void beginRenderReflectedScene() {
        FBO reflected = renderingProcess.getFBO("sceneReflected");

        if (reflected == null) {
            return;
        }

        reflected.bind();

        setViewportTo(reflected.dimensions());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        GL11.glCullFace(GL11.GL_FRONT);
    }

    public void endRenderReflectedScene() {
        GL11.glCullFace(GL11.GL_BACK);
        renderingProcess.unbindFbo("sceneReflected");
        setViewportToFullSize();
    }

    public void beginRenderSceneSky() {
        setRenderBufferMask("sceneOpaque", true, false, false);
    }

    public void endRenderSceneSky() {
        setRenderBufferMask("sceneOpaque", true, true, true);
    }

    public void endRenderSkyBands() {
        renderingProcess.bindFbo("sceneOpaque");
    }

    public void beginRenderFirstPerson() {
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDepthFunc(GL11.GL_ALWAYS);
    }

    public void endRenderFirstPerson() {
        GL11.glDepthFunc(GL_LEQUAL);
        GL11.glPopMatrix();
    }

    // TODO: figure how lighting works and what this does
    public void beginRenderLightGeometryStencilPass() {
        renderingProcess.bindFbo("sceneOpaque");
        setRenderBufferMask("sceneOpaque", false, false, false);
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
    public void endRenderLightGeometryStencilPass() {
        setRenderBufferMask("sceneOpaque", true, true, true);
        renderingProcess.unbindFbo("sceneOpaque");
    }

    public void beginRenderLightGeometry() {
        renderingProcess.bindFbo("sceneOpaque");

        // Only write to the light buffer
        setRenderBufferMask("sceneOpaque", false, false, true);

        glStencilFunc(GL_NOTEQUAL, 0, 0xFF);

        glDepthMask(true);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);

        glEnable(GL_CULL_FACE);
        glCullFace(GL_FRONT);
    }

    public void endRenderLightGeometry() {
        glDisable(GL_STENCIL_TEST);
        glCullFace(GL_BACK);

        renderingProcess.unbindFbo("sceneOpaque");
    }

    public void beginRenderDirectionalLights() {
        renderingProcess.bindFbo("sceneOpaque");
    }

    public void endRenderDirectionalLights() {
        glDisable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glEnable(GL_DEPTH_TEST);

        setRenderBufferMask("sceneOpaque", true, true, true);
        renderingProcess.unbindFbo("sceneOpaque");

        renderingProcess.applyLightBufferPass("sceneOpaque");
    }

    public void beginRenderSimpleBlendMaterialsIntoCombinedPass() {
        beginRenderSceneOpaque();
        GL11.glEnable(GL_BLEND);
        GL11.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
    }

    public void beginRenderSceneReflectiveRefractive(boolean isHeadUnderWater) {
        renderingProcess.bindFbo("sceneReflectiveRefractive");

        // Make sure the water surface is rendered if the player is underwater.
        if (isHeadUnderWater) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    public void endRenderSceneReflectiveRefractive(boolean isHeadUnderWater) {
        if (isHeadUnderWater) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        renderingProcess.unbindFbo("sceneReflectiveRefractive");
    }

    public void endRenderSimpleBlendMaterialsIntoCombinedPass() {
        GL11.glDisable(GL_BLEND);
        GL11.glDepthMask(true);
        endRenderSceneOpaque();
    }

    public void preChunkRenderSetup(Vector3f chunkPositionRelativeToCamera) {
        GL11.glPushMatrix();
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);
    }

    public void postChunkRenderCleanup() {
        GL11.glPopMatrix();
    }

    public void setRenderBufferMask(String fboName, boolean color, boolean normal, boolean lightBuffer) {
        FBO fbo = renderingProcess.getFBO(fboName);
        setRenderBufferMask(fbo, color, normal, lightBuffer);
    }

    public void setRenderBufferMask(FBO fbo, boolean color, boolean normal, boolean lightBuffer) {
        if (fbo == null) {
            return;
        }

        int attachmentId = 0;

        IntBuffer bufferIds = BufferUtils.createIntBuffer(3);

        if (fbo.colorBufferTextureId != 0) {
            if (color) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.normalsBufferTextureId != 0) {
            if (normal) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }
        if (fbo.lightBufferTextureId != 0) {
            if (lightBuffer) {
                bufferIds.put(GL_COLOR_ATTACHMENT0_EXT + attachmentId);
            }

            attachmentId++;
        }

        bufferIds.flip();

        GL20.glDrawBuffers(bufferIds);
    }

    public void setViewportToFullSize() {
        glViewport(0, 0, fullScale.width(), fullScale.height());
    }

    public void setViewportTo(Dimensions dimensions) {
        glViewport(0, 0, dimensions.width(), dimensions.height());
    }

    public void setFullScale(Dimensions newDimensions) {
        fullScale = newDimensions;
    }
}
