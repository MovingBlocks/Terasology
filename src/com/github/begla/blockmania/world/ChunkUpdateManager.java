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
import java.util.Collection;
import java.util.Collections;
import javolution.util.FastList;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public final class ChunkUpdateManager {

    private final FastList<ChunkUpdate> _displayListUpdates = new FastList<ChunkUpdate>(128);
    private final FastList<ChunkUpdate> _chunkUpdates = new FastList<ChunkUpdate>(1024);
    private int _amountGeneratedChunks = 0;
    private double _meanUpdateDuration = 0.0f;
    private World _parent;

    /**
     * 
     * @param _parent
     */
    public ChunkUpdateManager(World _parent) {
        this._parent = _parent;
    }

    /**
     * 
     */
    public void updateChunk() {
        long timeStart = System.currentTimeMillis();
        timeStart = System.currentTimeMillis();

        ChunkUpdate closestChunkUpdate = null;
        removeInvisibleChunkUpdates();
        
        if (!_chunkUpdates.isEmpty()) {
            closestChunkUpdate = _chunkUpdates.removeFirst();

            if (!_parent.isChunkVisible(closestChunkUpdate.getChunk())) {
                return;
            }

            processChunkUpdate(closestChunkUpdate);
        }

        _meanUpdateDuration += System.currentTimeMillis() - timeStart;
        _meanUpdateDuration /= 2;
    }

    /**
     * 
     */
    public void updateDisplayLists() {
        for (int i = 0; i < Configuration.DL_UPDATES_PER_CYCLE; i++) {
            // Take one chunk from the queue
            try {
                ChunkUpdate cu = _displayListUpdates.getFirst();

                if (cu != null) {
                    // Generate the display list of the center chunk
                    cu.getChunk().generateDisplayLists();

                    if (cu.isUpdateNeighbors()) {
                        Chunk[] neighbors = cu.getChunk().loadOrCreateNeighbors();

                        // Generate the display lists of the neighbor chunks
                        for (Chunk n : neighbors) {
                            if (n != null) {
                                n.generateDisplayLists();
                            }
                        }
                    }

                    // Remove the center chunk
                    _displayListUpdates.remove(cu);
                }
            } catch (Exception e) {
            }

        }
    }

    /**
     * 
     * @param c
     * @param updateNeighbors
     * @param markDirty
     * @param forceInvisibleChunks 
     */
    public void queueChunkForUpdate(Chunk c, boolean updateNeighbors, boolean markDirty, boolean forceInvisibleChunks) {
        if (markDirty) {
            c.setDirty(true);
        }

        // Do not queue clean chunks
        if (c.isDirty() && (_parent.isChunkVisible(c) || forceInvisibleChunks)) {
            ChunkUpdate cu = new ChunkUpdate(updateNeighbors, c);

            if (!_chunkUpdates.contains(cu)) {
                _chunkUpdates.add(cu);

                Collections.sort(_chunkUpdates);
            }
        }
    }

    /**
     * TODO
     *
     * @param c The chunk to process
     */
    private void processChunkUpdate(ChunkUpdate cu) {
        if (cu != null) {
            boolean lightCalculated = false;

            /*
             * Generate the chunk...
             */
            cu.getChunk().generate();

            /*
             * ... and fetch its neighbors...
             */
            Chunk[] neighbors = cu.getChunk().loadOrCreateNeighbors();


            /*
             * Before starting the illumination process, make sure that the neighbor chunks
             * are present and generated.
             */
            for (int i = 0; i < neighbors.length; i++) {
                if (neighbors[i] != null) {
                    neighbors[i].generate();
                }
            }

            /*
             * If the light of this chunk is marked as dirty...
             */
            if (cu.getChunk().isLightDirty()) {
                /*
                 * ... propagate light into adjacent chunks...
                 */
                cu.getChunk().updateLight();
                lightCalculated = true;
            }

            for (int i = 0; i < neighbors.length; i++) {
                if (neighbors[i] != null) {
                    if (neighbors[i].isDirty() && cu.isUpdateNeighbors()) {
                        if (neighbors[i].isLightDirty()) {
                            queueChunkForUpdate(neighbors[i], false, false, false);
                        } else {
                            neighbors[i].generateVertexArrays();
                        }
                    } else if (lightCalculated) {
                        queueChunkForUpdate(neighbors[i], false, false, false);
                    }
                }
            }


            /*
             * Check if this chunk was changed...
             */
            if (cu.getChunk().isDirty()) {
                /*
                 * ... if yes, regenerate the vertex arrays
                 */
                cu.getChunk().generateVertexArrays();
                _displayListUpdates.add(cu);
                _amountGeneratedChunks++;
            }
        }
    }

    /**
     * 
     * @return
     */
    public int updatesSize() {
        return _chunkUpdates.size();
    }

    /**
     * 
     * @return
     */
    public int updatesDLSize() {
        return _displayListUpdates.size();
    }

    /**
     * 
     * @param updates
     */
    public void removeChunkUpdates(Collection<ChunkUpdate> updates) {
        _chunkUpdates.removeAll(updates);
    }

    public void removeInvisibleChunkUpdates() {
        FastList<ChunkUpdate> updatesToDelete = new FastList<ChunkUpdate>();

        for (FastList.Node<ChunkUpdate> n = _chunkUpdates.tail(), end = _chunkUpdates.head(); (n = n.getPrevious()) != end;) {

            Chunk c = n.getValue().getChunk();

            if (c != null) {
                if (!_parent.isChunkVisible(c)) {
                    updatesToDelete.add(n.getValue());
                } else {
                    break;
                }
            }
        }

        removeChunkUpdates(updatesToDelete);
    }

    /**
     * 
     * @return
     */
    public double getMeanUpdateDuration() {
        return _meanUpdateDuration;
    }

    /**
     * 
     * @return
     */
    public int getAmountGeneratedChunks() {
        return _amountGeneratedChunks;
    }
}
