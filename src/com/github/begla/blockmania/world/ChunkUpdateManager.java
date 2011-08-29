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

import com.github.begla.blockmania.Configuration;
import javolution.util.FastList;
import javolution.util.FastSet;

import java.util.Collections;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager {

    private final FastList<Chunk> _displayListUpdates = new FastList<Chunk>(128);
    private FastList<Chunk> _visibleChunks;

    private int _amountGeneratedChunks = 0;
    private double _meanUpdateDuration = 0.0f;
    private final World _parent;

    /**
     * @param _parent
     */
    public ChunkUpdateManager(World _parent) {
        this._parent = _parent;
    }

    /**
     * TODO
     */
    public void updateChunk() {
        if (_visibleChunks == null)
            return;

        FastList<Chunk> visibleChunks = _visibleChunks;
        Collections.sort(visibleChunks);

        long timeStart = System.currentTimeMillis();

        Chunk closestChunk;

        if (!visibleChunks.isEmpty()) {
            closestChunk = visibleChunks.getFirst();

            if (closestChunk != null) {

                // IMPORTANT: Do not touch any chunks which display lists are being generated at the moment!!
                synchronized (this) {
                    if (_displayListUpdates.contains(closestChunk))
                        return;
                }

                processChunkUpdate(closestChunk);
            }
        }

        _meanUpdateDuration += System.currentTimeMillis() - timeStart;
        _meanUpdateDuration /= 2;
    }

    /**
     * TODO
     */
    public void updateDisplayLists() {

        synchronized (this) {
            Collections.sort(_displayListUpdates);
        }

        for (int i = 0; i < Configuration.DL_UPDATES_PER_CYCLE; i++) {

            if (_displayListUpdates.size() > 0) {
                Chunk c = _displayListUpdates.getFirst();

                if (c != null) {
                    // Generate the display list of the center chunk
                    try {
                        c.generateDisplayLists();
                    } catch (Exception e) {
                        // Do nothing
                    }
                }

                synchronized (this) {
                    _displayListUpdates.remove(c);
                }
            }
        }
    }

    /**
     * TODO
     *
     * @param visibleChunks
     */
    public void updateVisibleChunks(FastSet<Chunk> visibleChunks) {
        FastList<Chunk> newVisibleChunks = new FastList<Chunk>(visibleChunks);

        // Remove chunks which need no update
        for (int i = newVisibleChunks.size() - 1; i >= 0; --i) {
            Chunk c = newVisibleChunks.get(i);

            if (!c.isDirty() && !c.isLightDirty() && !c.isFresh()) {
                newVisibleChunks.remove(i);
            }
        }

        _visibleChunks = newVisibleChunks;
    }

    /**
     * TODO
     */
    private void processChunkUpdate(Chunk c) {
        if (c != null) {
            /*
             * Generate the chunk...
             */
            c.generate();

            /*
             * ... and fetch its neighbors...
             */
            Chunk[] neighbors = c.loadOrCreateNeighbors();

            /*
             * Before starting the illumination process, make sure that the neighbor chunks
             * are present and generated.
             */
            for (Chunk neighbor : neighbors) {
                if (neighbor != null) {
                    neighbor.generate();
                }
            }

            /*
             * If the light of this chunk is marked as dirty...
             */
            if (c.isLightDirty()) {
                /*
                 * ... propagate light into adjacent chunks...
                 */
                c.updateLight();
            }

            /*
             * Check if this chunk was changed...
             */
            if (c.isDirty()) {
                /*
                 * ... if yes, regenerate the vertex arrays
                 */
                c.generateMesh();
                synchronized (this) {
                    _displayListUpdates.add(c);
                }
                _amountGeneratedChunks++;
            }
        }
    }

    /**
     * @return
     */
    public int updatesSize() {
        return _visibleChunks.size();
    }

    /**
     * @return
     */
    public int updatesDLSize() {
        return _displayListUpdates.size();
    }

    /**
     * @return
     */
    public double getMeanUpdateDuration() {
        return _meanUpdateDuration;
    }

    /**
     * @return
     */
    public int getAmountGeneratedChunks() {
        return _amountGeneratedChunks;
    }
}
