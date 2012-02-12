package org.terasology.model.blocks.management

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

    public Block loadBlock(ConfigObject blockConfig) {
        LiquidBlock block = new LiquidBlock();
        configureBlock(block, blockConfig);
        return block;
    }

    protected void configureBlock(LiquidBlock b, ConfigObject c)
    {
        super.configureBlock(b, c);

        // Now load extra stuff
    }
}
