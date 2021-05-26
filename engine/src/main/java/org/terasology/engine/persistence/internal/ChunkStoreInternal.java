// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.joml.Vector3i;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.protobuf.EntityData;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkSerializer;

final class ChunkStoreInternal implements ChunkStore {

    private Vector3i chunkPosition;
    private Chunk chunk;

    private EngineEntityManager entityManager;
    private EntityData.EntityStore entityStore;

    ChunkStoreInternal(EntityData.ChunkStore chunkData, EngineEntityManager entityManager,
                       BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        this.chunkPosition = new Vector3i(chunkData.getX(), chunkData.getY(), chunkData.getZ());
        this.entityManager = entityManager;

        this.chunk = ChunkSerializer.decode(chunkData, blockManager, extraDataManager);
        this.entityStore = chunkData.getStore();
    }

    @Override
    public Vector3i getChunkPosition() {
        return new Vector3i(chunkPosition);
    }

    @Override
    public Chunk getChunk() {
        chunk.prepareForReactivation();
        return chunk;
    }

    @Override
    public void restoreEntities() {
        new EntityRestorer(entityManager).restore(entityStore);
    }
}
