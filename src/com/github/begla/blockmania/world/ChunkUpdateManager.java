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

import javolution.util.FastList;

import java.util.Collections;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager {

    private final FastList<Chunk> _displayListUpdates = new FastList<Chunk>(128);

    private double _meanUpdateDuration = 0.0f;
    private final World _parent;

    private int _chunkUpdateAmount;

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
        FastList<Chunk> dirtyChunks = new FastList<Chunk>(_parent.getVisibleChunks());

        for (int i = dirtyChunks.size() - 1; i >= 0; i--) {
            Chunk c = dirtyChunks.get(i);

            if (!(c.isDirty() || c.isFresh() || c.isLightDirty())) {
                dirtyChunks.remove(i);
            }
        }

        Collections.sort(dirtyChunks);
        _chunkUpdateAmount = dirtyChunks.size();
        long timeStart = System.currentTimeMillis();


        if (dirtyChunks.size() > 0) {
            Chunk closestChunk = dirtyChunks.removeFirst();
            processChunkUpdate(closestChunk);
        }

        _meanUpdateDuration += System.currentTimeMillis() - timeStart;
        _meanUpdateDuration /= 2;
    }

    /**
     * TODO
     */
    public void updateDisplayLists() {
        if (!_displayListUpdates.isEmpty()) {
            Chunk c = _displayListUpdates.removeFirst();
            c.generateVBOs();
        }
    }

    /**
     * TODO
     */
    private void processChunkUpdate(Chunk c) {
        if (c != null) {
            if (!c.isDirty() && !c.isFresh() && !c.isLightDirty()) {
                return;
            }

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
                _displayListUpdates.add(c);
            }
        }
    }

    /**
     * @return
     */
    public int updatesSize() {
        return _chunkUpdateAmount;
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
}
