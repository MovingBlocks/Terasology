/*
 * Copyright 2012
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

package org.terasology.game.logic.world.generator;

import org.junit.Before;
import org.junit.Test;
import org.terasology.logic.newWorld.NewChunk;
import org.terasology.logic.newWorld.generator.InternalLightGenerator;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class InternalLightGeneratorTest {

    InternalLightGenerator generator;
    Block airBlock;
    Block solidBlock;

    @Before
    public void setup() {
        generator = new InternalLightGenerator();
        airBlock = BlockManager.getInstance().getBlock((byte)0);
        solidBlock = new Block();
        solidBlock.withTitle("Stone");
        solidBlock.withId((byte)1);
        BlockManager.getInstance().addBlock(solidBlock);
    }

    @Test
    public void unblockedSunlightPropagation() {

        NewChunk chunk = new NewChunk(0,0,0);
        generator.generateChunk(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(NewChunk.CHUNK_DIMENSION_X, NewChunk.CHUNK_DIMENSION_Y, NewChunk.CHUNK_DIMENSION_Z))) {
            assertEquals(NewChunk.MAX_LIGHT, chunk.getSunlight(pos));
        }
    }

    @Test
    public void blockedSunlightPropagation() {
        NewChunk chunk = new NewChunk(0,0,0);
        chunk.setBlock(0, 15, 0, solidBlock);
        generator.generateChunk(chunk);

        assertEquals(0, chunk.getSunlight(0, 15, 0));
        for (int y = 0; y < 15; ++y) {
            assertEquals(NewChunk.MAX_LIGHT - 1, chunk.getSunlight(0, y, 0));
        }
    }

    @Test
    public void pinholeSunlightPropagation() {
        NewChunk chunk = new NewChunk(0,0,0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0,NewChunk.CHUNK_DIMENSION_Y - 1,0), new Vector3i(NewChunk.CHUNK_DIMENSION_X,1,NewChunk.CHUNK_DIMENSION_Z))) {
            chunk.setBlock(pos, solidBlock);
        }
        chunk.setBlock(8, NewChunk.CHUNK_DIMENSION_Y - 1, 8, airBlock);
        generator.generateChunk(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(NewChunk.CHUNK_DIMENSION_X, NewChunk.CHUNK_DIMENSION_Y - 1, NewChunk.CHUNK_DIMENSION_Z))) {
            int dist = TeraMath.fastAbs(pos.x - 8) + TeraMath.fastAbs(pos.z - 8);
            int expected = Math.max(NewChunk.MAX_LIGHT - dist, 0);
            assertEquals("Incorrect at " + pos, expected, chunk.getSunlight(pos));
        }

    }

    @Test
    public void sunlightPropagatesUpward() {
        NewChunk chunk = new NewChunk(0,0,0);
        for (Vector3i pos : Region3i.createFromCenterExtents(new Vector3i(9,9,9), Vector3i.one())) {
            chunk.setBlock(pos, solidBlock);
        }

        chunk.setBlock(new Vector3i(9,9,9), airBlock);
        chunk.setBlock(new Vector3i(9,8,9), airBlock);

        generator.generateChunk(chunk);
        assertEquals((byte)13, chunk.getSunlight(new Vector3i(9,7,9)));
        assertEquals((byte)12, chunk.getSunlight(new Vector3i(9,8,9)));
        assertEquals((byte)11, chunk.getSunlight(new Vector3i(9,9,9)));
    }
}
