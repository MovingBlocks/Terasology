/*
 * Copyright 2016 MovingBlocks
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

import org.lwjgl.opengl.GL11;
import org.terasology.math.geom.Vector3f;

/**
 * The GraphicState class aggregates a number of methods setting the OpenGL state
 * before and after rendering passes.
 *
 * In many circumstances these methods do little more than binding the appropriate
 * Frame Buffer Object (FBO) and texture buffers so the OpenGL implementation and
 * the shaders know where to read from and write to. In some cases more involved
 * OpenGL state changes occur, i.e. in the lighting-related methods.
 *
 * Also, most methods come in pairs named preRenderSetup*() and postRenderCleanup*(),
 * reflecting their use before or after an actual render takes place. Usually the first
 * method changes an unspecified default state while the second method reinstates it.
 *
 * A number of FBO references are kept up to date through the refreshDynamicFBOs() and
 * setSceneShadowMap() methods. At this stage the FrameBuffersManager class is tasked
 * with running these methods whenever its FBOs change.
 */
public class GraphicState {
    // As this class pretty much always deals with OpenGL states, it did occur to me
    // that it might be better called OpenGLState or something along that line. I
    // eventually decided for GraphicState as it resides in the rendering.opengl
    // package anyway and rendering.opengl.OpenGLState felt cumbersome. --emanuele3d
    private FrameBuffersManager buffersManager;
    private Buffers buffers = new Buffers();

    /**
     * Graphic State constructor.
     *
     * This constructor only sets the internal reference to the rendering process. It does not obtain
     * all the references to FBOs nor it initializes the other instance it requires to operate correctly.
     * As such, it relies on the caller to make sure that at the appropriate time (when the buffers are
     * available) refreshDynamicFBOs() and setSceneShadowMap() are called and the associated internal
     * FBO references are initialized.
     *
     * @param buffersManager An instance of the FrameBuffersManager class, used to obtain references to its FBOs.
     */
    public GraphicState(FrameBuffersManager buffersManager) {
        // a reference to the frameBuffersManager is not strictly necessary, as it is used only
        // in refreshDynamicFBOs() and it could be passed as argument to it. We do it this
        // way however to maintain similarity with the way the PostProcessor works.
        this.buffersManager = buffersManager;
    }

    /**
     * This method disposes of a GraphicState instance by simply nulling a number of internal references.
     *
     * It is probably not strictly necessary as the Garbage Collection mechanism should be able to dispose
     * instances of this class without much trouble once they are out of scope. But it is probably good
     * form to include and use a dispose() method, to make it explicit when an instance will no longer be
     * useful.
     */
    public void dispose() {
        buffersManager = null;
        buffers = null;
    }

    /**
     * Used to initialize and eventually refresh the internal references to FBOs primarily
     * held by the FrameBuffersManager instance.
     *
     * Instances of the GraphicState class cannot operate unless this method has been called
     * at least once, the FBOs retrieved through it not null. It then needs to be called again
     * every time the FrameBuffersManager instance changes its FBOs. This occurs whenever
     * the display resolution changes or when a screenshot is taken with a resolution that
     * is different from that of the display.
     */
    public void refreshDynamicFBOs() {
        buffers.sceneOpaque               = buffersManager.getFBO("sceneOpaque");
        buffers.sceneReflectiveRefractive = buffersManager.getFBO("sceneReflectiveRefractive");
        buffers.sceneReflected            = buffersManager.getFBO("sceneReflected");
    }

    public void setSceneOpaqueFBO(FBO newSceneOpaque) {
        buffers.sceneOpaque = newSceneOpaque;
    }

    /**
     * Used to initialize and update the internal reference to the Shadow Map FBO.
     *
     * Gets called every time the FrameBuffersManager instance changes the ShadowMap FBO.
     * This will occur whenever the ShadowMap resolution is changed, i.e. via the rendering
     * settings.
     *
     * @param newShadowMap the FBO containing the new shadow map buffer
     */
    public void setSceneShadowMap(FBO newShadowMap) {
        buffers.sceneShadowMap = newShadowMap;
    }

    /**
     * Sets the state prior to the rendering of a chunk.
     *
     * In practice this just positions the chunk appropriately, relative to the camera.
     *
     * @param chunkPositionRelativeToCamera Effectively: chunkCoordinates * chunkDimensions - cameraCoordinate
     */
    public void preRenderSetupChunk(Vector3f chunkPositionRelativeToCamera) {
        GL11.glPushMatrix();
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);
    }

    /**
     * Resets the state after the rendering of a chunk.
     *
     * See preRenderSetupChunk() for additional information.
     */
    public void postRenderCleanupChunk() {
        GL11.glPopMatrix();
    }

    private class Buffers {
        public FBO sceneOpaque;
        public FBO sceneReflectiveRefractive;
        public FBO sceneReflected;
        public FBO sceneShadowMap;
    }
}
