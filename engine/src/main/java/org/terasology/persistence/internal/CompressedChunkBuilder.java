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

import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * Provides an easy to get a compressed version of a chunk from a chunk snapshot taken previously.
 * @author Florian <florian@fkoeberle.de>
 */
public class CompressedChunkBuilder {
    private EntityData.EntityStore entityStore;
    private ChunkImpl chunkWithSnapshot;

    /**
     *
     * @param entityStore encoded entities to be stored.
     * @param chunkWithSnapshot chunk for which {@link ChunkImpl#createSnapshot()} has been called.
     */
    public CompressedChunkBuilder(EntityData.EntityStore entityStore, ChunkImpl chunkWithSnapshot) {
        this.entityStore = entityStore;
        this.chunkWithSnapshot = chunkWithSnapshot;
    }

    public byte[] buildEncodedChunk() {
        EntityData.ChunkStore.Builder encoded = chunkWithSnapshot.encodeAndReleaseSnapshot();
        encoded.setStore(entityStore);
        EntityData.ChunkStore store = encoded.build();
        return compressChunkStore(store);
    }

    private byte[] compressChunkStore(EntityData.ChunkStore store) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            store.writeTo(gzipOut);
        } catch(IOException e) {
            // as no real IO is involved this should not happen
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }
}
