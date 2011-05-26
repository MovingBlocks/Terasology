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
 * 
 */
package com.github.begla.blockmania.world;

import gnu.trove.list.TLinkable;

/**
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class ChunkUpdate implements Comparable<ChunkUpdate>, TLinkable<ChunkUpdate> {

    private ChunkUpdate _next, _prev;
    private boolean _updateNeighbors;
    private Chunk _chunk;
    private byte _priority;

    /**
     * 
     * @param _updateNeighbors
     * @param _chunk
     * @param priority  
     */
    public ChunkUpdate(boolean _updateNeighbors, Chunk _chunk, byte priority) {
        this._updateNeighbors = _updateNeighbors;
        this._chunk = _chunk;

        if (priority <= 0) {
            priority = 1;
        }

        this._priority = priority;
    }

    /**
     * 
     * @param _updateNeighbors
     * @param _chunk 
     */
    public ChunkUpdate(boolean _updateNeighbors, Chunk _chunk) {
        this._updateNeighbors = _updateNeighbors;
        this._chunk = _chunk;
        this._priority = 1;
    }

    /**
     * 
     * @return
     */
    public boolean isUpdateNeighbors() {
        return _updateNeighbors;
    }

    /**
     * 
     * @return
     */
    public Chunk getChunk() {
        return _chunk;
    }

    /**
     * 
     * @return 
     */
    public byte getPriority() {
        return _priority;
    }

    /**
     * 
     * @return 
     */
    public double getWeight() {
        double weight = _chunk.calcDistanceToPlayer() / (double) _priority;
        return weight;
    }

    /**
     * 
     * @param o
     * @return
     */
    @Override
    public int compareTo(ChunkUpdate o) {
        return new Double(getWeight()).compareTo(o.getWeight());
    }

    /**
     * 
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (o.getClass() == ChunkUpdate.class) {
            ChunkUpdate cu = (ChunkUpdate) o;
            cu.getChunk().equals(_chunk);
        }
        return false;
    }

    /**
     * 
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this._chunk != null ? this._chunk.hashCode() : 0);
        return hash;
    }

    @Override
    public ChunkUpdate getNext() {
        return _next;
    }

    @Override
    public ChunkUpdate getPrevious() {
        return _prev;
    }

    @Override
    public void setNext(ChunkUpdate t) {
        _next = t;
    }

    @Override
    public void setPrevious(ChunkUpdate t) {
        _prev = t;
    }
}
