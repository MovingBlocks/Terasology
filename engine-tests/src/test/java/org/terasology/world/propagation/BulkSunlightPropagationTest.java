/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.propagation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.NullWorldAtlas;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class BulkSunlightPropagationTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private Block air;
    private Block solid;
    private SunlightPropagationRules lightRules;
    private SunlightRegenPropagationRules regenRules;

    private StubPropagatorWorldView regenWorldView;
    private StubPropagatorWorldView lightWorldView;

    private BatchPropagator sunlightPropagator;
    private SunlightRegenBatchPropagator propagator;


    @Before
    public void setup() throws Exception {
        super.setup();

        regenRules = new SunlightRegenPropagationRules();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(),
                Lists.<String>newArrayList(), Maps.<String, Short>newHashMap(), true, new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);

        solid = new Block();
        solid.setDisplayName("Solid");
        solid.setUri(new BlockUri("engine:solid"));
        solid.setId((byte) 5);
        for (Side side : Side.values()) {
            solid.setFullSide(side, true);
        }
        blockManager.addBlockFamily(new SymmetricFamily(solid.getURI(), solid), true);

        Map<Vector3i, Block> blockData = Maps.newHashMap();
        regenWorldView = new StubPropagatorWorldView(ChunkConstants.CHUNK_REGION, blockData);
        lightWorldView = new StubPropagatorWorldView(ChunkConstants.CHUNK_REGION, blockData);

        lightRules = new SunlightPropagationRules(regenWorldView);
        sunlightPropagator = new StandardBatchPropagator(lightRules, lightWorldView);
        propagator = new SunlightRegenBatchPropagator(regenRules, regenWorldView, sunlightPropagator, lightWorldView);

        air = BlockManager.getAir();

    }

    @Test
    public void allowSunlightVertical() {
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 16, 0), new Vector3i(ChunkConstants.SIZE_X - 1, ChunkConstants.SIZE_Y - 1, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setValueAt(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
            lightWorldView.setValueAt(pos, ChunkConstants.MAX_SUNLIGHT);
        }
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 15, 0), new Vector3i(ChunkConstants.SIZE_X - 1, 15, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setBlockAt(pos, solid);
        }
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X - 1, 14, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setValueAt(pos, (byte) (14 - pos.y));
        }

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), air);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));

        for (int y = 0; y < 16; y++) {
            assertEquals("Incorrect value at " + y, ChunkConstants.MAX_SUNLIGHT_REGEN, regenWorldView.getValueAt(new Vector3i(16, y, 16)));
            assertEquals(ChunkConstants.MAX_SUNLIGHT, lightWorldView.getValueAt(new Vector3i(16, y, 16)));
        }
        for (int y = 0; y < 15; y++) {
            assertEquals(ChunkConstants.MAX_SUNLIGHT - 1, lightWorldView.getValueAt(new Vector3i(15, y, 16)));
        }
    }

    @Test
    public void stopSunlightVertical() {
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 16, 0), new Vector3i(ChunkConstants.SIZE_X - 1, ChunkConstants.SIZE_Y - 1, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setValueAt(pos, ChunkConstants.MAX_SUNLIGHT_REGEN);
            lightWorldView.setValueAt(pos, ChunkConstants.MAX_SUNLIGHT);
        }
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 15, 0), new Vector3i(ChunkConstants.SIZE_X - 1, 15, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setBlockAt(pos, solid);
        }
        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X - 1, 14, ChunkConstants.SIZE_Z - 1))) {
            regenWorldView.setValueAt(pos, (byte) (14 - pos.y));
        }

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), air);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), solid, air));

        regenWorldView.setBlockAt(new Vector3i(16, 15, 16), solid);
        propagator.process(new BlockChange(new Vector3i(16, 15, 16), air, solid));
        sunlightPropagator.process(new BlockChange(new Vector3i(16, 15, 16), air, solid));

        for (Vector3i pos : Region3i.createBounded(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X - 1, 15, ChunkConstants.SIZE_Z - 1))) {
            assertEquals("Incorrect value at " + pos, Math.max(0, 14 - pos.y), regenWorldView.getValueAt(pos));
            assertEquals("Incorrect value at " + pos, 0, lightWorldView.getValueAt(pos));
        }
    }

}
