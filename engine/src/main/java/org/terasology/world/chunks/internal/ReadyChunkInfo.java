/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.chunks.internal;

import java.util.List;

import gnu.trove.list.TIntList;
import gnu.trove.map.TShortObjectMap;

import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.math.geom.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.world.chunks.Chunk;

/**
 */
public class ReadyChunkInfo {
    private Vector3i pos;
    private TShortObjectMap<TIntList> blockPositionMapppings;
    private ChunkStore chunkStore;
    private Chunk chunk;
    private boolean newChunk;
    private List<EntityStore> entities;

    public ReadyChunkInfo(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings, List<EntityStore> entities) {
        this.pos = chunk.getPosition();
        this.blockPositionMapppings = blockPositionMapppings;
        this.newChunk = true;
        this.chunk = chunk;
        this.entities = entities;
    }

    public ReadyChunkInfo(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings, ChunkStore chunkStore, List<EntityStore> entities) {
        this.pos = chunk.getPosition();
        this.blockPositionMapppings = blockPositionMapppings;
        this.chunkStore = chunkStore;
        this.newChunk = chunkStore == null;
        this.chunk = chunk;
        this.entities = entities;
    }

    public List<EntityStore> getEntities() {
        return entities;
    }

    public Vector3i getPos() {
        return pos;
    }

    public TShortObjectMap<TIntList> getBlockPositionMapppings() {
        return blockPositionMapppings;
    }

    public ChunkStore getChunkStore() {
        return chunkStore;
    }

    public boolean isNewChunk() {
        return newChunk;
    }

    public Chunk getChunk() {
        return chunk;
    }
}
