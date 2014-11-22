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

import com.google.common.collect.Lists;
import gnu.trove.set.TIntSet;
import gnu.trove.set.TLongSet;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.persistence.ChunkStore;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.ChunkSerializer;

import java.util.List;

/**
 * @author Immortius
 */
final class ChunkStoreInternal implements ChunkStore {

    private StorageManagerInternal storageManager;
    private Vector3i chunkPosition;
    private Chunk chunk;

    private EngineEntityManager entityManager;
    private EntityData.EntityStore entityStore;
    private TLongSet externalRefs;

    public ChunkStoreInternal(Chunk chunk, StorageManagerInternal storageManager, EngineEntityManager entityManager) {
        this.chunk = chunk;
        this.chunkPosition = new Vector3i(chunk.getPosition());
        this.storageManager = storageManager;
        this.entityManager = entityManager;
    }

    public ChunkStoreInternal(EntityData.ChunkStore chunkData, TLongSet externalRefs, StorageManagerInternal storageManager, EngineEntityManager entityManager) {
        this.chunkPosition = new Vector3i(chunkData.getX(), chunkData.getY(), chunkData.getZ());
        this.storageManager = storageManager;
        this.entityManager = entityManager;

        this.chunk = ChunkSerializer.decode(chunkData);
        this.entityStore = chunkData.getStore();
        this.externalRefs = externalRefs;
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
        new EntityRestorer(entityManager).restore(entityStore, externalRefs);
    }
}
