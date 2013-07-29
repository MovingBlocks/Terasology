package org.terasology.world;

import org.junit.Before;
import org.junit.Test;
import org.terasology.config.Config;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;
import org.terasology.world.chunks.Chunk;

import javax.vecmath.Vector3f;

import static org.junit.Assert.assertEquals;


public class ChunkTest {

    private Chunk chunk;
    private BlockManagerImpl blockManager;

    @Before
    public void setup() {
        CoreRegistry.put(Config.class, new Config());
        blockManager = new BlockManagerImpl(new WorldAtlas(4096), new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);
        chunk = new Chunk(new Vector3i(0, 0, 0));
    }

    @Test
    public void testChangeBlock() {

        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("some:uri"), new Block()), false);
        Block block = blockManager.getBlock("some:uri");
        chunk.setBlock(new Vector3i(1, 2, 3), block);
        assertEquals(block, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

    @Test
    public void getAABB() {
        assertEquals(new Vector3f(0,0,0), chunk.getAABB().getMin());
        assertEquals(new Vector3f(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z), chunk.getAABB().getMax());
    }

}

