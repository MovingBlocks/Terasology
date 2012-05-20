/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.logic.world;

import com.google.common.collect.Sets;
import org.terasology.game.CoreRegistry;
import org.terasology.game.GameEngine;
import org.terasology.game.Terasology;
import org.terasology.logic.manager.Config;
import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.WorldProvider;
import org.terasology.rendering.primitives.ChunkMesh;
import org.terasology.rendering.primitives.ChunkTessellator;
import org.terasology.rendering.primitives.NewChunkTessellator;
import org.terasology.rendering.world.WorldRenderer;

import java.util.HashSet;
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
    private static final int MAX_THREADS = Config.getInstance().getMaxThreads();

    /* CHUNK UPDATES */
    private static final Set<NewChunk> _currentlyProcessedChunks = Sets.newHashSet();

    private final NewChunkTessellator tessellator;
    private final WorldProvider worldProvider;

    public ChunkUpdateManager(NewChunkTessellator tessellator, WorldProvider worldProvider) {
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
    public boolean queueChunkUpdate(NewChunk chunk, final UPDATE_TYPE type) {

        if (!_currentlyProcessedChunks.contains(chunk) && (_currentlyProcessedChunks.size() < MAX_THREADS || type != UPDATE_TYPE.DEFAULT)) {
            executeChunkUpdate(chunk);
            return true;
        }

        return false;
    }

    private void executeChunkUpdate(final NewChunk c) {
        _currentlyProcessedChunks.add(c);
        c.setDirty(false);

        // Create a new thread and start processing
        Runnable r = new Runnable() {
            public void run() {
                ChunkMesh[] newMeshes = new ChunkMesh[WorldRenderer.VERTICAL_SEGMENTS];
                for (int seg = 0; seg < WorldRenderer.VERTICAL_SEGMENTS; seg++) {
                    newMeshes[seg] = tessellator.generateMesh(worldProvider, c.getPos(), NewChunk.CHUNK_DIMENSION_Y / WorldRenderer.VERTICAL_SEGMENTS, seg * (NewChunk.CHUNK_DIMENSION_Y / WorldRenderer.VERTICAL_SEGMENTS));
                }

                c.setPendingMesh(newMeshes);
                _currentlyProcessedChunks.remove(c);
            }
        };

        CoreRegistry.get(GameEngine.class).submitTask("Chunk Update", r);
    }

}
