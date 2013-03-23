/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.world.generator;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.terasology.game.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerAuthority;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.lighting.InternalLightProcessor;

/**
 * @author Immortius
 */
public class InternalLightGeneratorTest {

    Block airBlock;
    Block solidBlock;

    @Before
    public void setup() {
        BlockManagerAuthority blockManager = new BlockManagerAuthority();
        CoreRegistry.put(BlockManager.class, blockManager);
        airBlock = BlockManager.getAir();
        solidBlock = new Block();
        solidBlock.setDisplayName("Stone");
        solidBlock.setUri(new BlockUri("engine:stone"));
        solidBlock.setId((byte) 1);
        for (Side side : Side.values()) {
            solidBlock.setFullSide(side, true);
        }
        solidBlock.setTranslucent(false);
        blockManager.addBlockFamily(new SymmetricFamily(solidBlock.getURI(), solidBlock), true);
    }

    @Test
    public void unblockedSunlightPropagation() {

        Chunk chunk = new Chunk(0,0,0);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z))) {
            assertEquals(Chunk.MAX_LIGHT, chunk.getSunlight(pos));
        }
    }

    @Test
    public void blockedSunlightPropagation() {
        Chunk chunk = new Chunk(0,0,0);
        chunk.setBlock(0, 15, 0, solidBlock);
        InternalLightProcessor.generateInternalLighting(chunk);

        assertEquals(0, chunk.getSunlight(0, 15, 0));
        for (int y = 0; y < 15; ++y) {
            assertEquals(Chunk.MAX_LIGHT - 1, chunk.getSunlight(0, y, 0));
        }
    }

    @Test
    public void pinholeSunlightPropagation() {
        Chunk chunk = new Chunk(0,0,0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, Chunk.SIZE_Y - 1,0), new Vector3i(Chunk.SIZE_X,1, Chunk.SIZE_Z))) {
            chunk.setBlock(pos, solidBlock);
        }
        chunk.setBlock(8, Chunk.SIZE_Y - 1, 8, airBlock);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(Chunk.SIZE_X, Chunk.SIZE_Y - 1, Chunk.SIZE_Z))) {
            int dist = TeraMath.fastAbs(pos.x - 8) + TeraMath.fastAbs(pos.z - 8);
            int expected = Math.max(Chunk.MAX_LIGHT - dist, 0);
            assertEquals("Incorrect at " + pos, expected, chunk.getSunlight(pos));
        }

    }

    @Test
    public void sunlightPropagatesUpward() {
        Chunk chunk = new Chunk(0,0,0);
        for (Vector3i pos : Region3i.createFromCenterExtents(new Vector3i(9,9,9), Vector3i.one())) {
            chunk.setBlock(pos, solidBlock);
        }

        chunk.setBlock(new Vector3i(9,9,9), airBlock);
        chunk.setBlock(new Vector3i(9,8,9), airBlock);

        InternalLightProcessor.generateInternalLighting(chunk);
        assertEquals((byte)13, chunk.getSunlight(new Vector3i(9,7,9)));
        assertEquals((byte)12, chunk.getSunlight(new Vector3i(9,8,9)));
        assertEquals((byte)11, chunk.getSunlight(new Vector3i(9,9,9)));
    }
}
