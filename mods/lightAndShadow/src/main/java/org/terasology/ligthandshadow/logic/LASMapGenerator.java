package org.terasology.ligthandshadow.logic;

import org.terasology.pathfinding.MazeChunkGenerator;
import org.terasology.pathfinding.PathfinderTestGenerator;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.generator.BaseMapGenerator;
import org.terasology.world.generator.MapGeneratorUri;
import org.terasology.world.generator.core.BasicHMTerrainGenerator;
import org.terasology.world.generator.core.FlatTerrainGenerator;
import org.terasology.world.generator.core.FloraGenerator;
import org.terasology.world.generator.core.ForestGenerator;
import org.terasology.world.liquid.LiquidsGenerator;

/**
 * @author synopia
 */
public class LASMapGenerator extends BaseMapGenerator {
    public LASMapGenerator() {
        super(new MapGeneratorUri("lightAndShadow:mapgen"));
    }

    @Override
    public String name() {
        return "Light and Shadow Map";
    }

    @Override
    public void setup() {
        Block start = BlockManager.getInstance().getBlock("lightAndShadow:redSpawn");
        Block target = BlockManager.getInstance().getBlock("lightAndShadow:redTarget");
        registerChunkGenerator(new MazeChunkGenerator(80, 40, 25, 40,2));
//        registerChunkGenerator(new BasicHMTerrainGenerator());
        registerChunkGenerator(new FloraGenerator());
        registerChunkGenerator(new ForestGenerator());
        registerChunkGenerator(new LiquidsGenerator());
    }
}
