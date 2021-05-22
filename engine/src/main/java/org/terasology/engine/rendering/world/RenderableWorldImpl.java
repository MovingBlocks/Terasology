// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.subsystem.lwjgl.GLBufferPool;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.logic.ChunkMeshRenderer;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.joml.geom.AABBfc;
import org.terasology.math.TeraMath;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.LodChunkProvider;
import org.terasology.engine.world.chunks.RenderableChunk;
import org.terasology.engine.world.generator.ScalableWorldGenerator;
import org.terasology.engine.world.generator.WorldGenerator;

import java.util.ArrayList;
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
    private static final int MAX_LOADABLE_CHUNKS = ViewDistance.MEGA.getChunkDistance().x() * ViewDistance.MEGA.getChunkDistance().y() * ViewDistance.MEGA.getChunkDistance().z();
    private static final Vector3fc CHUNK_CENTER_OFFSET = new Vector3f(Chunks.CHUNK_SIZE).div(2);

    private static final Logger logger = LoggerFactory.getLogger(RenderableWorldImpl.class);

    private final int maxChunksForShadows = TeraMath.clamp(CoreRegistry.get(Config.class).getRendering().getMaxChunksUsedForShadowMapping(), 64, 1024);

    private final WorldProvider worldProvider;
    private ChunkProvider chunkProvider;
    private LodChunkProvider lodChunkProvider;

    private ChunkTessellator chunkTessellator;
    private final ChunkMeshUpdateManager chunkMeshUpdateManager;
    private final List<Chunk> chunksInProximityOfCamera = Lists.newArrayListWithCapacity(MAX_LOADABLE_CHUNKS);
    private BlockRegion renderableRegion = new BlockRegion(BlockRegion.INVALID);
    private ViewDistance currentViewDistance;
    private RenderQueuesHelper renderQueues;
    private ChunkMeshRenderer chunkMeshRenderer;

    private Camera playerCamera;
    private Camera shadowMapCamera;

    private Config config = CoreRegistry.get(Config.class);
    private RenderingConfig renderingConfig = config.getRendering();

    private int statDirtyChunks;
    private int statVisibleChunks;
    private int statIgnoredPhases;


    RenderableWorldImpl(Context context, Camera playerCamera) {

        worldProvider = context.get(WorldProvider.class);
        chunkProvider = context.get(ChunkProvider.class);
        chunkTessellator = context.get(ChunkTessellator.class);
        chunkMeshUpdateManager = new ChunkMeshUpdateManager(chunkTessellator, worldProvider);

        this.playerCamera = playerCamera;
        WorldGenerator worldGenerator = context.get(WorldGenerator.class);
        if (worldGenerator instanceof ScalableWorldGenerator) {
            lodChunkProvider = new LodChunkProvider(context, (ScalableWorldGenerator) worldGenerator, chunkTessellator, renderingConfig.getViewDistance(), (int) renderingConfig.getChunkLods(), calcCameraCoordinatesInChunkUnits());
        } else {
            lodChunkProvider = null;
        }

        renderQueues = new RenderQueuesHelper(new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkFrontToBackComparator()),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, new ChunkBackToFrontComparator()));
    }

    @Override
    public void onChunkLoaded(Vector3ic chunkCoordinates) {
        if (renderableRegion.contains(chunkCoordinates)) {
            Chunk chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk != null) {
                chunksInProximityOfCamera.add(chunk);
                Collections.sort(chunksInProximityOfCamera, new ChunkFrontToBackComparator());
                if (lodChunkProvider != null) {
                    lodChunkProvider.onRealChunkLoaded(chunkCoordinates);
                }
            } else {
                logger.warn("Warning: onChunkLoaded called for a null chunk!");
            }
        }
        for (Vector3ic pos : new BlockRegion(chunkCoordinates).expand(1, 1, 1)) {
            Chunk chunk = chunkProvider.getChunk(pos);
            if (chunk != null) {
                chunk.setDirty(true);
            }
        }
    }

    @Override
    public void onChunkUnloaded(Vector3ic chunkCoordinates) {
        if (renderableRegion.contains(chunkCoordinates)) {
            Chunk chunk;
            Iterator<Chunk> iterator = chunksInProximityOfCamera.iterator();
            while (iterator.hasNext()) {
                chunk = iterator.next();
                if (chunk.getPosition(new org.joml.Vector3i()).equals(chunkCoordinates)) {
                    chunk.disposeMesh();
                    iterator.remove();
                    break;
                }
            }
        }
        if (lodChunkProvider != null) {
            lodChunkProvider.onRealChunkUnloaded(chunkCoordinates);
        }
    }

    /**
     * @return true if pregeneration is complete
     */
    @Override
    public boolean pregenerateChunks() {
        boolean pregenerationIsComplete = true;

        chunkProvider.update();

        Chunk chunk;
        ChunkMesh newMesh;
        ChunkView localView;
        for (Vector3ic chunkCoordinates : calculateRenderableRegion(renderingConfig.getViewDistance())) {
            chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk == null) {
                pregenerationIsComplete = false;
            } else if (chunk.isDirty()) {
                localView = worldProvider.getLocalView(chunkCoordinates);
                if (localView == null) {
                    continue;
                }
                chunk.setDirty(false);

                newMesh = chunkTessellator.generateMesh(localView);
                newMesh.generateVBOs();
                newMesh.discardData();

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

        PerformanceMonitor.startActivity("Update Lighting");
        worldProvider.processPropagation();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Chunk update");
        chunkProvider.update();
        PerformanceMonitor.endActivity();

        PerformanceMonitor.startActivity("Update Close Chunks");
        updateChunksInProximity(calculateRenderableRegion(renderingConfig.getViewDistance()));
        PerformanceMonitor.endActivity();

        if (lodChunkProvider != null) {
            PerformanceMonitor.startActivity("Update LOD Chunks");
            lodChunkProvider.update(calcCameraCoordinatesInChunkUnits());
            PerformanceMonitor.endActivity();
        }
    }

    /**
     * Updates the list of chunks around the player.
     *
     * @return True if the list was changed
     */
    @Override
    public boolean updateChunksInProximity(BlockRegion newRenderableRegion) {
        if (!newRenderableRegion.equals(renderableRegion)) {
            Chunk chunk;
            for (Vector3ic chunkPositionToRemove : renderableRegion) {
                if (!newRenderableRegion.contains(chunkPositionToRemove)) {
                    Iterator<Chunk> nearbyChunks = chunksInProximityOfCamera.iterator();
                    for (Iterator<Chunk> it = nearbyChunks; it.hasNext(); ) {
                        chunk = it.next();
                        if (chunk.getPosition(new org.joml.Vector3i()).equals(chunkPositionToRemove)) {
                            chunk.disposeMesh();
                            nearbyChunks.remove();
                            break;
                        }

                    }
                }
            }
            boolean chunksHaveBeenAdded = false;
            for (Vector3ic chunkPositionToAdd : newRenderableRegion) {
                if (!renderableRegion.contains(chunkPositionToAdd)) {
                    chunk = chunkProvider.getChunk(chunkPositionToAdd);
                    if (chunk != null) {
                        chunksInProximityOfCamera.add(chunk);
                        chunksHaveBeenAdded = true;
                    }
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
    public boolean updateChunksInProximity(ViewDistance newViewDistance, int chunkLods) {
        if (newViewDistance != currentViewDistance || (lodChunkProvider != null && chunkLods != lodChunkProvider.getChunkLods())) {
            logger.info("New Viewing Distance: {}", newViewDistance);
            currentViewDistance = newViewDistance;
            if (lodChunkProvider != null) {
                lodChunkProvider.updateRenderableRegion(newViewDistance, chunkLods, calcCameraCoordinatesInChunkUnits());
            }
            return updateChunksInProximity(calculateRenderableRegion(newViewDistance));
        } else {
            return false;
        }
    }

    private BlockRegion calculateRenderableRegion(ViewDistance newViewDistance) {
        Vector3i cameraCoordinates = calcCameraCoordinatesInChunkUnits();
        Vector3ic renderableRegionSize = newViewDistance.getChunkDistance();
        Vector3i renderableRegionExtents = new Vector3i(renderableRegionSize.x() / 2, renderableRegionSize.y() / 2, renderableRegionSize.z() / 2);
        return new BlockRegion(cameraCoordinates).expand(renderableRegionExtents);
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset chunk
     */
    private Vector3i calcCameraCoordinatesInChunkUnits() {
        org.joml.Vector3f cameraCoordinates = playerCamera.getPosition();
        return Chunks.toChunkPos(cameraCoordinates, new Vector3i());
    }

    @Override
    public void generateVBOs() {
        PerformanceMonitor.startActivity("Building Mesh VBOs");
        ChunkMesh pendingMesh;
        chunkMeshUpdateManager.setCameraPosition(playerCamera.getPosition());
        for (Chunk chunk : chunkMeshUpdateManager.availableChunksForUpdate()) {

            if (chunk.hasPendingMesh() && chunksInProximityOfCamera.contains(chunk)) {
                pendingMesh = chunk.getPendingMesh();
                pendingMesh.generateVBOs();
                pendingMesh.discardData();
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

        renderQueues.clear();

        ChunkMesh mesh;
        boolean isDynamicShadows = renderingConfig.isDynamicShadows();

        List<RenderableChunk> allChunks = new ArrayList<>(chunksInProximityOfCamera);
        allChunks.addAll(chunkMeshRenderer.getRenderableChunks());
        if (lodChunkProvider != null) {
            lodChunkProvider.addAllChunks(allChunks);
        }

        for (RenderableChunk chunk : allChunks) {
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
            }
            chunkCounter++;
        }

        if (isFirstRenderingStageForCurrentFrame) {
            for (Chunk chunk : chunksInProximityOfCamera) {
                if (isChunkValidForRender(chunk) && (chunk.isDirty() || !chunk.hasMesh())) {
                    statDirtyChunks++;
                    chunkMeshUpdateManager.queueChunkUpdate(chunk);
                    processedChunks++;
                }
            }
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
        if (lodChunkProvider != null) {
            lodChunkProvider.shutdown();
        }
    }

    private boolean isChunkValidForRender(RenderableChunk chunk) {
        return chunk.isReady();
    }

    private boolean isChunkVisibleFromMainLight(RenderableChunk chunk) {
        //TODO: need to work out better scheme for shadowMapCamera
        if (shadowMapCamera == null) {
            return false;
        }
        return isChunkVisible(shadowMapCamera, chunk); //TODO: find an elegant way
    }

    private boolean isChunkVisible(RenderableChunk chunk) {
        return isChunkVisible(playerCamera, chunk);
    }

    private boolean isChunkVisible(Camera camera, RenderableChunk chunk) {
        return camera.hasInSight(chunk.getAABB());
    }

    private boolean isChunkVisibleReflection(RenderableChunk chunk) {
        AABBfc bounds = chunk.getAABB();
        return playerCamera.getViewFrustumReflected().testAab(bounds.minX(), bounds.minY(), bounds.minZ(),
                bounds.maxX(), bounds.maxY(), bounds.maxZ());
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
    public void setChunkMeshRenderer(ChunkMeshRenderer meshes) {
        chunkMeshRenderer = meshes;
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
        Vector3f result = chunk.getRenderPosition();
        result.add(CHUNK_CENTER_OFFSET);

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
