// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import com.google.common.base.Preconditions;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.logic.ChunkMeshRenderer;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.rendering.world.viewDistance.ViewDistance;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.LodChunkProvider;
import org.terasology.engine.world.chunks.RenderableChunk;
import org.terasology.joml.geom.AABBfc;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * TODO: write javadoc unless this class gets slated for removal, which might be.
 */
class RenderableWorldImpl implements RenderableWorld {
    private static final Logger logger = LoggerFactory.getLogger(RenderableWorldImpl.class);

    private static final int MAX_ANIMATED_CHUNKS = 64;
    private static final int MAX_LOADABLE_CHUNKS =
            ViewDistance.MEGA.getChunkDistance().x() * ViewDistance.MEGA.getChunkDistance().y() * ViewDistance.MEGA.getChunkDistance().z();
    private static final Vector3fc CHUNK_CENTER_OFFSET = new Vector3f(Chunks.CHUNK_SIZE).div(2);

    private final int maxChunksForShadows;

    private final WorldProvider worldProvider;
    private final ChunkProvider chunkProvider;
    private final LodChunkProvider lodChunkProvider;

    private final ChunkTessellator chunkTessellator;
    private BlockRegion renderableRegion = new BlockRegion(BlockRegion.INVALID);
    private ViewDistance currentViewDistance;
    private final RenderQueuesHelper renderQueues;
    private ChunkMeshRenderer chunkMeshRenderer;

    private final Camera playerCamera;
    private Camera shadowMapCamera;

    private final RenderingConfig renderingConfig;

    private int statDirtyChunks;
    private int statVisibleChunks;
    private int statIgnoredPhases;

    private final RenderableWorldImpl.ChunkFrontToBackComparator frontToBackComparator;
    private final RenderableWorldImpl.ChunkBackToFrontComparator backToFrontComparator;

    private final ChunkMeshWorker chunkWorker;

