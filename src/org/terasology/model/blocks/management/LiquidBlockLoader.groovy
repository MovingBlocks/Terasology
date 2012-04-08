package org.terasology.model.blocks.management

import org.terasology.math.Rotation
import org.terasology.model.blocks.Block
import org.terasology.model.blocks.LiquidBlock

/**
 * @author Immortius <immortius@gmail.com>
 */
class LiquidBlockLoader extends SimpleBlockLoader {

    public LiquidBlockLoader(Map<String, Integer> imageIndex)
    {
        super(imageIndex);
    }

    public Block loadBlock(ConfigObject blockConfig, Rotation rotation) {
        LiquidBlock block = new LiquidBlock();
        configureBlock(block, blockConfig, rotation);
        return block;
    }

    protected void configureBlock(LiquidBlock b, ConfigObject c, Rotation rotation)
    {
        super.configureBlock(b, c, rotation);

        // Now load extra stuff
    }
}
