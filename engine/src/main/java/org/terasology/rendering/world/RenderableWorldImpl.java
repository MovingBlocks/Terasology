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
package org.terasology.rendering.world;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.world.viewDistance.ViewDistance;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.RenderableChunk;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * TODO: write javadoc unless this class gets slated for removal, which might be.
 */
class RenderableWorldImpl implements RenderableWorld {

    private static final int MAX_ANIMATED_CHUNKS = 64;
    private static final int MAX_BILLBOARD_CHUNKS = 64;
    private static final int MAX_LOADABLE_CHUNKS = ViewDistance.MEGA.getChunkDistance().x * ViewDistance.MEGA.getChunkDistance().y * ViewDistance.MEGA.getChunkDistance().z;
    private static final Vector3f CHUNK_CENTER_OFFSET = new Vector3f(0.5f, 0.5f, 0.5f);

    private static final Logger logger = LoggerFactory.getLogger(RenderableWorldImpl.class);

    private final int maxChunksForShadows = TeraMath.clamp(CoreRegistry.get(Config.class).getRendering().getMaxChunksUsedForShadowMapping(), 64, 1024);

    private final WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private ChunkTessellator chunkTessellator;
    private final ChunkMeshUpdateManager chunkMeshUpdateManager;
    private final List<RenderableChunk> chunksInProximityOfCamera = Lists.newArrayListWithCapacity(MAX_LOADABLE_CHUNKS);
    private Region3i renderableRegion = Region3i.empty();
    private ViewDistance currentViewDistance;
    private RenderQueuesHelper renderQueues;

    private Camera playerCamera;
    private Camera shadowMapCamera;

    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();

    private int statDirtyChunks;
    private int statVisibleChunks;
    private int statIgnoredPhases;


