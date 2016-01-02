/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Provides an easy to get a compressed version of a chunk. Either the chunk most have a snapshot of it's state
 * or it must be an unloaded chunk which no longer changes.
 *
 */
public class CompressedChunkBuilder {
    private EntityData.EntityStore entityStore;
    private ChunkImpl chunk;
    private boolean viaSnapshot;
    private byte[] result;
    private Set<EntityRef> storedEntities;

    /**
     *
     * @param entitiesToSave all persistent entities within the given chunk
     * @param chunkUnloaded if true the chunk data will be used directly.  If deactivate is false then the chunk will be
     *                      but in snapshot mode so that concurrent modifications (and possibly future unload) is
     *                      possible.
     */
    public CompressedChunkBuilder(EngineEntityManager entityManager, ChunkImpl chunk,
                                  Collection<EntityRef> entitiesToSave,
                                  boolean chunkUnloaded) {
        EntityStorer storer = new EntityStorer(entityManager);
        entitiesToSave.stream().filter(EntityRef::isPersistent).forEach(storer::store);
        storedEntities = storer.getStoredEntities();
        this.entityStore = storer.finaliseStore();

        this.chunk = chunk;
        this.viaSnapshot = !chunkUnloaded;
        if (viaSnapshot) {
            this.chunk.createSnapshot();
        }
    }

    /**
     *
     * @param entityStore encoded entities to be stored.
     * @param chunk       chunk for which {@link ChunkImpl#createSnapshot()} has been called.
     * @param viaSnapshot specifies if the previously taken snapshot will be encoded or if
     */
    public CompressedChunkBuilder(EntityData.EntityStore entityStore, ChunkImpl chunk, boolean viaSnapshot) {
        this.entityStore = entityStore;
        this.chunk = chunk;
        this.viaSnapshot = viaSnapshot;
    }

    public synchronized byte[] buildEncodedChunk() {
        if (result == null) {

            EntityData.ChunkStore.Builder encoded;
            if (viaSnapshot) {
                encoded = chunk.encodeAndReleaseSnapshot();
            } else {
                encoded = chunk.encode();
            }
            encoded.setStore(entityStore);
            EntityData.ChunkStore store = encoded.build();
            result = compressChunkStore(store);
        }
        return result;
    }

    private byte[] compressChunkStore(EntityData.ChunkStore store) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            store.writeTo(gzipOut);
        } catch (IOException e) {
            // as no real IO is involved this should not happen
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    public Set<EntityRef> getStoredEntities() {
        return storedEntities;
    }
}
