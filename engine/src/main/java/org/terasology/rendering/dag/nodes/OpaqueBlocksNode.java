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
package org.terasology.rendering.dag.nodes;


import org.lwjgl.opengl.GL11;
import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * This node renders the opaque blocks in the world.
 *
 * In a typical world this is the majority of the world's landscape.
 */
public class OpaqueBlocksNode extends AbstractNode implements WireframeCapable {

    private static final ResourceUrn CHUNK_SHADER = new ResourceUrn("engine:prog.chunk");

    @In
    private WorldRenderer worldRenderer;

    @In
    private Config config;

    @In
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;
    private Material chunkShader;
    private SetWireframe wireframeStateChange;
    private RenderingDebugConfig renderingDebugConfig;

    /**
     * Initialises this node. -Must- be called once after instantiation.
     */
    @Override
    public void initialise() {
        playerCamera = worldRenderer.getActiveCamera();

        wireframeStateChange = new SetWireframe(true);
        renderingDebugConfig = config.getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
        addDesiredStateChange(new EnableMaterial(CHUNK_SHADER.toString()));
        chunkShader = getMaterial(CHUNK_SHADER);
    }

    public void enableWireframe() {
        if (!getDesiredStateChanges().contains(wireframeStateChange)) {
            addDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    public void disableWireframe() {
        if (getDesiredStateChanges().contains(wireframeStateChange)) {
            removeDesiredStateChange(wireframeStateChange);
            worldRenderer.requestTaskListRefresh();
        }
    }

    /**
     * Renders the world's opaque blocks, effectively, the world's landscape.
     * Does not render semi-transparent blocks, i.e. semi-transparent vegetation.
     *
     * If RenderingDebugConfig.isRenderChunkBoundingBoxes() returns true
     * this method also draws wireframe boxes around chunks, displaying
     * their boundaries.
     *
     * Finally, takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/opaqueChunks");

        final Vector3f cameraPosition = playerCamera.getPosition();

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        READ_ONLY_GBUFFER.bind(); // TODO: remove when we can bind this via a StateChange
        chunkShader.setFloat("clip", 0.0f, true);

        playerCamera.lookThrough(); // TODO: remove. Placed here to make the dependency explicit.

        while (renderQueues.chunksOpaque.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaque.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkShader, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(OPAQUE, chunkPosition, cameraPosition);

                if (renderingDebugConfig.isRenderChunkBoundingBoxes()) {
                    renderChunkBoundingBox(chunk, chunkPosition, cameraPosition);
                }

            } else {
                numberOfChunksThatAreNotReadyYet++;
            }
        }

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }

    private void renderChunkBoundingBox(RenderableChunk chunk, Vector3f chunkPosition, Vector3f cameraPosition) {
        GL11.glPushMatrix();

        // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
        final Vector3f chunkPositionRelativeToCamera =
                new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                        chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                        chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);
        GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

        new AABBRenderer(chunk.getAABB()).renderLocally(1f);

        GL11.glPopMatrix(); // Resets the matrix stack after the rendering of a chunk.
    }
}
