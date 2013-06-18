package org.terasology.world.block;

import gnu.trove.list.TIntList;
import org.terasology.world.BlockEntityRegistry;

/**
 * This event informs of the addition of new blocks. It is sent against a block type entity of the blocks involved,
 * with the positions of those blocks.
 * @author Immortius
 */
public class OnAddedBlocks extends BlockLifecycleEvent {

    public OnAddedBlocks(TIntList positions, BlockEntityRegistry registry) {
        super(positions, registry);
    }
}
