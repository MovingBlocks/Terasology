package org.terasology.world.chunks;

import org.terasology.entitySystem.AbstractEvent;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class ChunkUnloadedEvent extends AbstractEvent {

    private Vector3i chunkPos = new Vector3i();

    public ChunkUnloadedEvent(Vector3i chunkPos) {
        this.chunkPos.set(chunkPos);
    }

    public Vector3i getChunkPos() {
        return chunkPos;
    }
}
