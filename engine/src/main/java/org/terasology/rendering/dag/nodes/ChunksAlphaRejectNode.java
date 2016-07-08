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
import org.terasology.config.RenderingDebugConfig;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.dag.Node;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.rendering.world.WorldRendererImpl;
import static org.terasology.rendering.opengl.OpenGLUtils.disableWireframeIf;
import static org.terasology.rendering.opengl.OpenGLUtils.enableWireframeIf;

/**
 * TODO: Diagram of this node
 * Alpha reject is used for semi-transparent billboards, which in turn are used for ground plants.
 */
public class ChunksAlphaRejectNode implements Node {

    @In
    private Config config;

    @In
    private ComponentSystemManager componentSystemManager;

    @In
    private WorldRenderer worldRenderer;

    @In
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;
    private RenderingDebugConfig renderingDebugConfig;

    @Override
    public void initialise() {
        renderingDebugConfig = config.getRendering().getDebug();
        playerCamera = worldRenderer.getActiveCamera();
    }

    @Override
    public void process() {
        PerformanceMonitor.startActivity("rendering/chunksAlphaReject");
        enableWireframeIf(renderingDebugConfig.isWireframe());

        worldRenderer.renderChunks(renderQueues.chunksAlphaReject,
                ChunkMesh.RenderPhase.ALPHA_REJECT, playerCamera,
                WorldRendererImpl.ChunkRenderMode.DEFAULT);

        disableWireframeIf(renderingDebugConfig.isWireframe());
        PerformanceMonitor.endActivity();
    }
}
