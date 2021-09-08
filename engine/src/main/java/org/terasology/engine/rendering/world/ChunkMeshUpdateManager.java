// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.monitoring.chunk.ChunkMonitor;
import org.terasology.engine.rendering.primitives.ChunkMesh;
import org.terasology.engine.rendering.primitives.ChunkTessellator;
import org.terasology.engine.utilities.concurrency.TaskMaster;
import org.terasology.engine.world.ChunkView;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.pipeline.ChunkTask;
import org.terasology.engine.world.chunks.pipeline.ShutdownChunkTask;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the mechanism for updating and generating chunk meshes.
 *
 */
public final class ChunkMeshUpdateManager {
    private static final int NUM_TASK_THREADS = 8;

    private static final Logger logger = LoggerFactory.getLogger(ChunkMeshUpdateManager.class);

    /* CHUNK UPDATES */
    private final Set<Chunk> chunksProcessing = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final BlockingDeque<Chunk> chunksComplete = Queues.newLinkedBlockingDeque();

    private TaskMaster<ChunkTask> chunkUpdater;

    private final ChunkTessellator tessellator;
    private final WorldProvider worldProvider;
    /**
     * This variable is volatile, so that it's value is visible to worker thread that calculates the best task to
     * process
     */
    private volatile float cameraChunkPosX;
    private volatile float cameraChunkPosY;
    private volatile float cameraChunkPosZ;

    public ChunkMeshUpdateManager(ChunkTessellator tessellator, WorldProvider worldProvider) {
        this.tessellator = tessellator;
        this.worldProvider = worldProvider;

        chunkUpdater = TaskMaster.createPriorityTaskMaster("Chunk-Updater", NUM_TASK_THREADS, 100, new ChunkUpdaterComparator());
    }

    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored. Chunk updates can be forced though.
     *
     * @param chunk The chunk to update
     * @return True if a chunk update was executed
     */
    // TODO: Review this system
    public boolean queueChunkUpdate(Chunk chunk) {

        if (!chunksProcessing.contains(chunk)) {
            executeChunkUpdate(chunk);
            return true;
        }

        return false;
    }

    /**
     * The method tells the chunk mesh update manager where the camera is, so that is able to prioritize chunks near the
     * camera. It stores the values in volatile variables so that the change is visible to the chunk updating threads
     * immediately.
     */
    public void setCameraPosition(Vector3f cameraPosition) {
        Vector3i chunkPos = Chunks.toChunkPos(new Vector3i(cameraPosition, RoundingMode.FLOOR));
        cameraChunkPosX = chunkPos.x;
        cameraChunkPosY = chunkPos.y;
        cameraChunkPosZ = chunkPos.z;
    }

    public List<Chunk> availableChunksForUpdate() {
        List<Chunk> result = Lists.newArrayListWithExpectedSize(chunksComplete.size());
        chunksComplete.drainTo(result);
        chunksProcessing.removeAll(result);
        return result;
    }

    private void executeChunkUpdate(final Chunk c) {
        chunksProcessing.add(c);

        ChunkUpdateTask task = new ChunkUpdateTask(c, tessellator, worldProvider, this);
        try {
            chunkUpdater.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    private void finishedProcessing(Chunk c) {
        chunksComplete.add(c);
    }

    public void shutdown() {
        chunkUpdater.shutdown(new ShutdownChunkTask(), false);
    }


    private static class ChunkUpdateTask implements ChunkTask {

        private Chunk c;
        private ChunkTessellator tessellator;
        private WorldProvider worldProvider;
        private ChunkMeshUpdateManager chunkMeshUpdateManager;

        ChunkUpdateTask(Chunk chunk, ChunkTessellator tessellator, WorldProvider worldProvider, ChunkMeshUpdateManager chunkMeshUpdateManager) {
            this.chunkMeshUpdateManager = chunkMeshUpdateManager;
            this.c = chunk;
            this.tessellator = tessellator;
            this.worldProvider = worldProvider;
        }


        @Override
        public String getName() {
            return "Update chunk";
        }

        @Override
        public boolean isTerminateSignal() {
            return false;
        }

        @Override
        public void run() {
            ChunkMesh newMesh;
            ChunkView chunkView = worldProvider.getLocalView(c.getPosition());
            if (chunkView != null) {
                /*
                 * Important set dirty flag first, so that a concurrent modification of the chunk in the mean time we
                 * will end up with a dirty chunk.
                 */
                c.setDirty(false);
                if (chunkView.isValidView()) {
                    newMesh = tessellator.generateMesh(chunkView);

                    c.setPendingMesh(newMesh);
                    ChunkMonitor.fireChunkTessellated(c, newMesh);
                }

            }
            chunkMeshUpdateManager.finishedProcessing(c);
            // Clean these up because the task executor holds the object in memory.
            c = null;
            tessellator = null;
            worldProvider = null;
        }

        @Override
        public Chunk getChunk() {
            return (Chunk) c;
        }
    }

    private class ChunkUpdaterComparator implements Comparator<ChunkTask> {
        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1) - score(o2);
        }

        private int score(ChunkTask task) {
            if (task.isTerminateSignal()) {
                return -1;
            }
            return distFromRegion(task.getPosition(), new Vector3i(cameraChunkPosX, cameraChunkPosY, cameraChunkPosZ, RoundingMode.FLOOR));
        }

        private int distFromRegion(Vector3i pos, Vector3i regionCenter) {
            return (int) pos.gridDistance(regionCenter);
        }
    }
}
