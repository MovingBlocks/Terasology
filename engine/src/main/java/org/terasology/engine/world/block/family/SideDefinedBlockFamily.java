// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.family;

import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;

public interface SideDefinedBlockFamily extends BlockFamily {
    /**
     * Returns a block that is facing the specified side from this block family, if one exists.
     *
     * @param side
     * @return
     */
    Block getBlockForSide(Side side);

    /**
     * Returns a side the specified block from this block family is facing.
     *
     * @param block
     * @return
     */
    Side getSide(Block block);
}
