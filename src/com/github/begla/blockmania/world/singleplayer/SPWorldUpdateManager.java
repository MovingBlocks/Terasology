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
package com.github.begla.blockmania.world.singleplayer;

import com.github.begla.blockmania.main.Blockmania;
import com.github.begla.blockmania.world.chunk.Chunk;
import javolution.util.FastSet;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Provides support for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class SPWorldUpdateManager {

    private static final int MAX_THREADS = Math.max(Runtime.getRuntime().availableProcessors() - 2, 1);
    private static final ExecutorService _threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    private static final FastSet<Chunk> _currentlyProcessedChunks = new FastSet<Chunk>();
    /* ------ */
    private final PriorityBlockingQueue<Chunk> _vboUpdates = new PriorityBlockingQueue<Chunk>();
    /* ------ */
    private double _averageUpdateDuration = 0.0;

    public boolean queueChunkUpdate(Chunk c) {
        final Chunk chunkToProcess = c;

        if (!_currentlyProcessedChunks.contains(chunkToProcess) && _currentlyProcessedChunks.size() < MAX_THREADS) {
            _currentlyProcessedChunks.add(chunkToProcess);

            // ... create a new thread and start processing.
            Runnable r = new Runnable() {
                public void run() {
                    long timeStart = Blockmania.getInstance().getTime();

                    processChunkUpdate(chunkToProcess);
                    _currentlyProcessedChunks.remove(chunkToProcess);

                    _averageUpdateDuration += Blockmania.getInstance().getTime() - timeStart;
                    _averageUpdateDuration /= 2;
                }
            };

            _threadPool.execute(r);
            return true;
        }

        return false;
    }

    /**
     * Processes the given chunk and finally queues it for updating the VBOs.
     *
     * @param c The chunk to process
     */
    private void processChunkUpdate(Chunk c) {
        // If the chunk was changed, update the VBOs.
        if (c.processChunk())
            _vboUpdates.add(c);
    }

    /**
     * Updates the VBOs of all currently queued chunks.
     */
    public void updateVBOs() {
        while (_vboUpdates.size() > 0) {
            Chunk c = _vboUpdates.poll();

            if (c != null)
                c.generateVBOs();
        }
    }

    public int getVboUpdatesSize() {
        return _vboUpdates.size();
    }

    public double getAverageUpdateDuration() {
        return _averageUpdateDuration;
    }
}
