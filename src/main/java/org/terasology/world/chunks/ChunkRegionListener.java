package org.terasology.world.chunks;

import org.terasology.math.Vector3i;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public interface ChunkRegionListener {

    public void onChunkReady(Vector3i pos, Chunk chunk);
}
