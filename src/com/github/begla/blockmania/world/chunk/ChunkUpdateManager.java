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

import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.main.BlockmaniaConfiguration;
import com.github.begla.blockmania.world.observer.BlockObserver;
import javolution.util.FastSet;

/**
 * (byte) 0
 * Provides support for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager implements BlockObserver {

    /* CHUNK UPDATES */
    private static final FastSet<Chunk> _currentlyProcessedChunks = new FastSet<Chunk>();
    private double _averageUpdateDuration = 0.0;


    /**
     * Updates the given chunk using a new thread from the thread pool. If the maximum amount of chunk updates
     * is reached, the chunk update is ignored.
     *
     * @param c The chunk to update
     * @return True if a chunk update was executed
     */
    public boolean queueChunkUpdate(Chunk c, boolean force) {
        final Chunk chunkToProcess = c;
        final int maxThreads = (Integer) BlockmaniaConfiguration.getInstance().getConfig().get("System.maxThreads");

        if (!_currentlyProcessedChunks.contains(chunkToProcess) && (_currentlyProcessedChunks.size() < maxThreads || force)) {
            _currentlyProcessedChunks.add(chunkToProcess);

            // ... create a new thread and start processing.
            Runnable r = new Runnable() {
                public void run() {
                    long timeStart = Blockmania.getInstance().getTime();

                    chunkToProcess.processChunk();

                    _currentlyProcessedChunks.remove(chunkToProcess);

                    _averageUpdateDuration += Blockmania.getInstance().getTime() - timeStart;
                    _averageUpdateDuration /= 2;
                }
            };

            Blockmania.getInstance().getThreadPool().execute(r);
            return true;
        }

        return false;
    }

    public double getAverageUpdateDuration() {
        return _averageUpdateDuration;
    }

    public void lightChanged(Chunk chunk, BlockPosition pos) {

    }

    public void blockChanged(Chunk chunk, BlockPosition pos) {
        queueChunkUpdate(chunk, true);
    }
}
