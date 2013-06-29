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

package org.terasology.world;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.engine.CoreRegistry;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.block.management.BlockManagerImpl;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.lighting.LightPropagator;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class LightPropagationTest extends TerasologyTestingEnvironment {

    private static final Vector3i WORLD_MIN = new Vector3i(-Chunk.SIZE_X, 0, -Chunk.SIZE_Z);
    private static final Vector3i WORLD_MAX = new Vector3i(2 * Chunk.SIZE_X - 1, Chunk.SIZE_Y - 1, 2 * Chunk.SIZE_Z - 1);

    ChunkView view;
    LightPropagator propagator;
    Block air;
    Block dirt;
    Block torch;

    @Before
    public void setup() {
        Chunk[] chunks = new Chunk[]{new Chunk(new Vector3i(-1, 0, -1)), new Chunk(new Vector3i(0, 0, -1)), new Chunk(new Vector3i(1, 0, -1)),
                new Chunk(new Vector3i(-1, 0, 0)), new Chunk(new Vector3i(0, 0, 0)), new Chunk(new Vector3i(1, 0, 0)),
                new Chunk(new Vector3i(-1, 0, 1)), new Chunk(new Vector3i(0, 0, 1)), new Chunk(new Vector3i(1, 0, 1))};

        view = new RegionalChunkView(chunks, Region3i.createFromCenterExtents(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1)), new Vector3i(1, 1, 1));
        view.lock();
        propagator = new LightPropagator(view);
        BlockManagerImpl blockManager = new BlockManagerImpl(Lists.<String>newArrayList(), Maps.<String, Byte>newHashMap(), true, new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);

        air = BlockManager.getAir();
        dirt = new Block();
        dirt.setDisplayName("Dirt");
        dirt.setUri(new BlockUri("engine:dirt"));
        dirt.setId((byte) 1);
        for (Side side : Side.values()) {
            dirt.setFullSide(side, true);
        }
        blockManager.addBlockFamily(new SymmetricFamily(dirt.getURI(), dirt), true);
        torch = new Block();
        torch.setDisplayName("Torch");
        torch.setUri(new BlockUri("engine:torch"));
        torch.setId((byte) 2);
        torch.setLuminance(Chunk.MAX_LIGHT);
        blockManager.addBlockFamily(new SymmetricFamily(torch.getURI(), torch), true);
    }

    @After
    public void dispose() {
        if (view != null) {
            view.unlock();
        }
    }

    @Test
    public void testSunlightPropagationIntoDarkness() {
        for (int i = 0; i < Chunk.SIZE_Y; ++i) {
            view.setSunlight(0, i, 0, Chunk.MAX_LIGHT);
        }
        propagator.propagateOutOfTargetChunk();
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(-1, WORLD_MAX.y, WORLD_MAX.z))) {
            byte expected = (byte) Math.max(0, Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x) - TeraMath.fastAbs(pos.z));
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }

    @Test
    public void newUnblockedLightPropagation() {
        view.setBlock(5, 32, 5, torch);
        propagator.update(5, 32, 5, torch, air);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, WORLD_MAX)) {
            byte expected = (byte) Math.max(0, Chunk.MAX_LIGHT - TeraMath.fastAbs(5 - pos.x) - TeraMath.fastAbs(32 - pos.y) - TeraMath.fastAbs(5 - pos.z));
            assertEquals(pos.toString(), expected, view.getLight(pos));
        }
    }

    @Test
    public void newBlockedLightPropagation() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(5, WORLD_MIN.y, WORLD_MIN.z), new Vector3i(5, WORLD_MAX.y, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        Vector3i lightPos = new Vector3i(4, 32, 5);
        view.setBlock(lightPos, torch);
        propagator.update(lightPos, torch, air);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(4, WORLD_MAX.y, WORLD_MAX.z))) {
            byte expected = (byte) Math.max(0, Chunk.MAX_LIGHT - lightPos.gridDistance(pos));
            assertEquals(pos.toString(), expected, view.getLight(pos));
        }
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(5, WORLD_MIN.y, WORLD_MIN.z), WORLD_MAX)) {
            assertEquals(pos.toString(), 0, view.getLight(pos));
        }
    }

    @Test
    public void pullLightThroughHolePropagation() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(5, WORLD_MIN.y, WORLD_MIN.z), new Vector3i(5, WORLD_MAX.y, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        Vector3i lightPos = new Vector3i(4, 32, 5);
        view.setBlock(lightPos, torch);
        propagator.update(lightPos, torch, air);
        Vector3i holePos = new Vector3i(5, 32, 5);
        view.setBlock(holePos, air);
        propagator.update(holePos, air, dirt);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(4, WORLD_MAX.y, WORLD_MAX.z))) {
            byte expected = (byte) Math.max(0, Chunk.MAX_LIGHT - lightPos.gridDistance(pos));
            assertEquals(pos.toString(), expected, view.getLight(pos));
        }
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(5, WORLD_MIN.y, WORLD_MIN.z), new Vector3i(5, WORLD_MAX.y, WORLD_MAX.z))) {
            if (!pos.equals(holePos)) {
                assertEquals(pos.toString(), 0, view.getLight(pos));
            }
        }
        assertEquals(Chunk.MAX_LIGHT - 1, view.getLight(holePos));
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(6, WORLD_MIN.y, WORLD_MIN.z), WORLD_MAX)) {
            byte expected = (byte) Math.max(0, Chunk.MAX_LIGHT - 1 - holePos.gridDistance(pos));
            assertEquals(pos.toString(), expected, view.getLight(pos));
        }
    }

    @Test
    public void simpleClearLightPropagation() {
        view.setBlock(5, 32, 5, torch);
        propagator.update(5, 32, 5, torch, air);
        view.setBlock(5, 32, 5, air);
        propagator.update(5, 32, 5, air, torch);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, WORLD_MAX)) {
            assertEquals(pos.toString(), 0, view.getLight(pos));
        }
    }

    @Test
    public void pushSunlight() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(WORLD_MIN.x, Chunk.SIZE_Y - 1, WORLD_MIN.z), new Vector3i(WORLD_MAX.x, Chunk.SIZE_Y - 1, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), air);
        assertEquals(Region3i.createFromMinAndSize(new Vector3i(8 - 14, 0, 8 - 14), new Vector3i(29, Chunk.SIZE_Y, 29)), propagator.update(8, Chunk.SIZE_Y - 1, 8, air, dirt));
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(WORLD_MAX.x, WORLD_MAX.y - 1, WORLD_MAX.z))) {
            int expected = Math.max(Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x - 8) - TeraMath.fastAbs(pos.z - 8), 0);
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }

    @Test
    public void pushSunlightOverlapping() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(WORLD_MIN.x, Chunk.SIZE_Y - 1, WORLD_MIN.z), new Vector3i(WORLD_MAX.x, Chunk.SIZE_Y - 1, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), air);
        assertEquals(Region3i.createFromMinAndSize(new Vector3i(8 - 14, 0, 8 - 14), new Vector3i(29, Chunk.SIZE_Y, 29)), propagator.update(8, Chunk.SIZE_Y - 1, 8, air, dirt));
        view.setBlock(new Vector3i(14, Chunk.SIZE_Y - 1, 8), air);
        assertEquals(Region3i.createFromMinAndSize(new Vector3i(12, 0, -6), new Vector3i(17, Chunk.SIZE_Y, 29)), propagator.update(14, Chunk.SIZE_Y - 1, 8, air, dirt));
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(WORLD_MAX.x, WORLD_MAX.y - 1, WORLD_MAX.z))) {
            int expected = Math.max(Math.max(Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x - 8) - TeraMath.fastAbs(pos.z - 8), 0), Math.max(Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x - 14) - TeraMath.fastAbs(pos.z - 8), 0));
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }

    @Test
    public void simpleBlockSunlight() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(WORLD_MIN.x, Chunk.SIZE_Y - 1, WORLD_MIN.z), new Vector3i(WORLD_MAX.x, Chunk.SIZE_Y - 1, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), air);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, air, dirt);
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), dirt);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, dirt, air);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, WORLD_MAX)) {
            assertEquals(pos.toString(), 0, view.getSunlight(pos));
        }
    }

    @Test
    public void blockSomeSunlight() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(WORLD_MIN.x, Chunk.SIZE_Y - 1, WORLD_MIN.z), new Vector3i(WORLD_MAX.x, Chunk.SIZE_Y - 1, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), air);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, air, dirt);
        view.setBlock(new Vector3i(14, Chunk.SIZE_Y - 1, 8), air);
        propagator.update(14, Chunk.SIZE_Y - 1, 8, air, dirt);
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), dirt);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, dirt, air);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(WORLD_MAX.x, WORLD_MAX.y - 1, WORLD_MAX.z))) {
            int expected = Math.max(Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x - 14) - TeraMath.fastAbs(pos.z - 8), 0);
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }

    @Test
    public void diagonalPropagation() {
        for (Vector3i pos : Region3i.createFromMinMax(new Vector3i(WORLD_MIN.x, Chunk.SIZE_Y - 1, WORLD_MIN.z), new Vector3i(WORLD_MAX.x, Chunk.SIZE_Y - 1, WORLD_MAX.z))) {
            view.setBlock(pos, dirt);
        }

        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), air);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, air, dirt);
        view.setBlock(new Vector3i(14, Chunk.SIZE_Y - 1, 8), air);
        propagator.update(14, Chunk.SIZE_Y - 1, 8, air, dirt);
        view.setBlock(new Vector3i(8, Chunk.SIZE_Y - 1, 8), dirt);
        propagator.update(8, Chunk.SIZE_Y - 1, 8, dirt, air);
        for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, new Vector3i(WORLD_MAX.x, WORLD_MAX.y - 1, WORLD_MAX.z))) {
            int expected = Math.max(Chunk.MAX_LIGHT - TeraMath.fastAbs(pos.x - 14) - TeraMath.fastAbs(pos.z - 8), 0);
            assertEquals(pos.toString(), expected, view.getSunlight(pos));
        }
    }

    /*@Test
    public void pushSunlightPerf() {
        Block air = BlockManager.getInstance().getAir();
        long total = 0;
        for (int i = 0; i < 1; ++i) {
            for (Vector3i pos : Region3i.createFromMinMax(WORLD_MIN, WORLD_MAX)) {
                view.setSunlight(pos, Chunk.MAX_LIGHT);
                view.setBlock(pos, air, dirt);
            }
            long start = System.nanoTime();
            for (Vector3i pos : Diamond3iIterator.iterate(new Vector3i(8,128,8),8)) {
                view.setBlock(pos, dirt, air);
                new LightPropagator(view).update(pos, dirt, air);
            }
            total += System.nanoTime() - start;
        }
        System.out.println((double)(total) / 10e8);
    }*/

}
