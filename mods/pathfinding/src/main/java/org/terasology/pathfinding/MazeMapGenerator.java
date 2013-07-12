package org.terasology.pathfinding;

import org.terasology.world.generator.BaseMapGenerator;
import org.terasology.world.generator.MapGeneratorUri;

/**
 * @author synopia
 */
public class MazeMapGenerator extends BaseMapGenerator {
    public MazeMapGenerator() {
        super(new MapGeneratorUri("pathfinding:maze"));
    }

    @Override
    public String name() {
        return "Maze";
    }

    @Override
    public void setup() {
        MazeChunkGenerator generator = new MazeChunkGenerator(10,10, 1,50,1);
        registerChunkGenerator(generator);
    }

    @Override
    public boolean hasSetup() {
        return false;
    }
}
