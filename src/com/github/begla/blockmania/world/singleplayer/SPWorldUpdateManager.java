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

import com.github.begla.blockmania.world.chunk.Chunk;
import javolution.util.FastList;
import javolution.util.FastSet;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Provides support for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class SPWorldUpdateManager {

    private static final int MAX_THREADS = Math.max(Runtime.getRuntime().availableProcessors() / 2, 1);
    private static final Executor _threadPool = Executors.newFixedThreadPool(MAX_THREADS);
    /* ------ */
    private final PriorityBlockingQueue<Chunk> _vboUpdates = new PriorityBlockingQueue<Chunk>();
    private final FastSet<Chunk> _currentlyProcessedChunks = new FastSet<Chunk>();
    /* ------ */
    private double _averageUpdateDuration = 0.0;
    /* ------ */
    private final SPWorld _parent;

    /**
     * Init. the world update manager.
     *
     * @param _parent The parent world
     */
    public SPWorldUpdateManager(SPWorld _parent) {
        this._parent = _parent;
    }

    public void queueChunkUpdates(FastList<Chunk> visibleChunks) {
        for (FastList.Node<Chunk> n = visibleChunks.head(), end = visibleChunks.tail(); (n = n.getNext()) != end; ) {
            if (n.getValue().isDirty() || n.getValue().isFresh() || n.getValue().isLightDirty())
                queueChunkUpdate(n.getValue());
        }
    }

    public void queueChunkUpdate(Chunk c) {
        final Chunk chunkToProcess = c;

        if (!_currentlyProcessedChunks.contains(chunkToProcess) && _currentlyProcessedChunks.size() < MAX_THREADS) {
            _currentlyProcessedChunks.add(chunkToProcess);

            // ... create a new thread and start processing.
            Runnable r = new Runnable() {
                public void run() {
                    long timeStart = System.currentTimeMillis();

                    processChunkUpdate(chunkToProcess);
                    _currentlyProcessedChunks.remove(chunkToProcess);

                    _averageUpdateDuration += System.currentTimeMillis() - timeStart;
                    _averageUpdateDuration /= 2;
                }
            };

            _threadPool.execute(r);
        }
    }

    /**
     * Processes the given chunk and finally queues it for updating the VBOs.
     *
     * @param c The chunk to process
     */
    private void processChunkUpdate(Chunk c) {
        if (c != null) {
            // If the chunk was changed, update the its VBOs.
            if (c.processChunk())
                _vboUpdates.add(c);
        }
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
