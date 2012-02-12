package org.terasology.model.blocks

/**
 * @author Immortius <immortius@gmail.com>
 */
class PlantBlockLoader extends SimpleBlockLoader {

    public PlantBlockLoader(Map<String, Integer> imageIndex)
    {
        super(imageIndex);
    }

    public Block loadBlock(ConfigObject blockConfig) {
        PlantBlock block = new PlantBlock();
        configureBlock(block, blockConfig);
        return block;
    }

    protected void configureBlock(PlantBlock b, ConfigObject c)
    {
        super.configureBlock(b, c);

        // Now load plant stuff
    }
}
