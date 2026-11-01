// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks;

import org.joml.Vector3ic;
import org.terasology.context.annotation.API;
import org.terasology.engine.world.block.Block;

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
    Vector3ic getBlockPos();
}
