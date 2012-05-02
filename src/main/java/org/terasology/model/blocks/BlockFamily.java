package org.terasology.model.blocks;

import org.terasology.math.Side;

/**
 * A collection of blocks that are all different rotations of the same core block.
 * This will enable such effects as players picking up a block with one orientation and it grouping
 * with the same block with different orientations, and placing it in different directions.
 * @author Immortius <immortius@gmail.com>
 */
public interface BlockFamily {

    /**
     * @return The name of the group
     */
    String getTitle();

    /**
     * Get the block id that is appropriate for placement in the given situation
     * @param attachmentSide The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param direction A secondary direction after the attachment side that determines the facing of the block.
     * @return The id of the appropriate block
     */
    byte getBlockIdFor(Side attachmentSide, Side direction);

    /**
     * Get the block that is appropriate for placement in the given situation
     * @param attachmentSide The side of the block which this block is being attached to, e.g. Top if the block is being placed on the ground
     * @param direction A secondary direction after the attachment side that determines the facing of the block.
     * @return The appropriate block
     */
    Block getBlockFor(Side attachmentSide, Side direction);

    /**
     * @return The base block defining the block group. Can be used for orientation-irrelevant behaviours
     */
    Block getArchetypeBlock();
}
