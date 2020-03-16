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
package org.terasology.persistence.internal;

import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.joml.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.protobuf.EntityData;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkSerializer;

/**
 */
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
