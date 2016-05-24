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
package org.terasology.rendering.world.dag;


import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.math.geom.Vector3f;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.ShaderManager;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.opengl.FrameBuffersManager;
import org.terasology.rendering.opengl.GraphicState;
import org.terasology.rendering.opengl.PostProcessor;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.RenderableWorld;
import org.terasology.rendering.world.WorldRendererImpl;
import org.terasology.utilities.Assets;
import org.terasology.utilities.collection.DirectedAcyclicClassGraph;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;


public class DAGRendererSystem implements ComponentSystem {
    public final RenderingConfig config;
    public final Camera playerCamera;
    public final BackdropProvider backdropProvider;
    public RenderQueuesHelper queues;
    public ComponentSystemManager systemManager;
    public DirectedAcyclicClassGraph<RenderableNode> nodes = new DirectedAcyclicClassGraph<>();
    // TODO: must be private
    public FrameBuffersManager buffersManager;
    public GraphicState graphicState;
    public PostProcessor postProcessor;
    public Material chunkShader;
    
    protected final RenderingDebugConfig debugConfig;
    private final RenderableWorld renderableWorld;
    private final WorldProvider worldProvider;
    private final Context context;

    private int statChunkMeshEmpty;
    private int statChunkNotReady;
    private int statRenderedTriangles;

    public DAGRendererSystem(Context context,
                             GLBufferPool bufferPool,
                             Camera playerCamera,
                             BackdropProvider backdropProvider,
                             RenderableWorld renderableWorld,
                             RenderingConfig renderingConfig,
                             RenderingDebugConfig debugConfig,
                             WorldProvider worldProvider,
                             ComponentSystemManager systemManager) {


        this.renderableWorld = renderableWorld;
        this.systemManager = systemManager;
        this.config = renderingConfig;
        this.debugConfig = debugConfig;
        this.worldProvider = worldProvider;
        this.queues = this.renderableWorld.getRenderQueues();
        this.context = context;
        this.playerCamera = playerCamera;
        this.backdropProvider = backdropProvider;


    }


    public void renderChunk(RenderableChunk chunk, ChunkMesh.RenderPhase phase, Camera camera, WorldRendererImpl.ChunkRenderMode mode) {
        if (chunk.hasMesh()) {
            final Vector3f cameraPosition = camera.getPosition();
            final Vector3f chunkPosition = chunk.getPosition().toVector3f();
            final Vector3f chunkPositionRelativeToCamera =
                    new Vector3f(chunkPosition.x * ChunkConstants.SIZE_X - cameraPosition.x,
                            chunkPosition.y * ChunkConstants.SIZE_Y - cameraPosition.y,
                            chunkPosition.z * ChunkConstants.SIZE_Z - cameraPosition.z);

            if (mode == WorldRendererImpl.ChunkRenderMode.DEFAULT || mode == WorldRendererImpl.ChunkRenderMode.REFLECTION) {
                if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                    chunkShader.activateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                    chunkShader.activateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                }

                chunkShader.setFloat3("chunkPositionWorld", chunkPosition.x * ChunkConstants.SIZE_X,
                        chunkPosition.y * ChunkConstants.SIZE_Y,
                        chunkPosition.z * ChunkConstants.SIZE_Z);
                chunkShader.setFloat("animated", chunk.isAnimated() ? 1.0f : 0.0f);

                if (mode == WorldRendererImpl.ChunkRenderMode.REFLECTION) {
                    chunkShader.setFloat("clip", camera.getClipHeight());
                } else {
                    chunkShader.setFloat("clip", 0.0f);
                }

                chunkShader.enable();

            } else if (mode == WorldRendererImpl.ChunkRenderMode.SHADOW_MAP) {

                nodes.get(ShadowMapNode.class).shader.enable();

            } else if (mode == WorldRendererImpl.ChunkRenderMode.Z_PRE_PASS) {
                context.get(ShaderManager.class).disableShader();
            }

            graphicState.preRenderSetupChunk(chunkPositionRelativeToCamera);

            if (chunk.hasMesh()) {
                if (this.debugConfig.isRenderChunkBoundingBoxes()) {
                    AABBRenderer aabbRenderer = new AABBRenderer(chunk.getAABB());
                    aabbRenderer.renderLocally(1f);
                    statRenderedTriangles += 12;
                }

                chunk.getMesh().render(phase);
                statRenderedTriangles += chunk.getMesh().triangleCount();
            }

            graphicState.postRenderCleanupChunk();

            // TODO: review - moving the deactivateFeature commands to the analog codeblock above doesn't work. Why?
            if (mode == WorldRendererImpl.ChunkRenderMode.DEFAULT || mode == WorldRendererImpl.ChunkRenderMode.REFLECTION) {
                if (phase == ChunkMesh.RenderPhase.REFRACTIVE) {
                    chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_REFRACTIVE_PASS);
                } else if (phase == ChunkMesh.RenderPhase.ALPHA_REJECT) {
                    chunkShader.deactivateFeature(ShaderProgramFeature.FEATURE_ALPHA_REJECT);
                }
            }
        } else {
            statChunkNotReady++;
        }
    }

    public Material getMaterial(String assetId) {
        return Assets.getMaterial(assetId).orElseThrow(() ->
                new RuntimeException("Failed to resolve required asset: '" + assetId + "'"));
    }

    public void resetStats() {
        statChunkMeshEmpty = 0;
        statChunkNotReady = 0;
        statRenderedTriangles = 0;
    }


    public String getMetrics() {
        StringBuilder builder = new StringBuilder();
        builder.append(renderableWorld.getMetrics());
        builder.append("Empty Mesh Chunks: ");
        builder.append(statChunkMeshEmpty);
        builder.append("\n");
        builder.append("Unready Chunks: ");
        builder.append(statChunkNotReady);
        builder.append("\n");
        builder.append("Rendered Triangles: ");
        builder.append(statRenderedTriangles);
        builder.append("\n");
        return builder.toString();
    }


    public void initRenderingSupport() {
        buffersManager = new FrameBuffersManager();
        context.put(FrameBuffersManager.class, buffersManager);

        graphicState = new GraphicState(buffersManager);
        postProcessor = new PostProcessor(buffersManager, graphicState);
        context.put(PostProcessor.class, postProcessor);

        buffersManager.setGraphicState(graphicState);
        buffersManager.setPostProcessor(postProcessor);
        buffersManager.initialize();

        context.get(ShaderManager.class).initShaders();
        postProcessor.initializeMaterials();

        nodes.get(ShadowMapNode.class).shader = getMaterial("engine:prog.shadowMap");

    }

    @Override
    public void initialise() {

    }

    @Override
    public void preBegin() {

    }

    @Override
    public void postBegin() {

    }

    @Override
    public void preSave() {

    }

    @Override
    public void postSave() {

    }

    @Override
    public void shutdown() {

    }

    public void addNode(RenderableNode node) {
        node.renderer = this;
        nodes.addNode(node);
    }
}
