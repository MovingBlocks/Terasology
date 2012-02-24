package org.terasology.model.blocks.management

import org.terasology.model.blocks.Block
import org.terasology.model.blocks.PlantBlock
import org.terasology.math.Rotation

/**
 * @author Immortius <immortius@gmail.com>
 */
class PlantBlockLoader extends SimpleBlockLoader {

    public PlantBlockLoader(Map<String, Integer> imageIndex)
    {
        super(imageIndex);
    }

    public Block loadBlock(ConfigObject blockConfig, Rotation rotation) {
        PlantBlock block = new PlantBlock();
        configureBlock(block, blockConfig, rotation);
        return block;
    }

    protected void configureBlock(PlantBlock b, ConfigObject c, Rotation rotation)
    {
        super.configureBlock(b, c, rotation);

        // Now load plant stuff
    }
}
