// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;

/**
 * Interface for Block family that gets updated by a change in a neighbor block.
 */
public interface UpdatesWithNeighboursFamily extends BlockFamily {
    /**
     * Update the block when a neighbor changes
     *
     * @param location the location of the block
     * @param oldBlock the block before the neighbor was updated
     *
     * @return The block from the family to be placed
     */
    Block getBlockForNeighborUpdate(Vector3ic location, Block oldBlock);
}
