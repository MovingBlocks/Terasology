// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

/**
 * Interface for Block family that gets updated by a change in a neighbor block.
 */
public interface UpdatesWithNeighboursFamily extends BlockFamily {
    /**
     * Update called when a neighbor block changes
     **/
    Block getBlockForNeighborUpdate(Vector3i location, Block oldBlock);
}
