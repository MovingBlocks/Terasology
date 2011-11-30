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
package com.github.begla.blockmania.world.chunk;

import com.github.begla.blockmania.configuration.ConfigurationManager;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.game.Blockmania;
import com.github.begla.blockmania.world.interfaces.BlockObserver;

import java.util.HashSet;

/**
 * Provides the mechanism for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager implements BlockObserver {

    public enum UPDATE_TYPE {
        DEFAULT, PLAYER_PLACED, PLAYER_REMOVED
    }

    /* CONST */
    private static final int MAX_THREADS = (Integer) ConfigurationManager.getInstance().getConfig().get("System.maxThreads");

    /* CHUNK UPDATES */
    private static final HashSet<Chunk> _currentlyProcessedChunks = new HashSet<Chunk>();

    /* STATISTICS */
    private double _averageUpdateDuration = 0.0;

    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored. Chunk updates can be forced though.
     *
     * @param chunk The chunk to update
     * @return True if a chunk update was executed
     */
    public boolean queueChunkUpdate(Chunk chunk, final UPDATE_TYPE type) {
        final Chunk chunkToProcess = chunk;

        if (!_currentlyProcessedChunks.contains(chunkToProcess) && (_currentlyProcessedChunks.size() < MAX_THREADS || type != UPDATE_TYPE.DEFAULT)) {
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
                long timeStart = Blockmania.getInstance().getTime();
                c.processChunk();

                _currentlyProcessedChunks.remove(c);

                _averageUpdateDuration += Blockmania.getInstance().getTime() - timeStart;
                _averageUpdateDuration /= 2;
            }
        };

        Blockmania.getInstance().getThreadPool().execute(r);
    }

    public double getAverageUpdateDuration() {
        return _averageUpdateDuration;
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {

    }

    public void blockPlaced(Chunk chunk, BlockPosition pos) {
        queueChunkUpdate(chunk, UPDATE_TYPE.PLAYER_PLACED);
    }

    public void blockRemoved(Chunk chunk, BlockPosition pos) {
        queueChunkUpdate(chunk, UPDATE_TYPE.PLAYER_REMOVED);
    }

}
