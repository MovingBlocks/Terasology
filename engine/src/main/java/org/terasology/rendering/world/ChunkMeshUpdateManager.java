/*
 * Copyright 2013 MovingBlocks
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
import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.RenderableChunk;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.chunks.pipeline.ShutdownChunkTask;

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
    private final Set<RenderableChunk> chunksProcessing = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final BlockingDeque<RenderableChunk> chunksComplete = Queues.newLinkedBlockingDeque();

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

        chunkUpdater = TaskMaster.createDynamicPriorityTaskMaster("Chunk-Updater", NUM_TASK_THREADS, new ChunkUpdaterComparator());
    }

    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored. Chunk updates can be forced though.
     *
     * @param chunk The chunk to update
     * @return True if a chunk update was executed
     */
    // TODO: Review this system
    public boolean queueChunkUpdate(RenderableChunk chunk) {

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
        Vector3i chunkPos = ChunkMath.calcChunkPos(cameraPosition);
        cameraChunkPosX = chunkPos.x;
        cameraChunkPosY = chunkPos.y;
        cameraChunkPosZ = chunkPos.z;
    }

    public List<RenderableChunk> availableChunksForUpdate() {
        List<RenderableChunk> result = Lists.newArrayListWithExpectedSize(chunksComplete.size());
        chunksComplete.drainTo(result);
        chunksProcessing.removeAll(result);
        return result;
    }

    private void executeChunkUpdate(final RenderableChunk c) {
        chunksProcessing.add(c);

        ChunkUpdateTask task = new ChunkUpdateTask(c, tessellator, worldProvider, this);
        try {
            chunkUpdater.put(task);
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue task {}", task, e);
        }
    }

    private void finishedProcessing(RenderableChunk c) {
        chunksComplete.add(c);
    }

    public void shutdown() {
        chunkUpdater.shutdown(new ShutdownChunkTask(), false);
    }


    private static class ChunkUpdateTask implements ChunkTask {

        private RenderableChunk c;
        private ChunkTessellator tessellator;
        private WorldProvider worldProvider;
        private ChunkMeshUpdateManager chunkMeshUpdateManager;

        public ChunkUpdateTask(RenderableChunk chunk, ChunkTessellator tessellator, WorldProvider worldProvider, ChunkMeshUpdateManager chunkMeshUpdateManager) {
            this.chunkMeshUpdateManager = chunkMeshUpdateManager;
            this.c = chunk;
            this.tessellator = tessellator;
            this.worldProvider = worldProvider;
        }

        @Override
        public Vector3i getPosition() {
            return c.getPosition();
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
                c.setDirty(false);
                chunkView.readLock();
                try {
                    if (chunkView.isValidView()) {
                        newMesh = tessellator.generateMesh(chunkView, ChunkConstants.SIZE_Y, 0);

                        c.setPendingMesh(newMesh);
                        ChunkMonitor.fireChunkTessellated(c.getPosition(), newMesh);
                    }
                } finally {
                    chunkView.readUnlock();
                }

            }
            chunkMeshUpdateManager.finishedProcessing(c);
            // Clean these up because the task executor holds the object in memory.
            c = null;
            tessellator = null;
            worldProvider = null;
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
            return distFromRegion(task.getPosition(), new Vector3i(cameraChunkPosX, cameraChunkPosY, cameraChunkPosZ));
        }

        private int distFromRegion(Vector3i pos, Vector3i regionCenter) {
            return pos.gridDistance(regionCenter);
        }
    }
}
