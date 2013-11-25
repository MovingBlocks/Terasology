/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.core.world.liquid;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Vector3i;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class LiquidSimulationTest extends TerasologyTestingEnvironment {

    ChunkView view;
    Block air;
    Block dirt;

    @Before
    public void setup() {
        ChunkImpl[] chunks = new ChunkImpl[]{new ChunkImpl(new Vector3i(-1, 0, -1)), new ChunkImpl(new Vector3i(0, 0, -1)), new ChunkImpl(new Vector3i(1, 0, -1)),
                new ChunkImpl(new Vector3i(-1, 0, 0)), new ChunkImpl(new Vector3i(0, 0, 0)), new ChunkImpl(new Vector3i(1, 0, 0)),
                new ChunkImpl(new Vector3i(-1, 0, 1)), new ChunkImpl(new Vector3i(0, 0, 1)), new ChunkImpl(new Vector3i(1, 0, 1))};

        view = new ChunkViewCoreImpl(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        view.lock();


        BlockManagerImpl blockManager = (BlockManagerImpl) CoreRegistry.get(BlockManager.class);
        air = BlockManager.getAir();
        dirt = new Block();
        dirt.setDisplayName("Dirt");
        dirt.setUri(new BlockUri("core:dirt"));
        dirt.setId((byte) 1);
        blockManager.addBlockFamily(new SymmetricFamily(dirt.getURI(), dirt), true);

        for (int x = -ChunkConstants.SIZE_X + 1; x < 2 * ChunkConstants.SIZE_X; ++x) {
            for (int z = -ChunkConstants.SIZE_Z + 1; z < 2 * ChunkConstants.SIZE_Z; ++z) {
                view.setBlock(x, 0, z, dirt);
            }
        }

    }

    @After
    public void dispose() {
        if (view != null) {
            view.unlock();
        }
    }

    @Test
    public void calcStateSolidBlock() {
        view.setBlock(new Vector3i(0, 1, 0), dirt);
        view.setLiquid(new Vector3i(1, 1, 0), new LiquidData(LiquidType.WATER, (byte) 7));

        assertEquals(new LiquidData(), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateFlowIntoDecaying() {
        view.setLiquid(new Vector3i(1, 1, 0), new LiquidData(LiquidType.WATER, (byte) 7));

        assertEquals(new LiquidData(LiquidType.WATER, 2), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateFlowDownwards() {
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData(LiquidType.WATER, 3));

        assertEquals(new LiquidData(LiquidType.WATER, 6), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }

    @Test
    public void calcStateWhenAnnexed() {
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData(LiquidType.WATER, 3));
        view.setLiquid(new Vector3i(0, 1, 0), new LiquidData(LiquidType.WATER, 6));
        view.setLiquid(new Vector3i(0, 2, 0), new LiquidData());
        assertEquals(new LiquidData(), LiquidSimulator.calcStateFor(new Vector3i(0, 1, 0), view));
    }
}
