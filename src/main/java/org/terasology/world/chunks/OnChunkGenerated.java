package org.terasology.world.chunks;

import org.terasology.entitySystem.event.Event;
import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public class OnChunkGenerated implements Event {

    private Vector3i chunkPos = new Vector3i();

    public OnChunkGenerated(Vector3i chunkPos) {
        this.chunkPos.set(chunkPos);
    }

    public Vector3i getChunkPos() {
        return chunkPos;
    }
}
