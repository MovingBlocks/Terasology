/*
 * Copyright 2018 MovingBlocks
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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.Diamond3iIterable;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.propagation.light.LightPropagationRules;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BulkLightPropagationTest extends TerasologyTestingEnvironment {

    private static final Vector3ic ZERO_VECTOR = new Vector3i();

    private BlockManagerImpl blockManager;
    private Block air;
    private Block fullLight;
    private Block weakLight;
    private Block mediumLight;
    private Block solid;
    private Block solidMediumLight;
    private LightPropagationRules lightRules;

    private BlockRegion testingRegion = new BlockRegion(-Chunks.SIZE_X, -Chunks.SIZE_Y, -Chunks.SIZE_Z,
            2 * Chunks.SIZE_X, 2 * Chunks.SIZE_Y, 2 * Chunks.SIZE_Z);

    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        lightRules = new LightPropagationRules();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        BlockFamilyDefinitionData fullLightData = new BlockFamilyDefinitionData();
        fullLightData.getBaseSection().setDisplayName("Torch");
        fullLightData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        fullLightData.getBaseSection().setLuminance(Chunks.MAX_LIGHT);
        fullLightData.getBaseSection().setTranslucent(true);
        fullLightData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:torch"), fullLightData, BlockFamilyDefinition.class);

        BlockFamilyDefinitionData weakLightData = new BlockFamilyDefinitionData();
        weakLightData.getBaseSection().setDisplayName("PartLight");
        weakLightData.getBaseSection().setLuminance((byte) 2);
        weakLightData.getBaseSection().setTranslucent(true);
        weakLightData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:weakLight"), weakLightData, BlockFamilyDefinition.class);

        BlockFamilyDefinitionData mediumLightData = new BlockFamilyDefinitionData();
        mediumLightData.getBaseSection().setDisplayName("MediumLight");
        mediumLightData.getBaseSection().setLuminance((byte) 5);
        mediumLightData.getBaseSection().setTranslucent(true);
        mediumLightData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:mediumLight"), mediumLightData, BlockFamilyDefinition.class);

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);

        BlockFamilyDefinitionData solidMediumLightData = new BlockFamilyDefinitionData();
        solidMediumLightData.getBaseSection().setDisplayName("SolidMediumLight");
        solidMediumLightData.getBaseSection().setTranslucent(false);
        solidMediumLightData.getBaseSection().setLuminance((byte) 5);
        solidMediumLightData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:solidMediumLight"), solidMediumLightData, BlockFamilyDefinition.class);

        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager, true);
        blockManager.initialise(Collections.EMPTY_LIST, Collections.EMPTY_MAP);
        CoreRegistry.put(BlockManager.class, blockManager);

        fullLight = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:torch")));
        weakLight = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:weakLight")));
        mediumLight = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:mediumLight")));
        solid = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));
        solidMediumLight = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:solidMediumLight")));
        air = blockManager.getBlock(BlockManager.AIR_ID);
    }

    @Test
    public void testAddLightInVacuum() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);

        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        assertEquals(fullLight.getLuminance(), worldView.getValueAt(new Vector3i(0, 0, 0)));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(0, 1, 0)));
        assertEquals(fullLight.getLuminance() - 14, worldView.getValueAt(new Vector3i(0, 14, 0)));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(fullLight.getLuminance() - i, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testRemoveLightInVacuum() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        worldView.setBlockAt(new Vector3i(0, 0, 0), air);
        propagator.process(new BlockChange(ZERO_VECTOR, fullLight, air));

        assertEquals(0, worldView.getValueAt(new Vector3i(0, 0, 0)));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(0, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testReduceLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        worldView.setBlockAt(new Vector3i(0, 0, 0), weakLight);
        propagator.process(new BlockChange(ZERO_VECTOR, fullLight, weakLight));

        assertEquals(weakLight.getLuminance(), worldView.getValueAt(new Vector3i(0, 0, 0)));
        for (int i = 1; i < 15; ++i) {
            byte expectedLuminance = (byte) Math.max(0, weakLight.getLuminance() - i);
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testAddOverlappingLights() {
        Vector3i lightPos = new Vector3i(5, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(Chunks.CHUNK_REGION, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        worldView.setBlockAt(lightPos, fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight), new BlockChange(lightPos, air, fullLight));

        assertEquals(fullLight.getLuminance(), worldView.getValueAt(new Vector3i(0, 0, 0)));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(fullLight.getLuminance() - 2, worldView.getValueAt(new Vector3i(2, 0, 0)));
        assertEquals(fullLight.getLuminance() - 2, worldView.getValueAt(new Vector3i(3, 0, 0)));
        assertEquals(fullLight.getLuminance() - 1, worldView.getValueAt(new Vector3i(4, 0, 0)));
        assertEquals(fullLight.getLuminance(), worldView.getValueAt(new Vector3i(5, 0, 0)));
    }

    @Test
    public void testRemoveOverlappingLight() {
        Vector3i lightPos = new Vector3i(5, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        worldView.setBlockAt(lightPos, fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight), new BlockChange(lightPos, air, fullLight));

        worldView.setBlockAt(lightPos, air);
        propagator.process(new BlockChange(lightPos, fullLight, air));

        for (int i = 0; i < 16; ++i) {
            byte expectedLuminance = (byte) Math.max(0, fullLight.getLuminance() - i);
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testRemoveLightOverlappingAtEdge() {
        Vector3i lightPos = new Vector3i(2, 0, 0);

        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), weakLight);
        worldView.setBlockAt(lightPos, weakLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, weakLight), new BlockChange(lightPos, air, weakLight));

        worldView.setBlockAt(lightPos, air);
        propagator.process(new BlockChange(lightPos, weakLight, air));

        for (int i = 0; i < weakLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) Math.max(0, weakLight.getLuminance() - i);
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testAddLightInLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(2, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(2, 0, 0), air, mediumLight));

        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) Math.max(0, fullLight.getLuminance() - i);
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testAddAdjacentLights() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(1, 0, 0), mediumLight);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, mediumLight), new BlockChange(ZERO_VECTOR, air
                , mediumLight));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                long dist = Math.min(new Vector3i(0, 0, 0).gridDistance(pos), new Vector3i(1, 0, 0).gridDistance(pos));
                byte expectedLuminance = (byte) Math.max(mediumLight.getLuminance() - dist, 0);
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testAddWeakLightNextToStrongLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), weakLight);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, weakLight));
        assertEquals(14, worldView.getValueAt(new Vector3i(1, 0, 0)));
    }

    @Test
    public void testRemoveAdjacentLights() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        worldView.setBlockAt(new Vector3i(1, 0, 0), mediumLight);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, mediumLight), new BlockChange(ZERO_VECTOR, air
                , mediumLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), mediumLight, air), new BlockChange(ZERO_VECTOR,
                mediumLight, air));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) 0;
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }

    @Test
    public void testAddSolidBlocksLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(Chunks.CHUNK_REGION, air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), mediumLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, mediumLight));

        worldView.setBlockAt(new Vector3i(1, 0, 0), solid);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), air, solid));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(1, worldView.getValueAt(new Vector3i(2, 0, 0)));
    }

    @Test
    public void testRemoveSolidAllowsLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        for (Vector3ic pos : new BlockRegion(1, 0, 0).expand(0, 30, 30)) {
            worldView.setBlockAt(new Vector3i(pos), solid);
        }
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), solid, air));

        assertEquals(14, worldView.getValueAt(new Vector3i(1, 0, 0)));
        assertEquals(13, worldView.getValueAt(new Vector3i(2, 0, 0)));
    }

    @Test
    public void testRemoveSolidAndLight() {
        StubPropagatorWorldView worldView = new StubPropagatorWorldView(testingRegion, air);
        for (Vector3ic pos : new BlockRegion(1, 0, 0).expand(0, 30, 30)) {
            worldView.setBlockAt(new Vector3i(pos), solid);
        }
        worldView.setBlockAt(new Vector3i(0, 0, 0), fullLight);
        BatchPropagator propagator = new StandardBatchPropagator(lightRules, worldView);
        propagator.process(new BlockChange(ZERO_VECTOR, air, fullLight));

        assertEquals(0, worldView.getValueAt(new Vector3i(1, 0, 0)));

        worldView.setBlockAt(new Vector3i(1, 0, 0), air);
        worldView.setBlockAt(new Vector3i(0, 0, 0), air);
        propagator.process(new BlockChange(new Vector3i(1, 0, 0), solid, air), new BlockChange(ZERO_VECTOR, fullLight
                , air));

        for (int i = 0; i < fullLight.getLuminance() + 1; ++i) {
            byte expectedLuminance = (byte) 0;
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(0, 0, 0), i).build()) {
                assertEquals(expectedLuminance, worldView.getValueAt(pos));
            }
        }
    }
}
