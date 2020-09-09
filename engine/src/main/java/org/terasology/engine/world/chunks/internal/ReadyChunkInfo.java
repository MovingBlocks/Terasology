// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.internal;

import gnu.trove.list.TIntList;
import gnu.trove.map.TShortObjectMap;
import org.terasology.engine.entitySystem.entity.EntityStore;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.math.geom.Vector3i;

import java.util.List;

/**
 *
 */
public class ReadyChunkInfo {
    private final Vector3i pos;
    private final TShortObjectMap<TIntList> blockPositionMapppings;
    private ChunkStore chunkStore;
    private final Chunk chunk;
    private final boolean newChunk;
    private final List<EntityStore> entities;

    public ReadyChunkInfo(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings, List<EntityStore> entities) {
        this.pos = chunk.getPosition();
        this.blockPositionMapppings = blockPositionMapppings;
        this.newChunk = true;
        this.chunk = chunk;
        this.entities = entities;
    }

    public ReadyChunkInfo(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings, ChunkStore chunkStore,
                          List<EntityStore> entities) {
        this.pos = chunk.getPosition();
        this.blockPositionMapppings = blockPositionMapppings;
        this.chunkStore = chunkStore;
        this.newChunk = chunkStore == null;
        this.chunk = chunk;
        this.entities = entities;
    }

    public static ReadyChunkInfo createForNewChunk(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings,
                                                   List<EntityStore> entities) {
        return new ReadyChunkInfo(chunk, blockPositionMapppings, entities);
    }

    public static ReadyChunkInfo createForRestoredChunk(Chunk chunk, TShortObjectMap<TIntList> blockPositionMapppings
            , ChunkStore chunkStore, List<EntityStore> entities) {
        return new ReadyChunkInfo(chunk, blockPositionMapppings, chunkStore, entities);
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
