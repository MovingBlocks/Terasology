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
package com.github.begla.blockmania.world;

import com.github.begla.blockmania.world.chunk.Chunk;
import javolution.util.FastList;
import javolution.util.FastSet;

import java.util.Collections;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Provides support for updating and generating chunks.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class WorldUpdateManager {

    private final PriorityBlockingQueue<Chunk> _vboUpdates = new PriorityBlockingQueue<Chunk>();
    private final FastSet<Chunk> _currentlyProcessedChunks = new FastSet<Chunk>();
    private int _threadCount = 0;

    private double _meanUpdateDuration = 0.0;
    private final World _parent;

    private int _chunkUpdateAmount;

    /**
     * @param _parent
     */
    public WorldUpdateManager(World _parent) {
        this._parent = _parent;
    }

    public void processChunkUpdates() {
        long timeStart = System.currentTimeMillis();

        final FastList<Chunk> dirtyChunks = new FastList<Chunk>(_parent.getVisibleChunks());

        for (int i = dirtyChunks.size() - 1; i >= 0; i--) {
            Chunk c = dirtyChunks.get(i);

            if (c == null) {
                dirtyChunks.remove(i);
                continue;
            }

            if (!(c.isDirty() || c.isFresh() || c.isLightDirty()) || !_parent.getPlayer().getViewFrustum().intersects(c.getAABB())) {
                dirtyChunks.remove(i);
            }
        }

        if (dirtyChunks.isEmpty()) {
            return;
        }

        Collections.sort(dirtyChunks);

        final Chunk chunkToProcess = dirtyChunks.getFirst();

        if (!_currentlyProcessedChunks.contains(chunkToProcess)) {

            _currentlyProcessedChunks.add(chunkToProcess);

            Thread t = new Thread() {
                @Override
                public void run() {
                    while (_threadCount > Math.max(Runtime.getRuntime().availableProcessors()/2, 1)) {
                        synchronized (_currentlyProcessedChunks) {
                            try {
                                _currentlyProcessedChunks.wait();
                            } catch (InterruptedException e) {
                            }
                        }
                    }

                    synchronized (_currentlyProcessedChunks) {
                        _threadCount++;
                    }

                    processChunkUpdate(chunkToProcess);
                    _currentlyProcessedChunks.remove(chunkToProcess);

                    synchronized (_currentlyProcessedChunks) {
                        _threadCount--;
                    }

                    synchronized (_currentlyProcessedChunks) {
                        _currentlyProcessedChunks.notify();
                    }
                }
            };

            t.start();
        }

        _chunkUpdateAmount = dirtyChunks.size();
        _meanUpdateDuration += System.currentTimeMillis() - timeStart;
        _meanUpdateDuration /= 2;
    }

    private void processChunkUpdate(Chunk c) {
        if (c != null) {
            if (c.processChunk())
                _vboUpdates.add(c);
        }
    }

    public void updateVBOs() {
        while (_vboUpdates.size() > 0) {
            Chunk c = _vboUpdates.poll();

            if (c != null)
                c.generateVBOs();
        }
    }

    public int getUpdatesSize() {
        return _chunkUpdateAmount;
    }

    public int getVboUpdatesSize() {
        return _vboUpdates.size();
    }

    public double getMeanUpdateDuration() {
        return _meanUpdateDuration;
    }

    public int getThreadCount() {
        return _threadCount;
    }
}
