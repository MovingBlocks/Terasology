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
package org.terasology.rendering.dag;

import org.lwjgl.opengl.GL11;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRendererImpl;

import static org.terasology.rendering.opengl.OpenGLUtils.bindDisplay;

/**
 * TODO: Diagram of this node
 */
public class ChunksRefractiveReflectiveNode implements Node {

    @In
    private RenderQueuesHelper renderQueues;

    @In
    private WorldRenderer worldRenderer;

    @In
    private FrameBuffersManager frameBuffersManager;

    private Camera playerCamera;

    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("Render Chunks (Refractive/Reflective)");

        boolean isHeadUnderWater = worldRenderer.isHeadUnderWater();
        preRenderSetupSceneReflectiveRefractive(isHeadUnderWater);

        worldRenderer.renderChunks(renderQueues.chunksAlphaBlend, ChunkMesh.RenderPhase.REFRACTIVE, playerCamera, WorldRendererImpl.ChunkRenderMode.DEFAULT);

        postRenderCleanupSceneReflectiveRefractive(isHeadUnderWater);
        PerformanceMonitor.endActivity();
        PerformanceMonitor.endActivity(); // end "Render World" activity
    }

    /**
     * Sets the state for the rendering of the reflective/refractive features of the scene.
     * <p>
     * At this stage this is the surface of water bodies, reflecting the sky and (if enabled)
     * the surrounding landscape, and refracting the underwater scenery.
     * <p>
     * If the isHeadUnderWater argument is set to True, the state is further modified to
     * accommodate the rendering of the water surface from an underwater point of view.
     *
     * @param isHeadUnderWater Set to True if the point of view is underwater, to render the water surface correctly.
     */
    private void preRenderSetupSceneReflectiveRefractive(boolean isHeadUnderWater) {
        FBO sceneReflectiveRefractive = frameBuffersManager.getFBO("sceneReflectiveRefractive");
        sceneReflectiveRefractive.bind();

        // Make sure the water surface is rendered if the player is underwater.
        if (isHeadUnderWater) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
    }

    /**
     * Resets the state after the rendering of the reflective/refractive features of the scene.
     * <p>
     * See preRenderSetupSceneReflectiveRefractive() for additional information.
     *
     * @param isHeadUnderWater Set to True if the point of view is underwater, for some additional resetting.
     */
    private void postRenderCleanupSceneReflectiveRefractive(boolean isHeadUnderWater) {
        if (isHeadUnderWater) {
            GL11.glEnable(GL11.GL_CULL_FACE);
        }

        bindDisplay();
    }
}