    RenderableWorldImpl(WorldRenderer worldRenderer, LodChunkProvider lodChunkProvider, ChunkProvider chunkProvider,
                        ChunkTessellator chunkTessellator, WorldProvider worldProvider, Config config, Camera playerCamera) {
        frontToBackComparator = new RenderableWorldImpl.ChunkFrontToBackComparator(worldRenderer);
        backToFrontComparator = new RenderableWorldImpl.ChunkBackToFrontComparator(worldRenderer);

        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;
        this.playerCamera = playerCamera;
        this.lodChunkProvider = lodChunkProvider;
        this.chunkTessellator = chunkTessellator;
        this.renderingConfig = config.getRendering();
        this.maxChunksForShadows = Math.clamp(config.getRendering().getMaxChunksUsedForShadowMapping(), 64, 1024);

        this.chunkWorker = ChunkMeshWorker.create(chunkTessellator, worldProvider, frontToBackComparator);
        renderQueues = new RenderQueuesHelper(new PriorityQueue<>(MAX_LOADABLE_CHUNKS,
                frontToBackComparator),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, frontToBackComparator),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, frontToBackComparator),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, frontToBackComparator),
                new PriorityQueue<>(MAX_LOADABLE_CHUNKS, backToFrontComparator));

    }

    @Override
    public void onChunkLoaded(Vector3ic chunkCoordinates) {
        if (renderableRegion.contains(chunkCoordinates)) {
            Chunk chunk = chunkProvider.getChunk(chunkCoordinates);
            if (chunk != null) {
                chunkWorker.add(chunk);
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
            chunkWorker.remove(chunkCoordinates);
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
                newMesh.updateMesh();
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
            for (Vector3ic chunkPositionToRemove : renderableRegion) {
                if (!newRenderableRegion.contains(chunkPositionToRemove)) {
                    chunkWorker.remove(chunkPositionToRemove);
                }
            }
            for (Vector3ic chunkPositionToAdd : newRenderableRegion) {
                if (!renderableRegion.contains(chunkPositionToAdd)) {
                    Chunk chunk = chunkProvider.getChunk(chunkPositionToAdd);
                    if (chunk != null) {
                        chunkWorker.add(chunk);
                    }
                }
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
                lodChunkProvider.updateRenderableRegion(newViewDistance, chunkLods,
                        calcCameraCoordinatesInChunkUnits());
            }
            return updateChunksInProximity(calculateRenderableRegion(newViewDistance));
        } else {
            return false;
        }
    }

    private BlockRegion calculateRenderableRegion(ViewDistance newViewDistance) {
        Vector3i cameraCoordinates = calcCameraCoordinatesInChunkUnits();
        Vector3ic renderableRegionSize = newViewDistance.getChunkDistance();
        Vector3i renderableRegionExtents = new Vector3i(renderableRegionSize.x() / 2, renderableRegionSize.y() / 2,
                renderableRegionSize.z() / 2);
        return new BlockRegion(cameraCoordinates).expand(renderableRegionExtents);
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset chunk
     */
    private Vector3i calcCameraCoordinatesInChunkUnits() {
        Vector3f cameraCoordinates = playerCamera.getPosition();
        return Chunks.toChunkPos(cameraCoordinates, new Vector3i());
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

        int chunkCounter = 0;

        renderQueues.clear();

        ChunkMesh mesh;
        boolean isDynamicShadows = renderingConfig.isDynamicShadows();
        int billboardLimit = (int) renderingConfig.getBillboardLimit();

        List<RenderableChunk> allChunks = new ArrayList<>(chunkWorker.chunks());
        allChunks.addAll(chunkMeshRenderer.getRenderableChunks());
        if (lodChunkProvider != null) {
            lodChunkProvider.addAllChunks(allChunks);
        }

        for (RenderableChunk chunk : allChunks) {
            if (isChunkValidForRender(chunk)) {
                mesh = chunk.getMesh();

                if (isDynamicShadows
                        && isFirstRenderingStageForCurrentFrame
                        && chunkCounter < maxChunksForShadows
                        && isChunkVisibleFromMainLight(chunk)) {
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

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.ALPHA_REJECT) > 0
                            && (billboardLimit == 0 || chunkCounter < billboardLimit)) {
                        renderQueues.chunksAlphaReject.add(chunk);
                    } else {
                        statIgnoredPhases++;
                    }

                    statVisibleChunks++;

                    chunk.setAnimated(statVisibleChunks < MAX_ANIMATED_CHUNKS);
                }

                if (isChunkVisibleReflection(chunk)) {
                    renderQueues.chunksOpaqueReflection.add(chunk);
                }
            }
            chunkCounter++;
        }

        if (isFirstRenderingStageForCurrentFrame) {
            statDirtyChunks = chunkWorker.update();
        }

        PerformanceMonitor.endActivity();
        return chunkWorker.numberChunkMeshProcessing();
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
    public static class ChunkFrontToBackComparator implements Comparator<RenderableChunk> {

        private final WorldRenderer worldRenderer;
        ChunkFrontToBackComparator(WorldRenderer worldRenderer) {
            this.worldRenderer = worldRenderer;
        }

        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            Preconditions.checkNotNull(chunk1);
            Preconditions.checkNotNull(chunk2);
            Vector3f cameraPosition = worldRenderer.getActiveCamera().getPosition();
            double distance1 = squaredDistanceToCamera(chunk1, cameraPosition);
            double distance2 = squaredDistanceToCamera(chunk2, cameraPosition);

            // Using Double.compare as simple d1 < d2 comparison is flagged as problematic by Jenkins
            // On the other hand Double.compare can return any positive/negative value apparently,
            // hence the need for Math.signum().
            return Math.signum(Double.compare(distance1, distance2));
        }
    }

    public static class ChunkBackToFrontComparator implements Comparator<RenderableChunk> {
        private final WorldRenderer worldRenderer;
        ChunkBackToFrontComparator(WorldRenderer worldRenderer) {
            this.worldRenderer = worldRenderer;
        }
        @Override
        public int compare(RenderableChunk chunk1, RenderableChunk chunk2) {
            Preconditions.checkNotNull(chunk1);
            Preconditions.checkNotNull(chunk2);
            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
            double distance1 = squaredDistanceToCamera(chunk1, cameraPosition);
            double distance2 = squaredDistanceToCamera(chunk2, cameraPosition);

            return Double.compare(distance2, distance1);
        }
    }
}
