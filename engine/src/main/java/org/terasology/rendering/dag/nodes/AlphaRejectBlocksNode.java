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

import org.terasology.assets.ResourceUrn;
import org.terasology.config.Config;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
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
import org.terasology.world.chunks.RenderableChunk;

import static org.terasology.rendering.opengl.DefaultDynamicFBOs.READ_ONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.ALPHA_REJECT;

/**
 * This node uses alpha-rejection to render semi-transparent blocks (i.e. tree foliage) and
 * semi-transparent billboards (i.e. plants on the ground).
 *
 * Alpha-rejection is the idea that if a fragment has an alpha value lower than some threshold
 * it gets discarded, leaving the color already stored in the frame buffer untouched.
 *
 * This is a less expensive way to render semi-transparent objects compared to alpha-blending.
 * In alpha-blending the color of a semi-transparent fragment is combined with
 * the color stored in the frame buffer and the resulting color overwrites the previously stored one.
 */
public class AlphaRejectBlocksNode extends AbstractNode implements WireframeCapable {

    private static final ResourceUrn CHUNK_SHADER = new ResourceUrn("engine:prog.chunk");

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;
    private Material chunkShader;
    private SetWireframe wireframeStateChange;
    private RenderingDebugConfig renderingDebugConfig;

    /**
     * Initialises the node. -Must- be called once after instantiation.
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
     * Renders the world's semi-transparent blocks, i.e. tree foliage and terrain plants.
     * Does not render fully opaque blocks, i.e. the typical landscape blocks.
     *
     * Takes advantage of the two methods
     *
     * - WorldRenderer.increaseTrianglesCount(int)
     * - WorldRenderer.increaseNotReadyChunkCount(int)
     *
     * to publish some statistics over its own activity.
     */
    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/chunksAlphaReject");

        final Vector3f cameraPosition = playerCamera.getPosition();

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        READ_ONLY_GBUFFER.bind();   // TODO: remove when we can bind this via a StateChange
        chunkShader.setFloat("clip", 0.0f, true);
        chunkShader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);

        playerCamera.lookThrough(); // TODO: remove. Placed here to make the dependency explicit.

        while (renderQueues.chunksAlphaReject.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksAlphaReject.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkShader, chunkPosition, chunk.isAnimated());
                numberOfRenderedTriangles += chunkMesh.render(ALPHA_REJECT, chunkPosition, cameraPosition);

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
