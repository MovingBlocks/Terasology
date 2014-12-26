/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.world;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.RenderableChunk;

import javax.vecmath.Vector3f;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Created by manu on 24.12.2014.
 */
public class RenderableWorldImpl implements RenderableWorld {

    public static final int MAX_ANIMATED_CHUNKS = 64;
    public static final int MAX_BILLBOARD_CHUNKS = 64;
    public static final int VERTICAL_SEGMENTS = CoreRegistry.get(Config.class).getSystem().getVerticalChunkMeshSegments();

    private static final int MAX_LOADABLE_CHUNKS = ViewDistance.MEGA.getChunkDistance().x * ViewDistance.MEGA.getChunkDistance().y * ViewDistance.MEGA.getChunkDistance().z;
    private static final int CHUNK_YSIZE_TO_SEGMENTS_RATIO = ChunkConstants.SIZE_Y / VERTICAL_SEGMENTS;

    private static final Logger logger = LoggerFactory.getLogger(RenderableWorldImpl.class);

    private final WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private Camera activeCamera;
    private Camera shadowMapCamera;

    private ChunkTessellator chunkTessellator;
    private final ChunkMeshUpdateManager chunkMeshUpdateManager;
    // TODO: Review usage of ChunkImpl throughout WorldRenderer
    private final List<RenderableChunk> chunksInProximityOfCamera = Lists.newArrayListWithCapacity(MAX_LOADABLE_CHUNKS);
    private Region3i renderableRegion = Region3i.EMPTY;
    private RenderQueuesHelper renderQueues;

    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();

    private int statDirtyChunks;
    private int statVisibleChunks;
    private int statIgnoredPhases;

