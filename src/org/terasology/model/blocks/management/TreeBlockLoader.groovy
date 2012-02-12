package org.terasology.model.blocks.management

import org.terasology.model.blocks.Block
import org.terasology.model.blocks.TreeBlock

/**
 * @author Immortius <immortius@gmail.com>
 */
class TreeBlockLoader extends SimpleBlockLoader {

    public TreeBlockLoader(Map<String, Integer> imageIndex)
    {
        super(imageIndex);
    }

    public Block loadBlock(ConfigObject blockConfig) {
        TreeBlock block = new TreeBlock();
        configureBlock(block, blockConfig);
        return block;
    }

    protected void configureBlock(TreeBlock b, ConfigObject c)
    {
        super.configureBlock(b, c);

        // Now load extra stuff
    }
}
