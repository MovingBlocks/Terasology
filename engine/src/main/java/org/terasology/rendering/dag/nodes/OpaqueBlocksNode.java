/*
 * Copyright 2017 MovingBlocks
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
import org.terasology.config.RenderingConfig;
import org.terasology.config.RenderingDebugConfig;
import org.terasology.context.Context;
import org.terasology.math.geom.Vector3f;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.rendering.AABBRenderer;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.cameras.SubmersibleCamera;
import org.terasology.rendering.dag.AbstractNode;
import org.terasology.rendering.dag.StateChange;
import org.terasology.rendering.dag.WireframeCapable;
import org.terasology.rendering.dag.WireframeTrigger;
import org.terasology.rendering.dag.stateChanges.BindFbo;
import org.terasology.rendering.dag.stateChanges.EnableMaterial;
import org.terasology.rendering.dag.stateChanges.LookThrough;
import org.terasology.rendering.dag.stateChanges.SetInputTexture;
import org.terasology.rendering.dag.stateChanges.SetWireframe;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.world.RenderQueuesHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.terasology.rendering.opengl.fbms.DisplayResolutionDependentFBOs.READONLY_GBUFFER;
import static org.terasology.rendering.primitives.ChunkMesh.RenderPhase.OPAQUE;

/**
 * This node renders the opaque blocks in the world.
 *
 * In a typical world this is the majority of the world's landscape.
 */
public class OpaqueBlocksNode extends AbstractNode implements WireframeCapable, PropertyChangeListener {
    private static final ResourceUrn CHUNK_MATERIAL = new ResourceUrn("engine:prog.chunk");

    private WorldRenderer worldRenderer;
    private RenderQueuesHelper renderQueues;
    private RenderingConfig renderingConfig;
    private WorldProvider worldProvider;

    private Material chunkMaterial;
    private SetWireframe wireframeStateChange;
    private RenderingDebugConfig renderingDebugConfig;

    private SubmersibleCamera activeCamera;

    private boolean isNormalMapping;
    private boolean isParallaxMapping;

    private StateChange setNormalTerrain;
    private StateChange setHeightTerrain;

    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.5f)
    private float parallaxBias = 0.05f;
    @SuppressWarnings("FieldCanBeLocal")
    @Range(min = 0.0f, max = 0.50f)
    private float parallaxScale = 0.05f;

    public OpaqueBlocksNode(Context context) {
        renderQueues = context.get(RenderQueuesHelper.class);
        worldProvider = context.get(WorldProvider.class);

        wireframeStateChange = new SetWireframe(true);
        renderingDebugConfig = context.get(Config.class).getRendering().getDebug();
        new WireframeTrigger(renderingDebugConfig, this);

        worldRenderer = context.get(WorldRenderer.class);
        activeCamera = worldRenderer.getActiveCamera();
        addDesiredStateChange(new LookThrough(activeCamera));

        addDesiredStateChange(new BindFbo(READONLY_GBUFFER, context.get(DisplayResolutionDependentFBOs.class)));

        addDesiredStateChange(new EnableMaterial(CHUNK_MATERIAL));

        chunkMaterial = getMaterial(CHUNK_MATERIAL);

        renderingConfig = context.get(Config.class).getRendering();
        isNormalMapping = renderingConfig.isNormalMapping();
        renderingConfig.subscribe(RenderingConfig.NORMAL_MAPPING, this);
        isParallaxMapping = renderingConfig.isParallaxMapping();
        renderingConfig.subscribe(RenderingConfig.PARALLAX_MAPPING, this);

        int textureSlot = 0;
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:terrain", CHUNK_MATERIAL, "textureAtlas"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:effects", CHUNK_MATERIAL, "textureEffects"));
        addDesiredStateChange(new SetInputTexture(textureSlot++, "engine:lavaStill", CHUNK_MATERIAL, "textureLava"));
        setNormalTerrain = new SetInputTexture(textureSlot++, "engine:terrainNormal", CHUNK_MATERIAL, "textureAtlasNormal");
        setHeightTerrain = new SetInputTexture(textureSlot, "engine:terrainHeight", CHUNK_MATERIAL, "textureAtlasHeight");

        if (isNormalMapping) {
            addDesiredStateChange(setNormalTerrain);

            if (isParallaxMapping) {
                addDesiredStateChange(setHeightTerrain);
            }
        }
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

        // Common Shader Parameters

        chunkMaterial.setFloat("time", worldProvider.getTime().getDays(), true);

        // Specific Shader Parameters

        chunkMaterial.setFloat("clip", 0.0f, true);

        if (isNormalMapping) {
            if (isParallaxMapping) {
                chunkMaterial.setFloat4("parallaxProperties", parallaxBias, parallaxScale, 0.0f, 0.0f, true);
            }
        }

        // Actual Node Processing

        final Vector3f cameraPosition = activeCamera.getPosition();

        int numberOfRenderedTriangles = 0;
        int numberOfChunksThatAreNotReadyYet = 0;

        while (renderQueues.chunksOpaque.size() > 0) {
            RenderableChunk chunk = renderQueues.chunksOpaque.poll();

            if (chunk.hasMesh()) {
                final ChunkMesh chunkMesh = chunk.getMesh();
                final Vector3f chunkPosition = chunk.getPosition().toVector3f();

                chunkMesh.updateMaterial(chunkMaterial, chunkPosition, chunk.isAnimated());
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

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getOldValue() != event.getNewValue()) {
            if (event.getPropertyName().equals(RenderingConfig.NORMAL_MAPPING)) {
                isNormalMapping = renderingConfig.isNormalMapping();
                if (isNormalMapping) {
                    addDesiredStateChange(setNormalTerrain);
                    if (isParallaxMapping) {
                        addDesiredStateChange(setHeightTerrain);
                    }
                } else {
                    removeDesiredStateChange(setNormalTerrain);
                    if (isParallaxMapping) {
                        removeDesiredStateChange(setHeightTerrain);
                    }
                }
            } else {
                isParallaxMapping = renderingConfig.isParallaxMapping();
                if (isNormalMapping) {
                    if (isParallaxMapping) {
                        addDesiredStateChange(setHeightTerrain);
                    } else {
                        removeDesiredStateChange(setHeightTerrain);
                    }
                }
            }

            worldRenderer.requestTaskListRefresh();
        }
    }
}
