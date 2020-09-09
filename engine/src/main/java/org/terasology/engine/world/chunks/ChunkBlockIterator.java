// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.module.sandbox.API;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
@API
public interface ChunkBlockIterator {

    /**
     * Updates the iterator to the next block
     *
     * @return True if a new block was found,
     */
    boolean next();

    /**
     * @return the current block
     */
    Block getBlock();

    /**
     * @return The world coords of the current block
     */
    Vector3i getBlockPos();
}
