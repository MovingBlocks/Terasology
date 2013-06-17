package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.core.PathfinderTestGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * @author synopia
 */
public class ConnectHeightMapTest {

    private TestHelper.TestWorld world;
    private TestHelper helper;

    public static String[] contourExpected = new String[]{
            "C              C",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "                ",
            "C              C",
    };

    @Test
    public void test1(){
        HeightMap center = new HeightMap(world, new Vector3i(1,0,1));
        HeightMap up = new HeightMap(world, new Vector3i(1,0,0));
        HeightMap down = new HeightMap(world, new Vector3i(1,0,2));
        HeightMap left = new HeightMap(world, new Vector3i(0,0,1));
        HeightMap right = new HeightMap(world, new Vector3i(2,0,1));

        center.update();
        up.update();
        down.update();
        left.update();
        right.update();

        center.connectNeighborMaps(left, up, right, down);
        assertCenter(center, left, up, right, down, contourExpected);

        center.disconnectNeighborMaps(left, up, right, down);
        center = new HeightMap(world, new Vector3i(1,0,1));
        center.update();
        center.connectNeighborMaps(left, up, right, down);
        assertCenter(center, left, up, right, down, contourExpected);


    }

    @Test
    public void test2(){
        HeightMap center = new HeightMap(world, new Vector3i(1,0,1));
        HeightMap up = new HeightMap(world, new Vector3i(1,0,0));
        HeightMap down = new HeightMap(world, new Vector3i(1,0,2));
        HeightMap left = new HeightMap(world, new Vector3i(0,0,1));
        HeightMap right = new HeightMap(world, new Vector3i(2,0,1));
        HeightMap lu = new HeightMap(world, new Vector3i(0, 0, 0));
        HeightMap ru = new HeightMap(world, new Vector3i(2, 0, 0));
        HeightMap ld = new HeightMap(world, new Vector3i(0, 0, 2));
        HeightMap rd = new HeightMap(world, new Vector3i(2, 0, 2));


        lu.update();
        lu.connectNeighborMaps(null, null, null, null);
        up.update();
        up.connectNeighborMaps(lu, null, null, null);

        ru.update();
        ru.connectNeighborMaps(up, null, null, null);

        left.update();
        left.connectNeighborMaps(null, lu, null, null);
        center.update();
        center.connectNeighborMaps(left, up, null, null);
        right.update();
        right.connectNeighborMaps(center, ru, null, null);

        ld.update();
        ld.connectNeighborMaps(null, left, null, null);
        down.update();
        down.connectNeighborMaps(ld, center, null, null);
        rd.update();
        rd.connectNeighborMaps(down, right, null, null);

        assertCenter(center, left, up, right, down, contourExpected);
    }

    @Before
    public void setup() {
        Block dirt = new Block();
        dirt.setPenetrable(false);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:Dirt"), dirt));
        helper = new TestHelper(new PathfinderTestGenerator(false));
        world = helper.world;
    }

    private void assertCenter(HeightMap center, HeightMap left, HeightMap up, HeightMap right, HeightMap down) {
        assertCenter(center, left, up, right, down, null);
    }
    private void assertCenter(HeightMap center, HeightMap left, HeightMap up, HeightMap right, HeightMap down, String[] contours) {
        final Floor centerFloor = center.getFloor(0);
        Floor upFloor = up.getFloor(0);
        Floor downFloor = down.getFloor(0);
        Floor leftFloor = left.getFloor(0);
        Floor rightFloor = right.getFloor(0);
        assertSet(centerFloor.getNeighborRegions(), upFloor, leftFloor, rightFloor, downFloor);

        if( contours!=null ) {
            String[] actual = helper.evaluate(new TestHelper.Runner() {
                @Override
                public char run(int x, int y, int z, char value) {
                    return centerFloor.isContour(x, z) ? 'C' : ' ';
                }
            }, 0, 50, 0, 16, 1, 16);
            Assert.assertArrayEquals(contours, actual);
        }
    }


    private <T>void assertSet( Set<T> set, T... items ) {
        Set<T> rest = new HashSet<T>(set);
        for (T item : items) {
            Assert.assertTrue(rest.remove(item));
        }
        Assert.assertEquals(0, rest.size());

    }
}