    RenderableWorldImpl(WorldProvider worldProvider,
                               ChunkProvider chunkProvider,
                               GLBufferPool bufferPool,
                               Camera playerCamera) {

        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;
        chunkTessellator = new ChunkTessellator(bufferPool);
        chunkMeshUpdateManager = new ChunkMeshUpdateManager(chunkTessellator, worldProvider);

        this.playerCamera = playerCamera;

        renderQueues = new RenderQueuesHelper(new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkBackToFrontComparator()));
    }

    @Override
    public void onChunkLoaded(Vector3i chunkCoordinates) {
        if (renderableRegion.encompasses(chunkCoordinates)) {
            Chunk chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk != null) {
                chunksInProximityOfCamera.add(chunk);
                Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
            } else {
                logger.warn("Warning: onChunkLoaded called for a null chunk!");
            }
        }
    }

    @Override
    public void onChunkUnloaded(Vector3i chunkCoordinates) {
        if (renderableRegion.encompasses(chunkCoordinates)) {
            RenderableChunk chunk;
            Iterator<RenderableChunk> iterator = chunksInProximityOfCamera.iterator();
            while (iterator.hasNext()) {
                chunk = iterator.next();
                if (chunk.getPosition().equals(chunkCoordinates)) {
                    chunk.disposeMesh();
                    iterator.remove();
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

        chunkProvider.completeUpdate();
        chunkProvider.beginUpdate();

        RenderableChunk chunk;
        ChunkMesh newMesh;
        ChunkView localView;
        for (Vector3i chunkCoordinates : calculateRenderableRegion(renderingConfig.getViewDistance())) {
            chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk == null) {
                pregenerationIsComplete = false;
            } else if (chunk.isDirty()) {
                localView = worldProvider.getLocalView(chunkCoordinates);
                if (localView == null) {
                    continue;
                }
                chunk.setDirty(false);

                newMesh = chunkTessellator.generateMesh(localView, ChunkConstants.SIZE_Y, 0);
                newMesh.generateVBOs();

                if (chunk.hasMesh()) {
                    chunk.getMesh().dispose();
                }
                chunk.setMesh(newMesh);

                pregenerationIsComplete = false;
                break;
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
    @Override
    public boolean updateChunksInProximity(Region3i newRenderableRegion) {
        if (!newRenderableRegion.equals(renderableRegion)) {
            Vector3i chunkPosition;
            RenderableChunk chunk;

            Iterator<Vector3i> chunksToRemove = renderableRegion.subtract(newRenderableRegion);
            while (chunksToRemove.hasNext()) {
                chunkPosition = chunksToRemove.next();
                Iterator<RenderableChunk> nearbyChunks = chunksInProximityOfCamera.iterator();
                while (nearbyChunks.hasNext()) {
                    chunk = nearbyChunks.next();
                    if (chunk.getPosition().equals(chunkPosition)) {
                        chunk.disposeMesh();
                        nearbyChunks.remove();
                        break;
                    }
                }
            }

            boolean chunksHaveBeenAdded = false;
            Iterator<Vector3i> chunksToAdd = newRenderableRegion.subtract(renderableRegion);
            while (chunksToAdd.hasNext()) {
                chunkPosition = chunksToAdd.next();
                chunk = chunkProvider.getChunk(chunkPosition);
                if (chunk != null) {
                    chunksInProximityOfCamera.add(chunk);
                    chunksHaveBeenAdded = true;
                }
            }

            if (chunksHaveBeenAdded) {
                Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
            }
            renderableRegion = newRenderableRegion;
            return true;
        }

        return false;
    }

    @Override
    public boolean updateChunksInProximity(ViewDistance newViewDistance) {
        if (newViewDistance != currentViewDistance) {
            logger.info("New Viewing Distance: {}", newViewDistance);
            currentViewDistance = newViewDistance;
            return updateChunksInProximity(calculateRenderableRegion(newViewDistance));
        } else {
            return false;
        }
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
        Vector3f cameraCoordinates = playerCamera.getPosition();
        return new Vector3i((int) (cameraCoordinates.x / ChunkConstants.SIZE_X),
                (int) (cameraCoordinates.y / ChunkConstants.SIZE_Y),
                (int) (cameraCoordinates.z / ChunkConstants.SIZE_Z));
    }

    @Override
    public void generateVBOs() {
        PerformanceMonitor.startActivity("Building Mesh VBOs");
        ChunkMesh pendingMesh;
        chunkMeshUpdateManager.setCameraPosition(playerCamera.getPosition());
        for (RenderableChunk chunk : chunkMeshUpdateManager.availableChunksForUpdate()) {

            if (chunk.hasPendingMesh() && chunksInProximityOfCamera.contains(chunk)) {
                pendingMesh = chunk.getPendingMesh();
                pendingMesh.generateVBOs();
                if (chunk.hasMesh()) {
                    chunk.getMesh().dispose();
                }
                chunk.setMesh(pendingMesh);
                chunk.setPendingMesh(null);

            } else {
                if (chunk.hasPendingMesh()) {
                    chunk.getPendingMesh().dispose();
                    chunk.setPendingMesh(null);
                }
            }
        }
        PerformanceMonitor.endActivity();
    }

    /**
     * Updates the currently visible chunks (in sight of the player).
     */
    @Override
    public int queueVisibleChunks(boolean isFirstRenderingStageForCurrentFrame) {
        PerformanceMonitor.startActivity("Queueing Visible Chunks");
        statDirtyChunks = 0;
        statVisibleChunks = 0;
        statIgnoredPhases = 0;

        int processedChunks = 0;
        int chunkCounter = 0;
        ChunkMesh mesh;
        boolean isDynamicShadows = renderingConfig.isDynamicShadows();

        for (RenderableChunk chunk : chunksInProximityOfCamera) {
            if (isChunkValidForRender(chunk)) {
                mesh = chunk.getMesh();

                if (isDynamicShadows && isFirstRenderingStageForCurrentFrame && chunkCounter < maxChunksForShadows && isChunkVisibleFromMainLight(chunk)) {
                    if (triangleCount(mesh, ChunkMesh.RenderPhase.OPAQUE) > 0) {
                        renderQueues.chunksOpaqueShadow.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }
                }

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

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.ALPHA_REJECT) > 0 && chunkCounter < MAX_BILLBOARD_CHUNKS) {
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
                if (isFirstRenderingStageForCurrentFrame && (chunk.isDirty() || !chunk.hasMesh())) {
                    statDirtyChunks++;
                    chunkMeshUpdateManager.queueChunkUpdate(chunk);
                    processedChunks++;
                }
            }
            chunkCounter++;
        }

        PerformanceMonitor.endActivity();
        return processedChunks;
    }

    private int triangleCount(ChunkMesh mesh, ChunkMesh.RenderPhase renderPhase) {
        if (mesh != null) {
            return mesh.triangleCount(renderPhase);
        } else {
            return 0;
        }
    }

    @Override
    public void dispose() {
        chunkMeshUpdateManager.shutdown();
    }

    private boolean isChunkValidForRender(RenderableChunk chunk) {
        return chunk.isReady() && areSurroundingChunksLoaded(chunk);
    }

    private boolean areSurroundingChunksLoaded(RenderableChunk chunk) {
        return worldProvider.getWorldViewAround(chunk.getPosition()) != null;
    }

    private boolean isChunkVisibleFromMainLight(RenderableChunk chunk) {
        return isChunkVisible(shadowMapCamera, chunk); //TODO: find an elegant way
    }

    private boolean isChunkVisible(RenderableChunk chunk) {
        return isChunkVisible(playerCamera, chunk);
    }

    private boolean isChunkVisible(Camera camera, RenderableChunk chunk) {
        return camera.hasInSight(chunk.getAABB());
    }

    private boolean isChunkVisibleReflection(RenderableChunk chunk) {
        return playerCamera.getViewFrustumReflected().intersects(chunk.getAABB());
    }

    @Override
    public RenderQueuesHelper getRenderQueues() {
        return renderQueues;
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return chunkProvider;
    }

    @Override
    public void setShadowMapCamera(Camera camera) {
        this.shadowMapCamera = camera;
    }

    @Override
    public String getMetrics() {
        String stringToReturn = "";
        stringToReturn += "Dirty Chunks: ";
        stringToReturn += statDirtyChunks;
        stringToReturn += "\n";
        stringToReturn += "Ignored Phases: ";
        stringToReturn += statIgnoredPhases;
        stringToReturn += "\n";
        stringToReturn += "Visible Chunks: ";
        stringToReturn += statVisibleChunks;
        stringToReturn += "\n";
        return stringToReturn;
    }

    private static float squaredDistanceToCamera(RenderableChunk chunk, Vector3f cameraPosition) {
        // For performance reasons, to avoid instantiating too many vectors in a frequently called method,
        // comments are in use instead of appropriately named vectors.
        Vector3f result = chunk.getPosition().toVector3f(); // chunk position in chunk coordinates
        result.add(CHUNK_CENTER_OFFSET);                    // chunk center in chunk coordinates

        result.x *= ChunkConstants.SIZE_X;    // chunk center in world coordinates
        result.y *= ChunkConstants.SIZE_Y;
        result.z *= ChunkConstants.SIZE_Z;

        result.sub(cameraPosition); // camera to chunk vector

        return result.lengthSquared();
    }

    // TODO: find the right place to check if the activeCamera has changed,
    // TODO: so that the comparators can hold an up-to-date reference to it
    // TODO: and avoid having to find it on a per-comparison basis.
    private static class ChunkFrontToBackComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            Preconditions.checkNotNull(chunk1);
            Preconditions.checkNotNull(chunk2);
            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            double distance1 = squaredDistanceToCamera(chunk1, cameraPosition);
            double distance2 = squaredDistanceToCamera(chunk2, cameraPosition);

            // Using Double.compare as simple d1 < d2 comparison is flagged as problematic by Jenkins
            // On the other hand Double.compare can return any positive/negative value apparently,
            // hence the need for Math.signum().
            return (int) Math.signum(Double.compare(distance1, distance2));
        }
    }

    private static class ChunkBackToFrontComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            Preconditions.checkNotNull(chunk1);
            Preconditions.checkNotNull(chunk2);
            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            double distance1 = squaredDistanceToCamera(chunk1, cameraPosition);
            double distance2 = squaredDistanceToCamera(chunk2, cameraPosition);

            if (distance1 == distance2) {
                return 0;
            } else if (distance2 > distance1) {
                return 1;
            } else {
                return -1;
            }
        }
    }

}
