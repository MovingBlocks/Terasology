/*
 * Copyright 2013 Moving Blocks
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

import com.google.common.collect.Sets;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunk;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides the mechanism for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager {

    public enum UPDATE_TYPE {
        DEFAULT, PLAYER_TRIGGERED
    }

    /* CONST */
    private static final int MAX_THREADS = CoreRegistry.get(Config.class).getSystem().getMaxThreads();

    /* CHUNK UPDATES */
    private static final Set<Chunk> currentlyProcessedChunks = Sets.newSetFromMap(new ConcurrentHashMap<Chunk, Boolean>());

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
     * @param chunk The chunk to update
     * @param type  The chunk update type
     * @return True if a chunk update was executed
     */
    // TODO: Review this system
    public boolean queueChunkUpdate(Chunk chunk, final UPDATE_TYPE type) {

        if (!currentlyProcessedChunks.contains(chunk) && (currentlyProcessedChunks.size() < MAX_THREADS || type != UPDATE_TYPE.DEFAULT)) {
            executeChunkUpdate(chunk);
            return true;
        }

        return false;
    }

    private void executeChunkUpdate(final Chunk c) {
        currentlyProcessedChunks.add(c);

        CoreRegistry.get(GameEngine.class).submitTask("Chunk Update", new ChunkUpdater(c, tessellator, worldProvider));
    }

    private static class ChunkUpdater implements Runnable {

        private Chunk c;
        private ChunkTessellator tessellator;
        private WorldProvider worldProvider;

        public ChunkUpdater(Chunk chunk, ChunkTessellator tessellator, WorldProvider worldProvider) {
            this.c = chunk;
            this.tessellator = tessellator;
            this.worldProvider = worldProvider;
        }

        @Override
        public void run() {
            ChunkMesh[] newMeshes = new ChunkMesh[WorldRenderer.VERTICAL_SEGMENTS];
            ChunkView chunkView = worldProvider.getLocalView(c.getPos());
            if (chunkView != null) {
                c.setDirty(false);
                for (int seg = 0; seg < WorldRenderer.VERTICAL_SEGMENTS; seg++) {
                    newMeshes[seg] = tessellator.generateMesh(chunkView, c.getPos(), Chunk.SIZE_Y / WorldRenderer.VERTICAL_SEGMENTS, seg * (Chunk.SIZE_Y / WorldRenderer.VERTICAL_SEGMENTS));
                }

                c.setPendingMesh(newMeshes);

            }
            currentlyProcessedChunks.remove(c);
            // Clean these up because the task executor holds the object in memory.
            c = null;
            tessellator = null;
            worldProvider = null;
        }
    }

}
