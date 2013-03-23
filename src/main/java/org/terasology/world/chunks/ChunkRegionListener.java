package org.terasology.world.chunks;

import org.terasology.math.Vector3i;

/**
 * @author Immortius
 */
public interface ChunkRegionListener {

    /**
     * Invoked when a chunk has entered relevance for this chunk region (may be just loaded, or region may have moved
     * to include it)
     *
     * @param pos
     * @param chunk
     */
    public void onChunkRelevant(Vector3i pos, Chunk chunk);

    /**
     * Invoked when a chunk ceases to be relevant for this chunk region (
     *
     * @param pos
     */
    public void onChunkIrrelevant(Vector3i pos);
}
