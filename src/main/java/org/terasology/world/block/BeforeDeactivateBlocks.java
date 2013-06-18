package org.terasology.world.block;

import gnu.trove.list.TIntList;
import org.terasology.world.BlockEntityRegistry;

/**
 * This event informs of the pending deactivation of a group of blocks. It is sent against the BlockTypeEntity for
 * a type of block, with the positions of thoe blocks being deactivated.
 * @author Immortius
 */
public class BeforeDeactivateBlocks extends BlockLifecycleEvent {

    public BeforeDeactivateBlocks(TIntList positions, BlockEntityRegistry registry) {
        super(positions, registry);
    }
}
