package org.terasology.pathfinding;

import org.terasology.world.generator.BaseMapGenerator;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author synopia
 */
public class PathfinderTestWorldMapGenerator extends BaseMapGenerator {
    public PathfinderTestWorldMapGenerator() {
        super(new MapGeneratorUri("pathfinding:testgen"));
    }

    @Override
    public void setup() {
        registerChunkGenerator(new PathfinderTestGenerator());
    }

    @Override
    public String name() {
        return "Pathfinder Test World";
    }

    @Override
    public boolean hasSetup() {
        return false;
    }
}
