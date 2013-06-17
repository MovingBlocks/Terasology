package org.terasology.pathfinding.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.generator.core.PathfinderTestGenerator;

import java.util.Arrays;

/**
 * @author synopia
 */
public class PathfinderTest {
    private TestHelper.TestWorld world;
    private Pathfinder pathfinder;
    private TestHelper helper;

    @Test
    public void test(){
        pathfinder.init(new Vector3i(0,0,0));
        pathfinder.init(new Vector3i(1,0,0));
        pathfinder.init(new Vector3i(2,0,0));
        pathfinder.init(new Vector3i(0,0,1));
        pathfinder.init(new Vector3i(1,0,1));
        pathfinder.init(new Vector3i(2,0,1));
        pathfinder.init(new Vector3i(0,0,2));
        pathfinder.init(new Vector3i(1,0,2));
        pathfinder.init(new Vector3i(2,0,2));

        Path path = pathfinder.findPath(pathfinder.getBlock(new Vector3i(0, 51, 1)), pathfinder.getBlock(new Vector3i(14+16, 45, 12)));
        Assert.assertEquals(0, path.size());

        helper.setAir(7, 50, 7);
        helper.setAir(7, 50, 8);
        helper.setAir(Chunk.SIZE_X-1, 47, 7);
        helper.setAir(Chunk.SIZE_X-1, 47, 8);
        helper.setAir(Chunk.SIZE_X, 47, 7);
        helper.setAir(Chunk.SIZE_X, 47, 8);


        pathfinder.update(new Vector3i(0,0,0));
        pathfinder.update(new Vector3i(1,0,0));

        path = pathfinder.findPath(pathfinder.getBlock(new Vector3i(0, 51, 1)), pathfinder.getBlock(new Vector3i(14+16, 45, 12)));
        Assert.assertTrue(0<path.size());
    }

    @Test
    public void testStairs() {
        assertStairs(0,0);
        assertStairs(1,0);
        assertStairs(0,1);
        assertStairs(1,1);
    }
    public void assertStairs(int chunkX, int chunkZ){
        int x = chunkX*Chunk.SIZE_X;
        int z = chunkZ*Chunk.SIZE_Z;
        HeightMap map = pathfinder.init(new Vector3i(chunkX,0,chunkZ));

        WalkableBlock startBlock = pathfinder.getBlock(new Vector3i(0 + x, 51, 1 + z));
        WalkableBlock targetBlock = pathfinder.getBlock(new Vector3i(x + Chunk.SIZE_X - 2, 45, z + Chunk.SIZE_Z - 4));

        Assert.assertEquals(map, startBlock.floor.heightMap);
        Assert.assertEquals(map, targetBlock.floor.heightMap);
        Assert.assertEquals(map.getBlock(x+0, 51, z+1), startBlock);
        Assert.assertEquals(map.getBlock(x+14, 45, z+12), targetBlock);
        Path path = pathfinder.findPath(startBlock, targetBlock);
        Assert.assertEquals(0, path.size());

        helper.setAir(x+7, 50, z+7);
        helper.setAir(x+7, 50, z+8);

        pathfinder.update(new Vector3i(chunkX,0,chunkZ));

        path = pathfinder.findPath(startBlock, targetBlock);
        Assert.assertTrue(0 < path.size());
    }

    @Before
    public void setup() {
        Block dirt = new Block();
        dirt.setPenetrable(false);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(new BlockUri("engine:Dirt"), dirt));
        helper = new TestHelper(new PathfinderTestGenerator(true));
        world = helper.world;
        pathfinder = new Pathfinder(world);
    }

}
