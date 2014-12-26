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
import org.terasology.engine.subsystem.lwjgl.GLBufferPool;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Vector3f;
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
    private static final int MAX_CHUNKS = ViewDistance.MEGA.getChunkDistance().x * ViewDistance.MEGA.getChunkDistance().y * ViewDistance.MEGA.getChunkDistance().z;

    private static final Logger logger = LoggerFactory.getLogger(RenderableWorldImpl.class);

    private final WorldProvider worldProvider;
    private ChunkProvider chunkProvider;

    private Camera activeCamera;
    private Camera lightCamera;

    private ChunkTessellator chunkTessellator;
    private final ChunkMeshUpdateManager chunkMeshUpdateManager;
    // TODO: Review usage of ChunkImpl throughout WorldRenderer
    private final List<RenderableChunk> chunksInProximity = Lists.newArrayListWithCapacity(MAX_CHUNKS);
    private Region3i renderRegion = Region3i.EMPTY;
    private RenderQueuesHelper renderQueues;

    private Config config;

    private int statDirtyChunks;
    private int statVisibleChunks;
    private int statIgnoredPhases;

    public RenderableWorldImpl(WorldProvider worldProvider, ChunkProvider chunkProvider, GLBufferPool bufferPool, Camera activeCamera, Camera lightCamera) {
        this.worldProvider = worldProvider;
        this.chunkProvider = chunkProvider;

        chunkTessellator = new ChunkTessellator(bufferPool);
        chunkMeshUpdateManager = new ChunkMeshUpdateManager(chunkTessellator, worldProvider);

        this.activeCamera = activeCamera;
        this.lightCamera = lightCamera;

        config = CoreRegistry.get(Config.class);

        renderQueues = new RenderQueuesHelper(new PriorityQueue<>(MAX_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_CHUNKS, new ChunkFrontToBackComparator()),
                                              new PriorityQueue<>(MAX_CHUNKS, new ChunkBackToFrontComparator()));
    }

    @Override
    public void onChunkLoaded(Vector3i pos) {
        if (renderRegion.encompasses(pos)) {
            RenderableChunk chunk = chunkProvider.getChunk(pos);
            chunksInProximity.add(chunk);
            Collections.sort(chunksInProximity, new ChunkFrontToBackComparator());
        }
    }

    @Override
    public void onChunkUnloaded(Vector3i pos) {
        if (renderRegion.encompasses(pos)) {
            Iterator<RenderableChunk> iterator = chunksInProximity.iterator();
            while (iterator.hasNext()) {
                RenderableChunk chunk = iterator.next();
                if (chunk.getPosition().equals(pos)) {
                    chunk.disposeMesh();
                    iterator.remove();
                    Collections.sort(chunksInProximity, new ChunkFrontToBackComparator());
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
        boolean complete = true;
        Vector3i newChunkPos = calcCamChunkOffset();
        Vector3i viewingDistance = config.getRendering().getViewDistance().getChunkDistance();

        chunkProvider.completeUpdate();
        chunkProvider.beginUpdate();
        for (Vector3i pos : Region3i.createFromCenterExtents(newChunkPos, new Vector3i(viewingDistance.x / 2, viewingDistance.y / 2, viewingDistance.z / 2))) {
            RenderableChunk chunk = chunkProvider.getChunk(pos);
            if (chunk == null) {
                complete = false;
            } else if (chunk.isDirty()) {
                ChunkView view = worldProvider.getLocalView(chunk.getPosition());
                if (view == null) {
                    continue;
                }
                chunk.setDirty(false);

                ChunkMesh[] newMeshes = new ChunkMesh[VERTICAL_SEGMENTS];
                for (int seg = 0; seg < VERTICAL_SEGMENTS; seg++) {
                    newMeshes[seg] = chunkTessellator.generateMesh(view,
                            ChunkConstants.SIZE_Y / VERTICAL_SEGMENTS, seg * (ChunkConstants.SIZE_Y / VERTICAL_SEGMENTS));
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
                return false;
            }
        }
        return complete;
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
        updateChunksInProximity(calculateViewRegion(config.getRendering().getViewDistance()));
        PerformanceMonitor.endActivity();

    }

    /**
     * Updates the list of chunks around the player.
     *
     * @return True if the list was changed
     */
    public boolean updateChunksInProximity(Region3i newRegion) {
        if (!newRegion.equals(renderRegion)) {
            Iterator<Vector3i> removeChunks = renderRegion.subtract(newRegion);
            while (removeChunks.hasNext()) {
                Vector3i pos = removeChunks.next();

                Iterator<RenderableChunk> iterator = chunksInProximity.iterator();
                while (iterator.hasNext()) {
                    RenderableChunk chunk = iterator.next();
                    if (chunk.getPosition().equals(pos)) {
                        chunk.disposeMesh();
                        iterator.remove();
                        break;
                    }
                }
            }

            Iterator<Vector3i> addChunks = newRegion.subtract(renderRegion);
            while (addChunks.hasNext()) {
                Vector3i pos = addChunks.next();
                RenderableChunk c = chunkProvider.getChunk(pos);
                if (c != null) {
                    chunksInProximity.add(c);
                }
            }

            renderRegion = newRegion;
            Collections.sort(chunksInProximity, new ChunkFrontToBackComparator());
            return true;
        }
        return false;
    }

    public boolean updateChunksInProximity(ViewDistance viewDistance) {
        logger.info("New Viewing Distance: {}", viewDistance);
        return updateChunksInProximity(calculateViewRegion(viewDistance));
    }

    private Region3i calculateViewRegion(ViewDistance viewingDistance) {
        Vector3i newChunkPos = calcCamChunkOffset();
        Vector3i distance = viewingDistance.getChunkDistance();
        return Region3i.createFromCenterExtents(newChunkPos, new Vector3i(distance.x / 2, distance.y / 2, distance.z / 2));
    }

    /**
     * Chunk position of the player.
     *
     * @return The player offset chunk
     */
    private Vector3i calcCamChunkOffset() {
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
            for (RenderableChunk c : chunkMeshUpdateManager.availableChunksForUpdate()) {
                if (chunksInProximity.contains(c) && c.getPendingMesh() != null) {
                    for (int i = 0; i < c.getPendingMesh().length; i++) {
                        c.getPendingMesh()[i].generateVBOs();
                    }
                    if (c.getMesh() != null) {
                        for (int i = 0; i < c.getMesh().length; i++) {
                            c.getMesh()[i].dispose();
                        }
                    }
                    c.setMesh(c.getPendingMesh());
                    c.setPendingMesh(null);
                } else {
                    ChunkMesh[] pendingMesh = c.getPendingMesh();
                    c.setPendingMesh(null);
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
        for (int i = 0; i < chunksInProximity.size(); i++) {
            RenderableChunk c = chunksInProximity.get(i);
            ChunkMesh[] mesh = c.getMesh();

            if (i < TeraMath.clamp(config.getRendering().getMaxChunksUsedForShadowMapping(), 64, 1024)
                    && config.getRendering().isDynamicShadows() && fillShadowRenderQueue) {
                if (isChunkVisibleLight(c) && isChunkValidForRender(c)) {
                    if (triangleCount(mesh, ChunkMesh.RenderPhase.OPAQUE) > 0) {
                        renderQueues.chunksOpaqueShadow.add(c);
                    } else {
                        statIgnoredPhases++;
                    }
                }
            }

            if (isChunkValidForRender(c)) {
                if (isChunkVisible(c)) {
                    if (triangleCount(mesh, ChunkMesh.RenderPhase.OPAQUE) > 0) {
                        renderQueues.chunksOpaque.add(c);
                    } else {
                        statIgnoredPhases++;
                    }

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.REFRACTIVE) > 0) {
                        renderQueues.chunksAlphaBlend.add(c);
                    } else {
                        statIgnoredPhases++;
                    }

                    if (triangleCount(mesh, ChunkMesh.RenderPhase.ALPHA_REJECT) > 0 && i < MAX_BILLBOARD_CHUNKS) {
                        renderQueues.chunksAlphaReject.add(c);
                    } else {
                        statIgnoredPhases++;
                    }

                    statVisibleChunks++;

                    if (statVisibleChunks < MAX_ANIMATED_CHUNKS) {
                        c.setAnimated(true);
                    } else {
                        c.setAnimated(false);
                    }
                }

                if (isChunkVisibleReflection(c)) {
                    renderQueues.chunksOpaqueReflection.add(c);
                }

                // Process all chunks in the area, not only the visible ones
                if (processChunkUpdates && processChunkUpdate(c)) {
                    processedChunks++;
                }
            }
        }

        return processedChunks;
    }

    private int triangleCount(ChunkMesh[] mesh, ChunkMesh.RenderPhase type) {
        int count = 0;

        if (mesh != null) {
            for (ChunkMesh subMesh : mesh) {
                count += subMesh.triangleCount(type);
            }
        }

        return count;
    }

    private boolean processChunkUpdate(RenderableChunk c) {
        if ((c.isDirty() || c.getMesh() == null)) {
            statDirtyChunks++;
            chunkMeshUpdateManager.queueChunkUpdate(c);
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        chunkMeshUpdateManager.shutdown();
    }

    public boolean isChunkValidForRender(RenderableChunk c) {
        return c.isReady() && c.areAdjacentChunksReady();
    }

    public boolean isChunkVisibleLight(RenderableChunk c) {
        return isChunkVisible(lightCamera, c);
    }

    public boolean isChunkVisible(RenderableChunk c) {
        return isChunkVisible(activeCamera, c);
    }

    public boolean isChunkVisible(Camera cam, RenderableChunk c) {
        return cam.getViewFrustum().intersects(c.getAABB());
    }

    public boolean isChunkVisibleReflection(RenderableChunk c) {
        return activeCamera.getViewFrustumReflected().intersects(c.getAABB());
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

    private static float distanceToCamera(RenderableChunk chunk) {
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
        public int compare(RenderableChunk o1, RenderableChunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            if (distance == distance2) {
                return 0;
            }

            return distance2 > distance ? -1 : 1;
        }
    }

    private static class ChunkBackToFrontComparator implements Comparator<RenderableChunk> {

        @Override
        public int compare(RenderableChunk o1, RenderableChunk o2) {
            double distance = distanceToCamera(o1);
            double distance2 = distanceToCamera(o2);

            if (o1 == null) {
                return 1;
            } else if (o2 == null) {
                return -1;
            }

            if (distance == distance2) {
                return 0;
            }

            return distance2 > distance ? 1 : -1;
        }
    }

}
