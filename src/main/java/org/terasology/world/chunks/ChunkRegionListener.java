package org.terasology.world.chunks;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public interface ChunkRegionListener {

    /**
     * Invoked when a chunk is ready.
     * Note: This happens off of the main thread
     * @param pos
     * @param chunk
     */
    public void onChunkReady(Vector3i pos, Chunk chunk);
}
