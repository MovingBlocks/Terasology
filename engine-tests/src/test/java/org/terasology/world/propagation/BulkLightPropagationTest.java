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
package org.terasology.world.propagation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.math.Diamond3iIterator;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.Vector3iUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.NullWorldAtlas;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.propagation.light.LightPropagationRules;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class BulkLightPropagationTest extends TerasologyTestingEnvironment {

    private BlockManagerImpl blockManager;
    private Block air;
    private Block fullLight;
    private Block weakLight;
    private Block mediumLight;
    private Block solid;
    private Block solidMediumLight;
    private LightPropagationRules lightRules;

    private Region3i testingRegion = Region3i.createFromMinMax(new Vector3i(-ChunkConstants.SIZE_X, -ChunkConstants.SIZE_Y, -ChunkConstants.SIZE_Z),
            new Vector3i(2 * ChunkConstants.SIZE_X, 2 * ChunkConstants.SIZE_Y, 2 * ChunkConstants.SIZE_Z));

    @Before
    public void setup() throws Exception {
        super.setup();
        lightRules = new LightPropagationRules();
        blockManager = new BlockManagerImpl(new NullWorldAtlas(),
                Lists.<String>newArrayList(), Maps.<String, Short>newHashMap(), true, new DefaultBlockFamilyFactoryRegistry());
        CoreRegistry.put(BlockManager.class, blockManager);
        fullLight = new Block();
        fullLight.setDisplayName("Torch");
        fullLight.setUri(new BlockUri("engine:torch"));
        fullLight.setId((short) 2);
        fullLight.setLuminance(ChunkConstants.MAX_LIGHT);
        blockManager.addBlockFamily(new SymmetricFamily(fullLight.getURI(), fullLight), true);

        weakLight = new Block();
        weakLight.setDisplayName("PartLight");
        weakLight.setUri(new BlockUri("engine:weakLight"));
        weakLight.setId((short) 3);
        weakLight.setLuminance((byte) 2);
        blockManager.addBlockFamily(new SymmetricFamily(weakLight.getURI(), weakLight), true);

        mediumLight = new Block();
        mediumLight.setDisplayName("MediumLight");
        mediumLight.setUri(new BlockUri("engine:mediumLight"));
        mediumLight.setId((short) 4);
        mediumLight.setLuminance((byte) 5);
        blockManager.addBlockFamily(new SymmetricFamily(mediumLight.getURI(), mediumLight), true);

        solid = new Block();
        solid.setDisplayName("Solid");
        solid.setUri(new BlockUri("engine:solid"));
        solid.setId((short) 5);
        for (Side side : Side.values()) {
            solid.setFullSide(side, true);
        }
        blockManager.addBlockFamily(new SymmetricFamily(solid.getURI(), solid), true);

        solidMediumLight = new Block();
        solidMediumLight.setDisplayName("SolidMediumLight");
        solidMediumLight.setUri(new BlockUri("engine:solidMediumLight"));
        solidMediumLight.setId((short) 6);
        solidMediumLight.setLuminance((byte) 5);
        for (Side side : Side.values()) {
            solidMediumLight.setFullSide(side, true);
        }
        blockManager.addBlockFamily(new SymmetricFamily(solidMediumLight.getURI(), solidMediumLight), true);


        air = BlockManager.getAir();
    }

    @Test
    public void addLightInVacuum() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);

        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight));

        assertEquals(fullLight.getLuminance(), worldView.getValueAt(Vector3iUtil.zero()));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(0, 1, 0)));
        assertEquals(fullLight.getLuminance() - 14, worldView.getValueAt(new Vector3i(0, 14, 0)));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(fullLight.getLuminance() - i, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void removeLightInVacuum() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight));

        worldView.setBlockAt(Vector3iUtil.zero(), air);
        propagator.process(new BlockChange(Vector3iUtil.zero(), fullLight, air));

        assertEquals(0, worldView.getValueAt(Vector3iUtil.zero()));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(0, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void reduceLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight));

        worldView.setBlockAt(Vector3iUtil.zero(), weakLight);
        propagator.process(new BlockChange(Vector3iUtil.zero(), fullLight, weakLight));

        assertEquals(weakLight.getLuminance(), worldView.getValueAt(Vector3iUtil.zero()));
        for (int i = 1; i < 15; ++i) {
            byte expectedLuminance = (byte) Math.max(0, weakLight.getLuminance() - i);
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void addOverlappingLights() {
        Vector3i lightPos = new Vector3i(5, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(ChunkConstants.CHUNK_REGION);
        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);
        worldView.setBlockAt(lightPos, fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight), new BlockChange(lightPos, air, fullLight));

        assertEquals(fullLight.getLuminance(), worldView.getValueAt(Vector3iUtil.zero()));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(fullLight.getLuminance() - 2, worldView.getValueAt(new Vector3i(2, 0, 0)));
        assertEquals(fullLight.getLuminance() - 2, worldView.getValueAt(new Vector3i(3, 0, 0)));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(4, 0, 0)));
        assertEquals(fullLight.getLuminance(), worldView.getValueAt(new Vector3i(5, 0, 0)));
    }

    @Test
    public void removeOverlappingLight() {
        Vector3i lightPos = new Vector3i(5, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);
        worldView.setBlockAt(lightPos, fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight), new BlockChange(lightPos, air, fullLight));

        worldView.setBlockAt(lightPos, air);
        propagator.process(new BlockChange(lightPos, fullLight, air));

        for (int i = 0; i < 16; ++i) {
            byte expectedLuminance = (byte) Math.max(0, fullLight.getLuminance() - i);
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void removeLightOverlappingAtEdge() {
        Vector3i lightPos = new Vector3i(2, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(Vector3iUtil.zero(), weakLight);
        worldView.setBlockAt(lightPos, weakLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, weakLight), new BlockChange(lightPos, air, weakLight));

        worldView.setBlockAt(lightPos, air);
        propagator.process(new BlockChange(lightPos, weakLight, air));

        for (int i = 0; i < weakLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) Math.max(0, weakLight.getLuminance() - i);
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void addLightInLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(new Vector3i(2, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(2, 0, 0), air, mediumLight));

        worldView.setBlockAt(Vector3iUtil.zero(), fullLight);
        propagator.process(new BlockChange(Vector3iUtil.zero(), air, fullLight));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) Math.max(0, fullLight.getLuminance() - i);
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void addAdjacentLights() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(new Vector3i(1, 0, 0), mediumLight);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, mediumLight), new BlockChange(new Vector3i(0, 0, 0), air, mediumLight));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                int dist = Math.min(Vector3iUtil.zero().gridDistance(pos), new Vector3i(1, 0, 0).gridDistance(pos));
                byte expectedLuminance = (byte) Math.max(mediumLight.getLuminance() - dist, 0);
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void addWeakLightNextToStrongLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(0, 0, 0), air, fullLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), weakLight);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, weakLight));
        assertEquals(14, worldView.getValueAt(new Vector3i(1, 0, 0)));
    }

    @Test
    public void removeAdjacentLights() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        worldView.setBlockAt(new Vector3i(1, 0, 0), mediumLight);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, mediumLight), new BlockChange(new Vector3i(0, 0, 0), air, mediumLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), mediumLight, air), new BlockChange(new Vector3i(0, 0, 0), mediumLight, air));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) 0;
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void addSolidBlocksLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(ChunkConstants.CHUNK_REGION);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(0, 0, 0), air, mediumLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), solid);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, solid));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(1, worldView.getValueAt(new Vector3i(2, 0, 0)));
    }

    @Test
    public void removeSolidAllowsLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        for (Vector3i pos : Region3i.createFromCenterExtents(new Vector3i(1, 0, 0), new Vector3i(0, 30, 30))) {
            worldView.setBlockAt(pos, solid);
        }
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(0, 0, 0), air, fullLight));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), solid, air));

        assertEquals(14, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(13, worldView.getValueAt(new Vector3i(2, 0, 0)));
    }

    @Test
    public void removeSolidAndLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion);
        for (Vector3i pos : Region3i.createFromCenterExtents(new Vector3i(1, 0, 0), new Vector3i(0, 30, 30))) {
            worldView.setBlockAt(pos, solid);
        }
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(0, 0, 0), air, fullLight));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), solid, air), new BlockChange(new Vector3i(0, 0, 0), fullLight, air));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) 0;
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(Vector3iUtil.zero(), i)) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

}
