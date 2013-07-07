package org.terasology.pathfinding.model;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Vector3i;
import org.terasology.pathfinding.MazeChunkGenerator;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;

/**
 * @author synopia
 */
public class MazeTest {
    private TestHelper.TestWorld world;
    private Pathfinder pathfinder;
    private TestHelper helper;

    @Test
    public void test() {
        WalkableBlock start = pathfinder.getBlock(new Vector3i(55, 6, 1));
        Assert.assertNotNull(start);
        WalkableBlock target = pathfinder.getBlock(new Vector3i(1, 3 * 3, 1));
        Assert.assertNotNull(target);

        StringBuilder sb = new StringBuilder();
        for (int l = 6; l < 7; l++) {
            for (int j = 0; j < 100; j++) {
                for (int i = 0; i <= 160; i++) {
                    WalkableBlock block = pathfinder.getBlock(new Vector3i(i, l, j));
                    if( block!=null ) {
                        sb.append(block.floor.isEntrance(block) ? 'C' : ' ');
                    } else {
                        sb.append(world.getBlock(i, l, j).isPenetrable() ? ' ' : 'X');
                    }
                }
                sb.append("\n");
            }
        }
        System.out.println(sb);
        Assert.assertTrue(pathfinder.findPath(start,target).size()>0);
    }


    @Before
    public void setup() {
        Block dirt = new Block();
        dirt.setPenetrable(false);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:Dirt"), dirt));
        int width = 160;
        int height = 100;
        helper = new TestHelper(new MazeChunkGenerator(width, height,4,0, 20));
        world = helper.world;
        pathfinder = new Pathfinder(world);
        for (int x = 0; x < width /16+1; x++) {
            for (int z = 0; z < height /16+1; z++) {
                 pathfinder.init(new Vector3i(x,0,z));
            }
        }
    }

}
