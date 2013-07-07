package org.terasology.persistence.internal;

import org.terasology.math.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public class ChunkStoreInternal implements ChunkStore {

    private StorageManagerInternal storageManager;
    private Vector3i chunkPosition;
    private Chunk chunk;

    public ChunkStoreInternal(Chunk chunk, StorageManagerInternal storageManager) {
        this.chunk = chunk;
        this.chunkPosition = new Vector3i(chunk.getPos());
        this.storageManager = storageManager;
    }

    @Override
    public Vector3i getChunkPosition() {
        return new Vector3i(chunkPosition);
    }

    @Override
    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void save() {
        storageManager.store(this);
    }
}
