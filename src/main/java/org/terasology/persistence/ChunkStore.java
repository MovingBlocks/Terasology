package org.terasology.persistence;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * A chunk store is used to save a chunk and its entity contents.
 * @author Immortius
 */
public interface ChunkStore {

    /**
     * @return The position of the chunk in its world
     */
    public Vector3i getChunkPosition();

    /**
     * @return The chunk itself
     */
    public Chunk getChunk();

    /**
     * Saves the chunk store, deactivating contained entities
     */
    public void save();

    /**
     * Saves the chunk store
     * @param deactivateEntities Whether the contained entities should be deactivated
     */
    public void save(boolean deactivateEntities);

    /**
     * Stores an entity into this chunk store
     * @param entity
     */
    public void store(EntityRef entity);

    /**
     * Restores all the entities stored with this chunk
     */
    public void restoreEntities();

    void storeAllEntities();
}
