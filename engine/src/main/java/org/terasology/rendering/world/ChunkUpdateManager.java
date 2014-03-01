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
import com.google.common.collect.Sets;
import org.terasology.config.Config;
import org.terasology.engine.GameEngine;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the mechanism for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager {

    /* CONST */
    private static final int MAX_THREADS = CoreRegistry.get(Config.class).getSystem().getMaxThreads();

    /* CHUNK UPDATES */
    private final Set<ChunkImpl> chunksProcessing = Sets.newSetFromMap(new ConcurrentHashMap<ChunkImpl, Boolean>());

    private final BlockingDeque<ChunkImpl> chunksComplete = Queues.newLinkedBlockingDeque();

    private final ChunkTessellator tessellator;
    private final WorldProvider worldProvider;

    public ChunkUpdateManager(ChunkTessellator tessellator, WorldProvider worldProvider) {
        this.tessellator = tessellator;
        this.worldProvider = worldProvider;
    }

    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored. Chunk updates can be forced though.
     *
     *
     * @param chunk The chunk to update
     * @return True if a chunk update was executed
     */
    // TODO: Review this system
    public boolean queueChunkUpdate(ChunkImpl chunk) {

        if (!chunksProcessing.contains(chunk)) {
            executeChunkUpdate(chunk);
            return true;
        }

        return false;
    }

    public List<ChunkImpl> availableChunksForUpdate() {
        List<ChunkImpl> result = Lists.newArrayListWithExpectedSize(chunksComplete.size());
        chunksComplete.drainTo(result);
        chunksProcessing.removeAll(result);
        return result;
    }

    private void executeChunkUpdate(final ChunkImpl c) {
        chunksProcessing.add(c);

        CoreRegistry.get(GameEngine.class).submitTask("Chunk Update", new ChunkUpdater(c, tessellator, worldProvider, this));
    }

    private void finishedProcessing(ChunkImpl c) {
        chunksComplete.add(c);
    }


    private static class ChunkUpdater implements Runnable {

        private ChunkImpl c;
        private ChunkTessellator tessellator;
        private WorldProvider worldProvider;
        private ChunkUpdateManager chunkUpdateManager;

        public ChunkUpdater(ChunkImpl chunk, ChunkTessellator tessellator, WorldProvider worldProvider, ChunkUpdateManager chunkUpdateManager) {
            this.chunkUpdateManager = chunkUpdateManager;
            this.c = chunk;
            this.tessellator = tessellator;
            this.worldProvider = worldProvider;
        }

        @Override
        public void run() {
            ChunkMesh[] newMeshes = new ChunkMesh[WorldRendererLwjgl.VERTICAL_SEGMENTS];
            ChunkView chunkView = worldProvider.getLocalView(c.getPos());
            if (chunkView != null) {
                c.setDirty(false);
                for (int seg = 0; seg < WorldRendererLwjgl.VERTICAL_SEGMENTS; seg++) {
                    int meshHeight = ChunkConstants.SIZE_Y / WorldRendererLwjgl.VERTICAL_SEGMENTS;
                    newMeshes[seg] = tessellator.generateMesh(chunkView, c.getPos(), meshHeight, seg * (ChunkConstants.SIZE_Y / WorldRendererLwjgl.VERTICAL_SEGMENTS));
                }

                c.setPendingMesh(newMeshes);
                ChunkMonitor.fireChunkTessellated(c.getPos(), newMeshes);

            }
            chunkUpdateManager.finishedProcessing(c);
            // Clean these up because the task executor holds the object in memory.
            c = null;
            tessellator = null;
            worldProvider = null;
        }
    }

}
