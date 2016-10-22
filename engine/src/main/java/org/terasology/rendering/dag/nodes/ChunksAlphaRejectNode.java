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
import org.terasology.engine.ComponentSystemManager;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.WireframeCapableNode;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.SetViewportToSizeOf;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;

/**
 * TODO: Diagram of this node
 * Alpha reject is used for semi-transparent billboards, which in turn are used for ground plants.
 */
public class ChunksAlphaRejectNode extends WireframeCapableNode {

    private static final ResourceUrn CHUNK_SHADER = new ResourceUrn("engine:prog.chunk");

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;
    private Material chunkShader;

    @Override
    public void initialise() {
        super.initialise();
        playerCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new SetViewportToSizeOf(READ_ONLY_GBUFFER));
        addDesiredStateChange(new EnableMaterial(CHUNK_SHADER.toString()));
        chunkShader = getMaterial(CHUNK_SHADER);
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/chunksAlphaReject");

        final Vector3f cameraPosition = playerCamera.getPosition();

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        READ_ONLY_GBUFFER.bind();
        chunkShader.setFloat("clip", 0.0f, true);
        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);

        while (renderQueues.chunksAlphaReject.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksAlphaReject.poll();

            if (chunk.hasMesh()) {
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();
                final Vector3f chunkPositionRelativeToCamera =
                        new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                                chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                                chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);

                chunkShader.setFloat3("chunkPositionWorld",
                        chunkPosition.x * ChunkConstants.SIZE_X,
                        chunkPosition.y * ChunkConstants.SIZE_Y,
                        chunkPosition.z * ChunkConstants.SIZE_Z,
                        true);
                chunkShader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f, true);

                // Effectively this just positions the chunk appropriately, relative to the camera.
                // chunkPositionRelativeToCamera = chunkCoordinates * chunkDimensions - cameraCoordinate
                GL11.glPushMatrix();
                GL11.glTranslatef(chunkPositionRelativeToCamera.x, chunkPositionRelativeToCamera.y, chunkPositionRelativeToCamera.z);

                chunk.getMesh().render(ChunkMesh.RenderPhase.ALPHA_REJECT);
                numberOfRenderedTriangles += chunk.getMesh().triangleCount();

                GL11.glPopMatrix(); // Resets the matrix stack after the rendering of a chunk.

            } else {
                numberOfChunksThatAreNotReadyYet++; // TODO: verify - should we count them only in ChunksOpaqueNode?
            }
        }

        chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);

        worldRenderer.increaseTrianglesCount(numberOfRenderedTriangles);
        worldRenderer.increaseNotReadyChunkCount(numberOfChunksThatAreNotReadyYet);

        PerformanceMonitor.endActivity();
    }
}
