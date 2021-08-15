// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block;

import gnu.trove.list.TIntList;
import org.terasology.engine.world.BlockEntityRegistry;

/**
 * This event informs of the activation of a group of blocks. It is sent against the BlockTypeEntity of the type of block
 * being activated, with the positions of those blocks.
 *
 */
public class OnActivatedBlocks extends BlockLifecycleEvent {

    public OnActivatedBlocks(TIntList positions, BlockEntityRegistry registry) {
        super(positions, registry);
    }
}