    public RenderableWorldImpl(WorldProvider worldProvider, ChunkProvider chunkProvider, GLBufferPool bufferPool, Camera activeCamera, Camera shadowMapCamera) {
        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;
        chunkTessellator = new ChunkTessellator(bufferPool);
        chunkMeshUpdateManager = new ChunkMeshUpdateManager(chunkTessellator, worldProvider);

        this.activeCamera = activeCamera;
        this.shadowMapCamera = shadowMapCamera;

        renderQueues = new RenderQueuesHelper(new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkBackToFrontComparator()));
    }

    @Override
    public void onChunkLoaded(Vector3i chunkCoordinates) {
        if (renderableRegion.encompasses(chunkCoordinates)) {
            chunksInProximityOfCamera.add(chunkProvider.getChunk(chunkCoordinates));
            Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
        }
    }

    @Override
    public void onChunkUnloaded(Vector3i chunkCoordinates) {
        if (renderableRegion.encompasses(chunkCoordinates)) {
            Iterator<RenderableChunk> iterator = chunksInProximityOfCamera.iterator();
            while (iterator.hasNext()) {
                RenderableChunk chunk = iterator.next();
                if (chunk.getPosition().equals(chunkCoordinates)) {
                    chunk.disposeMesh();
                    iterator.remove();
                    Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
                    break;
                }
            }
        }
    }

    /**
     * @return true if pregeneration is complete
     */
    @Override
    public boolean pregenerateChunks() {
        boolean pregenerationIsComplete = true;
        Vector3i cameraCoordinates = calcCameraCoordinatesInChunkUnits();
        Vector3i renderableRegionSize = renderingConfig.getViewDistance().getChunkDistance();
        Vector3i renderableRegionExtents = new Vector3i(renderableRegionSize.x / 2, renderableRegionSize.y / 2, renderableRegionSize.z / 2);

        chunkProvider.completeUpdate();
        chunkProvider.beginUpdate();

        for (Vector3i chunkCoordinates : Region3i.createFromCenterExtents(cameraCoordinates, renderableRegionExtents)) {
            RenderableChunk chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk == null) {
                pregenerationIsComplete = false;
            } else if (chunk.isDirty()) {
                ChunkView localView = worldProvider.getLocalView(chunk.getPosition());
                if (localView == null) {
                    continue;
                }
                chunk.setDirty(false);

                ChunkMesh[] newMeshes = new ChunkMesh[VERTICAL_SEGMENTS];
                for (int segment = 0; segment < VERTICAL_SEGMENTS; segment++) {
                    newMeshes[segment] = chunkTessellator.generateMesh(localView, CHUNK_YSIZE_TO_SEGMENTS_RATIO, segment * CHUNK_YSIZE_TO_SEGMENTS_RATIO);
                }

                chunk.setPendingMesh(newMeshes);

                if (chunk.getPendingMesh() != null) {

                    for (int j = 0; j < chunk.getPendingMesh().length; j++) {
                        chunk.getPendingMesh()[j].generateVBOs();
                    }
                    if (chunk.getMesh() != null) {
                        for (int j = 0; j < chunk.getMesh().length; j++) {
                            chunk.getMesh()[j].dispose();
                        }
                    }
                    chunk.setMesh(chunk.getPendingMesh());
                    chunk.setPendingMesh(null);
                }
                pregenerationIsComplete = false;
                return pregenerationIsComplete;
            }
        }
        return pregenerationIsComplete;
    }

    @Override
    public void update() {

        PerformanceMonitor.startActivity("Complete chunk update");
        chunkProvider.completeUpdate();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Update Lighting");
        worldProvider.processPropagation();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Begin chunk update");
        chunkProvider.beginUpdate();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Update Close Chunks");
        updateChunksInProximity(calculateRenderableRegion(renderingConfig.getViewDistance()));
        PerformanceMonitor.endActivity();

    }

    /**
     * Updates the list of chunks around the player.
     *
     * @return True if the list was changed
     */
    public boolean updateChunksInProximity(Region3i newRenderableRegion) {
        if (!newRenderableRegion.equals(renderableRegion)) {

            Iterator<Vector3i> chunksToRemove = renderableRegion.subtract(newRenderableRegion);
            while (chunksToRemove.hasNext()) {
                Vector3i chunkPosition = chunksToRemove.next();
                Iterator<RenderableChunk> nearbyChunks = chunksInProximityOfCamera.iterator();
                while (nearbyChunks.hasNext()) {
                    RenderableChunk chunk = nearbyChunks.next();
                    if (chunk.getPosition().equals(chunkPosition)) {
                        chunk.disposeMesh();
                        nearbyChunks.remove();
                        break;
                    }
                }
            }

            Iterator<Vector3i> chunksToAdd = newRenderableRegion.subtract(renderableRegion);
            while (chunksToAdd.hasNext()) {
                Vector3i chunkCoordinates = chunksToAdd.next();
                RenderableChunk chunk = chunkProvider.getChunk(chunkCoordinates);
                if (chunk != null) {
                    chunksInProximityOfCamera.add(chunk);
                }
            }

            renderableRegion = newRenderableRegion;
            Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
            return true;
        }
        return false;
    }

    public boolean updateChunksInProximity(ViewDistance viewDistance) {
        logger.info("New Viewing Distance: {}", viewDistance);
        return updateChunksInProximity(calculateRenderableRegion(viewDistance));
    }

    private Region3i calculateRenderableRegion(ViewDistance newViewDistance) {
        Vector3i cameraCoordinates = calcCameraCoordinatesInChunkUnits();
        Vector3i renderableRegionSize = newViewDistance.getChunkDistance();
        Vector3i renderableRegionExtents = new Vector3i(renderableRegionSize.x / 2, renderableRegionSize.y / 2, renderableRegionSize.z / 2);
        return Region3i.createFromCenterExtents(cameraCoordinates, renderableRegionExtents);
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset chunk
     */
    private Vector3i calcCameraCoordinatesInChunkUnits() {
        return new Vector3i((int) (activeCamera.getPosition().x / ChunkConstants.SIZE_X),
                (int) (activeCamera.getPosition().y / ChunkConstants.SIZE_Y),
                (int) (activeCamera.getPosition().z / ChunkConstants.SIZE_Z));
    }

    /**
     * Updates the currently visible chunks (in sight of the player).
     */
    @Override
    public void updateAndQueueVisibleChunks() {
        updateAndQueueVisibleChunks(true, true);
    }

    @Override
    public int updateAndQueueVisibleChunks(boolean fillShadowRenderQueue, boolean processChunkUpdates) {
        statDirtyChunks = 0;
        statVisibleChunks = 0;
        statIgnoredPhases = 0;

        if (processChunkUpdates) {
            PerformanceMonitor.startActivity("Building Mesh VBOs");
            chunkMeshUpdateManager.setCameraPosition(activeCamera.getPosition());
            for (RenderableChunk chunk : chunkMeshUpdateManager.availableChunksForUpdate()) {
                if (chunksInProximityOfCamera.contains(chunk) && chunk.getPendingMesh() != null) {
                    for (int i = 0; i < chunk.getPendingMesh().length; i++) {
                        chunk.getPendingMesh()[i].generateVBOs();
                    }
                    if (chunk.getMesh() != null) {
                        for (int i = 0; i < chunk.getMesh().length; i++) {
                            chunk.getMesh()[i].dispose();
                        }
                    }
                    chunk.setMesh(chunk.getPendingMesh());
                    chunk.setPendingMesh(null);
                } else {
                    ChunkMesh[] pendingMesh = chunk.getPendingMesh();
                    chunk.setPendingMesh(null);
                    if (pendingMesh != null) {
                        for (ChunkMesh mesh : pendingMesh) {
                            mesh.dispose();
                        }
                    }
                }
            }
            PerformanceMonitor.endActivity();
        }

        int processedChunks = 0;
        for (int i = 0; i < chunksInProximityOfCamera.size(); i++) {
            RenderableChunk chunk = chunksInProximityOfCamera.get(i);
            ChunkMesh[] mesh = chunk.getMesh();

            if (i < TeraMath.clamp(renderingConfig.getMaxChunksUsedForShadowMapping(), 64, 1024)
                    && renderingConfig.isDynamicShadows() && fillShadowRenderQueue) {
                if (isChunkVisibleLight(chunk) && isChunkValidForRender(chunk)) {
                    if (triangleCount(mesh, ChunkMesh.RenderPhase.OPAQUE) > 0) {
                        renderQueues.chunksOpaqueShadow.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }
                }
            }

            if (isChunkValidForRender(chunk)) {
                if (isChunkVisible(chunk)) {
                    if (triangleCount(mesh, ChunkMesh.RenderPhase.OPAQUE) > 0) {
                        renderQueues.chunksOpaque.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.REFRACTIVE) > 0) {
                        renderQueues.chunksAlphaBlend.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.ALPHA_REJECT) > 0 && i < MAX_BILLBOARD_CHUNKS) {
                        renderQueues.chunksAlphaReject.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }

                    statVisibleChunks++;

                    if (statVisibleChunks < MAX_ANIMATED_CHUNKS) {
                        chunk.setAnimated(true);
                    } else {
                        chunk.setAnimated(false);
                    }
                }

                if (isChunkVisibleReflection(chunk)) {
                    renderQueues.chunksOpaqueReflection.add(chunk);
                }

                // Process all chunks in the area, not only the visible ones
                if (processChunkUpdates && processChunkUpdate(chunk)) {
                    processedChunks++;
                }
            }
        }

        return processedChunks;
    }

    private int triangleCount(ChunkMesh[] mesh, ChunkMesh.RenderPhase renderPhase) {
        int count = 0;

        if (mesh != null) {
            for (ChunkMesh subMesh : mesh) {
                count += subMesh.triangleCount(renderPhase);
            }
        }

        return count;
    }

    private boolean processChunkUpdate(RenderableChunk chunk) {
        if ((chunk.isDirty() || chunk.getMesh() == null)) {
            statDirtyChunks++;
            chunkMeshUpdateManager.queueChunkUpdate(chunk);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        chunkMeshUpdateManager.shutdown();
    }

    public boolean isChunkValidForRender(RenderableChunk chunk) {
        return chunk.isReady() && chunk.areAdjacentChunksReady();
    }

    public boolean isChunkVisibleLight(RenderableChunk chunk) {
        return isChunkVisible(shadowMapCamera, chunk);
    }

    public boolean isChunkVisible(RenderableChunk chunk) {
        return isChunkVisible(activeCamera, chunk);
    }

    public boolean isChunkVisible(Camera camera, RenderableChunk chunk) {
        return camera.getViewFrustum().intersects(chunk.getAABB());
    }

    public boolean isChunkVisibleReflection(RenderableChunk chunk) {
        return activeCamera.getViewFrustumReflected().intersects(chunk.getAABB());
    }

    public RenderQueuesHelper getRenderQueues() {
        return renderQueues;
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return chunkProvider;
    }

    public String getMetrics() {
        StringBuilder builder = new StringBuilder();
        builder.append("Dirty Chunks: ");
        builder.append(statDirtyChunks);
        builder.append("\n");
        builder.append("Ignored Phases: ");
        builder.append(statIgnoredPhases);
        builder.append("\n");
        builder.append("Visible Chunks: ");
        builder.append(statVisibleChunks);
        builder.append("\n");
        return builder.toString();
    }

    private static float squaredDistanceToCamera(RenderableChunk chunk) {
        Vector3f result = new Vector3f((chunk.getPosition().x + 0.5f) * ChunkConstants.SIZE_X,
                (chunk.getPosition().y + 0.5f) * ChunkConstants.SIZE_Y, (chunk.getPosition().z + 0.5f) * ChunkConstants.SIZE_Z);

        Vector3f cameraPos = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
        result.x -= cameraPos.x;
        result.y -= cameraPos.y;
        result.z -= cameraPos.z;

        return result.lengthSquared();
    }

    private static class ChunkFrontToBackComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            double distance1 = squaredDistanceToCamera(chunk1);
            double distance2 = squaredDistanceToCamera(chunk2);

            if (chunk1 == null) {
                return -1;
            } else if (chunk2 == null) {
                return 1;
            }

            if (distance1 == distance2) {
                return 0;
            }

            return distance2 > distance1 ? -1 : 1;
        }
    }

    private static class ChunkBackToFrontComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            double distance1 = squaredDistanceToCamera(chunk1);
            double distance2 = squaredDistanceToCamera(chunk2);

            if (chunk1 == null) {
                return 1;
            } else if (chunk2 == null) {
                return -1;
            }

            if (distance1 == distance2) {
                return 0;
            }

            return distance2 > distance1 ? 1 : -1;
        }
    }

}
