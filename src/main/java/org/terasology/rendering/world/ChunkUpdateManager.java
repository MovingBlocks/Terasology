/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldView;
import org.terasology.world.chunks.Chunk;

import java.util.Set;

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
    private static final Set<Chunk> currentlyProcessedChunks = Sets.newHashSet();

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

        // Create a new thread and start processing
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ChunkMesh[] newMeshes = new ChunkMesh[WorldRenderer.VERTICAL_SEGMENTS];
                WorldView worldView = worldProvider.getLocalView(c.getPos());
                if (worldView != null) {
                    c.setDirty(false);
                    for (int seg = 0; seg < WorldRenderer.VERTICAL_SEGMENTS; seg++) {
                        newMeshes[seg] = tessellator.generateMesh(worldView, c.getPos(), Chunk.SIZE_Y / WorldRenderer.VERTICAL_SEGMENTS, seg * (Chunk.SIZE_Y / WorldRenderer.VERTICAL_SEGMENTS));
                    }

                    c.setPendingMesh(newMeshes);

                }
                currentlyProcessedChunks.remove(c);
            }
        };

        CoreRegistry.get(GameEngine.class).submitTask("Chunk Update", r);
    }

}
