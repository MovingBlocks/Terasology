package org.terasology.world.generator.chunkGenerators;

import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

public class SeedTreeGenerator extends TreeGenerator {
    private Block block;

    public SeedTreeGenerator setBlock(Block block) {
        this.block = block;
        return this;
    }

    @Override
    public void generate(ChunkView view, FastRandom rand, int posX, int posY, int posZ) {
        view.setBlock(posX, posY, posZ, block);
    }
}