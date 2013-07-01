package org.terasology.world.block.family;

import org.terasology.math.Side;
import org.terasology.world.block.Block;

public interface SideDefinedBlockFamily extends BlockFamily {
    public Block getBlockForSide(Side side);
    public Side getBlockSide(Block block);
}
