package org.terasology.model.blocks.management

import org.terasology.math.Rotation
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

    public Block loadBlock(ConfigObject blockConfig, Rotation rotation) {
        TreeBlock block = new TreeBlock();
        configureBlock(block, blockConfig, rotation);
        return block;
    }

    protected void configureBlock(TreeBlock b, ConfigObject c, Rotation rotation)
    {
        super.configureBlock(b, c, rotation);

        // Now load extra stuff
    }
}
