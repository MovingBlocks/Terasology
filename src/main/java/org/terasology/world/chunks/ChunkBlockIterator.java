package org.terasology.world.chunks;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public interface ChunkBlockIterator {

    /**
     * Updates the iterator to the next block
     * @return True if a new block was found,
     */
    public boolean next();

    /**
     *
     * @return the current block
     */
    public Block getBlock();

    /**
     *
     * @return The world coords of the current block
     */
    public Vector3i getBlockPos();
}
