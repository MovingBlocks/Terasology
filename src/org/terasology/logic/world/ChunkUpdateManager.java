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

import org.terasology.game.Terasology;
import org.terasology.logic.manager.ConfigurationManager;
import org.terasology.model.structures.BlockPosition;

import java.util.HashSet;

/**
 * Provides the mechanism for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager implements IBlockObserver {

    public enum UPDATE_TYPE {
        DEFAULT, PLAYER_TRIGGERED
    }

    /* CONST */
    private static final int MAX_THREADS = (Integer) ConfigurationManager.getInstance().getConfig().get("System.maxThreads");

    /* CHUNK UPDATES */
    private static final HashSet<Chunk> _currentlyProcessedChunks = new HashSet<Chunk>();


    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored. Chunk updates can be forced though.
     *
     * @param chunk The chunk to update
     * @param type  The chunk update type
     * @return True if a chunk update was executed
     */
    public boolean queueChunkUpdate(Chunk chunk, final UPDATE_TYPE type) {

        if (!_currentlyProcessedChunks.contains(chunk) && (_currentlyProcessedChunks.size() < MAX_THREADS || type != UPDATE_TYPE.DEFAULT)) {
            executeChunkUpdate(chunk);
            return true;
        }

        return false;
    }

    private void executeChunkUpdate(final Chunk c) {
        _currentlyProcessedChunks.add(c);

        // Create a new thread and start processing
        Runnable r = new Runnable() {
            public void run() {
                c.processChunk();
                _currentlyProcessedChunks.remove(c);
            }
        };

        Terasology.getInstance().submitTask("Chunk Update", r);
    }

    public void blockPlaced(Chunk chunk, BlockPosition pos) {
        queueChunkUpdate(chunk, UPDATE_TYPE.PLAYER_TRIGGERED);
    }

    public void blockRemoved(Chunk chunk, BlockPosition pos) {
        queueChunkUpdate(chunk, UPDATE_TYPE.PLAYER_TRIGGERED);
    }

}
