/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.internal.ContextImpl;
import org.terasology.core.world.generator.trees.TreeGenerator;
import org.terasology.core.world.generator.trees.Trees;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.MersenneRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

/**
 * TODO: more flexibility for estimated extents
 */
public class TreeTests {

    private static final Logger logger = LoggerFactory.getLogger(TreeTests.class);

    private BlockManager blockManager;
    private BiomeManager biomeManager;

    @Before
    public void setup() {
        ContextImpl context = new ContextImpl();
        CoreRegistry.setContext(context);

        // Needed only as long as #1536 is unresolved
        context.put(Config.class, new Config());

        blockManager = Mockito.mock(BlockManager.class);
        Block air = blockManager.getBlock(BlockManager.AIR_ID);

        biomeManager = Mockito.mock(BiomeManager.class);

        Mockito.when(blockManager.getBlock(Matchers.<BlockUri>any())).thenReturn(air);
        Mockito.when(blockManager.getBlock(Matchers.<String>any())).thenReturn(air);

        context.put(BlockManager.class, blockManager);
    }

    @Test
    public void testBirchDims() {
        Assert.assertEquals(new Vector3i(22, 32, 22), estimateExtent(Trees.birchTree()));
    }

    @Test
    public void testOakDims() {
        Assert.assertEquals(new Vector3i(14, 14, 14), estimateExtent(Trees.oakTree()));
    }

    @Test
    public void testOakVariationDims() {
        Assert.assertEquals(new Vector3i(21, 19, 20), estimateExtent(Trees.oakVariationTree()));
    }

    @Test
    public void testPineDims() {
        Assert.assertEquals(new Vector3i(25, 28, 26), estimateExtent(Trees.pineTree()));
    }

    @Test
    public void testRedTreeDims() {
        Assert.assertEquals(new Vector3i(14, 14, 14), estimateExtent(Trees.redTree()));
    }

    private Vector3i estimateExtent(TreeGenerator treeGen) {
        Vector3i maxExt = new Vector3i();

        for (int i = 0; i < 100; i++) {
            Vector3i ext = computeAABB(treeGen, i * 37);
            maximize(maxExt, ext);
        }

        return maxExt;
    }

    private Vector3i computeAABB(TreeGenerator treeGen, long seed) {
        Vector3i pos = new Vector3i(ChunkConstants.SIZE_X / 2, 0, ChunkConstants.SIZE_Z / 2);

        final Vector3i min = new Vector3i(pos);
        final Vector3i max = new Vector3i(pos);

        Rect2i chunks = Rect2i.createFromMinAndMax(-1, -1, 1, 1);
        for (BaseVector2i chunkPos : chunks.contents()) {
            Chunk chunk = new ChunkImpl(chunkPos.getX(), 0, chunkPos.getY(), blockManager, biomeManager) {
                @Override
                public Block setBlock(int x, int y, int z, Block block) {
                    Vector3i world = chunkToWorldPosition(x, y, z);
                    minimize(min, world);
                    maximize(max, world);

                    return null;
                }
            };

            Random random = new MersenneRandom(seed);
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Vector3i relPos = chunk.chunkToWorldPosition(0, 0, 0).sub(pos).invert();
            treeGen.generate(blockManager, chunk, random, relPos.x, relPos.y, relPos.z);
        }

        Vector3i ext = new Vector3i(max).sub(min);
//        logger.info(String.format("Min: %12s  Max: %12s  Extent: %s", min, max, ext));

        return ext;
    }

    private void minimize(Vector3i v, Vector3i other) {
        v.x = Math.min(v.x, other.x);
        v.y = Math.min(v.y, other.y);
        v.z = Math.min(v.z, other.z);
    }

    private void maximize(Vector3i v, Vector3i other) {
        v.x = Math.max(v.x, other.x);
        v.y = Math.max(v.y, other.y);
        v.z = Math.max(v.z, other.z);
    }
}
