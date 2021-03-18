// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import gnu.trove.list.TIntList;
import org.terasology.engine.world.BlockEntityRegistry;

/**
 * This event informs of the addition of new blocks. It is sent against a block type entity of the blocks involved,
 * with the positions of those blocks.
 *
 */
public class OnAddedBlocks extends BlockLifecycleEvent {

    public OnAddedBlocks(TIntList positions, BlockEntityRegistry registry) {
        super(positions, registry);
    }
}
