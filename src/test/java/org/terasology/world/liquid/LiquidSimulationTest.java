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

package org.terasology.world.liquid;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;

/**
 * @author Immortius
 */
public class LiquidSimulationTest {

    WorldView view;
    Block air;
    Block dirt;

    @Before
    public void setup() {
        Chunk[] chunks = new Chunk[] {new Chunk(new Vector3i(-1,0,-1)), new Chunk(new Vector3i(0,0,-1)), new Chunk(new Vector3i(1,0,-1)),
                new Chunk(new Vector3i(-1,0,0)), new Chunk(new Vector3i(0,0,0)), new Chunk(new Vector3i(1,0,0)),
                new Chunk(new Vector3i(-1,0,1)), new Chunk(new Vector3i(0,0,1)), new Chunk(new Vector3i(1,0,1))};

        view = new WorldView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1,1,1));

        air = BlockManager.getInstance().getBlock((byte)0);
        dirt = new Block();
        dirt.setDisplayName("Dirt");
        dirt.setUri(new BlockUri("engine:dirt"));
        dirt.setId((byte) 1);
        BlockManager.getInstance().addBlockFamily(new SymmetricFamily(dirt.getURI(), dirt));

        for (int x = -Chunk.SIZE_X + 1; x < 2 * Chunk.SIZE_X; ++x) {
            for (int z = -Chunk.SIZE_Z + 1; z < 2 * Chunk.SIZE_Z; ++z) {
                view.setBlock(x, 0, z, dirt, air);
            }
        }

    }

    @Test
    public void calcStateSolidBlock() {
        view.setBlock(new Vector3i(0,1,0), dirt, air);
        view.setLiquid(new Vector3i(1, 1, 0), new LiquidData(LiquidType.WATER, (byte)7), new LiquidData());

        assertEquals(new LiquidData(), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateFlowIntoDecaying() {
        view.setLiquid(new Vector3i(1, 1, 0), new LiquidData(LiquidType.WATER, (byte)7), new LiquidData());

        assertEquals(new LiquidData(LiquidType.WATER, 2), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateFlowDownwards() {
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData(LiquidType.WATER, 3), new LiquidData());

        assertEquals(new LiquidData(LiquidType.WATER, 6), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateWhenAnnexed() {
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData(LiquidType.WATER, 3), new LiquidData());
        view.setLiquid(new Vector3i(0, 1, 0), new LiquidData(LiquidType.WATER, 6), new LiquidData());
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData(), new LiquidData(LiquidType.WATER, 3));
        assertEquals(new LiquidData(), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }
}
