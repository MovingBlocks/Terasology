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

import org.terasology.config.Config;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.states.StateTypeImpl;
import org.terasology.rendering.dag.states.StateValue;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRendererImpl;

/**
 * TODO: Diagram of this node
 */
public class ChunksOpaqueNode extends AbstractNode {

    @In
    private Config config;

    @In
    private WorldRenderer worldRenderer;

    @In
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;

    public ChunksOpaqueNode(String id) {
        super(id);
    }

    @Override
    public void initialise() {
        addDesiredState(StateTypeImpl.WIREFRAME, StateValue.ENABLED);
        playerCamera = worldRenderer.getActiveCamera();
    }

    @Override
    public void process() {
        worldRenderer.renderChunks(renderQueues.chunksOpaque,
                ChunkMesh.RenderPhase.OPAQUE,
                playerCamera,
                WorldRendererImpl.ChunkRenderMode.DEFAULT);
    }
}
