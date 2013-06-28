package org.terasology.ligthandshadow.logic;

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
        super(new MapGeneratorUri("las:mapgen"));
    }

    @Override
    public String name() {
        return "Light and Shadow Map";
    }

    @Override
    public void setup() {
        registerChunkGenerator(new BasicHMTerrainGenerator());
        registerChunkGenerator(new FloraGenerator());
        registerChunkGenerator(new ForestGenerator());
        registerChunkGenerator(new LiquidsGenerator());
    }
}
