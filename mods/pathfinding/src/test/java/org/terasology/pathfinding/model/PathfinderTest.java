package org.terasology.pathfinding.model;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.generator.core.PathfinderTestGenerator;

/**
 * @author synopia
 */
public class PathfinderTest {
    private TestHelper.TestWorld world;
    private Pathfinder pathfinder;

    @Test
    public void test(){
        pathfinder.init(new Vector3i(0,0,0));
        pathfinder.init(new Vector3i(1,0,0));
        pathfinder.init(new Vector3i(2,0,0));

        Path path = pathfinder.findPath(pathfinder.getBlock(new Vector3i(0, 51, 1)), pathfinder.getBlock(new Vector3i(40, 50, 1)));
        System.out.println(path);
        path = pathfinder.findPath(pathfinder.getBlock(new Vector3i(0, 46, 1)), pathfinder.getBlock(new Vector3i(40, 50, 1)));
        System.out.println(path);
    }

    @Before
    public void setup() {
        Block dirt = new Block();
        dirt.setPenetrable(false);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:Dirt"), dirt));
        TestHelper helper = new TestHelper(new PathfinderTestGenerator(false));
        world = helper.world;
        pathfinder = new Pathfinder(world);
    }

}
