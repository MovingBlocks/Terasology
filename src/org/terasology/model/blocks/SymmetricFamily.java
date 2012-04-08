package org.terasology.model.blocks;

import org.terasology.math.Side;

/**
 * The standard block group consisting of a single symmetrical block that doesn't need rotations
 * @author Immortius <immortius@gmail.com>
 */
public class SymmetricFamily implements BlockFamily {
    
    Block block;
    
    public SymmetricFamily(Block block)
    {
        this.block = block;
        block.withBlockFamily(this);
    }
    
    public String getTitle() {
        return block.getTitle();
    }

    public byte getBlockIdFor(Side attachmentSide, Side direction) {
        return block.getId();
    }

    public Block getBlockFor(Side attachmentSide, Side direction) {
        return block;
    }

    public Block getArchetypeBlock() {
        return block;
    }
}
