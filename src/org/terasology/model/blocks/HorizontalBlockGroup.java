package org.terasology.model.blocks;

import org.terasology.math.Side;

import java.util.EnumMap;

/**
 * Block group for blocks that can be oriented around the vertical axis.
 * @author Immortius <immortius@gmail.com>
 */
public class HorizontalBlockGroup implements BlockGroup {

    String _name;
    EnumMap<Side, Block> _blocks = new EnumMap<Side, Block>(Side.class);

    /**
     * @param name The name for the block group.
     * @param blocks The set of blocks that make up the group. Front, Back, Left and Right must be provided - the rest is ignored.
     */
    public HorizontalBlockGroup(String name, EnumMap<Side, Block> blocks)
    {
        _name = name;
        for (Side side : Side.horizontalSides())
        {
            Block block = blocks.get(side);
            if (block == null)
            {
                throw new IllegalArgumentException("Missing block for side: " + side.toString());
            }
            _blocks.put(side, block);
            block.withBlockGroup(this);
        }
    }
    
    public String getTitle() {
        return _name;
    }

    public byte getBlockIdFor(Side attachmentSide, Side direction) {
        return getBlockFor(attachmentSide, direction).getId();        
    }

    public Block getBlockFor(Side attachmentSide, Side direction) {
        if (attachmentSide.isHorizontal())
        {
            return _blocks.get(attachmentSide.reverse());
        }
        return _blocks.get(direction.reverse());

    }

    public Block getArchetypeBlock() {
        return _blocks.get(Side.FRONT);
    }
}
