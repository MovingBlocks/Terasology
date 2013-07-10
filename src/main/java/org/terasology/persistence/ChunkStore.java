package org.terasology.persistence;

import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public interface ChunkStore {

    public Vector3i getChunkPosition();

    public Chunk getChunk();

    public void save();

    public void store(EntityRef entity);

    public void restoreEntities();
}
