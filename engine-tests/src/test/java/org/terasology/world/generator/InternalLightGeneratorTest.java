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
package org.terasology.world.generator;

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
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InternalLightGeneratorTest extends TerasologyTestingEnvironment {

    Block airBlock;
    Block solidBlock;
    Block fullLight;

    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;


    @BeforeEach
    public void setup() throws Exception {
        super.setup();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);
        airBlock = blockManager.getBlock(BlockManager.AIR_ID);


        extraDataManager = new ExtraBlockDataManager();

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solidBlock = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));

        BlockFamilyDefinitionData fullLightData = new BlockFamilyDefinitionData();
        fullLightData.getBaseSection().setDisplayName("Torch");
        fullLightData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        fullLightData.getBaseSection().setLuminance(ChunkConstants.MAX_LIGHT);
        fullLightData.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("engine:torch"), fullLightData, BlockFamilyDefinition.class);
        fullLight = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:torch")));
    }

    @Test
    public void testUnblockedSunlightRegenPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0,0,0).setSize(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z)) {
            byte expectedRegen = (byte) Math.min(Chunks.SIZE_Y - pos.y() - 1, Chunks.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void testBlockedSunlightRegenPropagationResets() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        for (Vector3ic pos : new BlockRegion(0, 60, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0, 61, 0).setSize(Chunks.SIZE_X, 3, Chunks.SIZE_Z)) {
            byte expectedRegen = (byte) Math.min(Chunks.SIZE_Y - pos.y() - 1, Chunks.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
        for (Vector3ic pos : new BlockRegion(0, 60, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            assertEquals(0, chunk.getSunlightRegen(pos));
        }
        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 59, Chunks.SIZE_Z)) {
            byte expectedRegen = (byte) Math.min(60 - pos.y() - 1, Chunks.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void testBlockedAtTopSunlightRegenPropagationResets() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        for (Vector3ic pos : new BlockRegion(0, 63, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0,0,0).setSize(Chunks.SIZE_X, Chunks.SIZE_Y - 1, Chunks.SIZE_Z)) {
            byte expectedRegen = (byte) Math.min(Chunks.SIZE_Y - pos.y() - 2, Chunks.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void testUnblockedSunlightPropagationAfterHittingMaxRegen() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0, 15, 0).setSize(Chunks.SIZE_X, Chunks.SIZE_Y - 15,
            Chunks.SIZE_Z)) {
            assertEquals(0, chunk.getSunlight(pos));
        }

        for (Vector3ic pos : new BlockRegion(0,0,0).setSize(Chunks.SIZE_X, Chunks.SIZE_Y - Chunks.MAX_SUNLIGHT_REGEN,
            Chunks.SIZE_Z)) {
            byte expectedSunlight = (byte) Math.min(Chunks.SIZE_Y - Chunks.SUNLIGHT_REGEN_THRESHOLD - pos.y() - 1, Chunks.MAX_SUNLIGHT);
            assertEquals(expectedSunlight, chunk.getSunlight(pos), () -> "Incorrect lighting at " + pos);
        }
    }

    @Test
    public void testBlockedSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        for (Vector3ic pos : new BlockRegion(0, 4, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 5,
            Chunks.SIZE_Z)) {
            assertEquals(0, chunk.getSunlight(pos), () -> "Incorrect lighting at " + pos);
        }
    }

    @Test
    public void testUnblockedSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3ic pos : new BlockRegion(0, 0, 0).setSize(Chunks.SIZE_X, 15,
            Chunks.SIZE_Z)) {
            assertEquals(15 - pos.y(), chunk.getSunlight(pos), () -> "Incorrect lighting at " + pos);
        }
    }

    @Test
    public void testHorizontalSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        for (Vector3ic pos : new BlockRegion(0, 4, 0).setSize(Chunks.SIZE_X, 1, Chunks.SIZE_Z)) {
            chunk.setBlock(pos, solidBlock);
        }
        chunk.setBlock(new Vector3i(16, 4, 16), airBlock);
        InternalLightProcessor.generateInternalLighting(chunk);

        assertEquals(12, chunk.getSunlight(16, 3, 16));
        assertEquals(11, chunk.getSunlight(15, 3, 16));
        assertEquals(11, chunk.getSunlight(17, 3, 16));
        assertEquals(11, chunk.getSunlight(16, 3, 15));
        assertEquals(11, chunk.getSunlight(16, 3, 17));

        assertEquals(12, chunk.getSunlight(15, 2, 16));
        assertEquals(12, chunk.getSunlight(17, 2, 16));
        assertEquals(12, chunk.getSunlight(16, 2, 15));
        assertEquals(12, chunk.getSunlight(16, 2, 17));
    }

    @Test
    public void testLightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0, blockManager, extraDataManager);
        chunk.setBlock(16, 32, 16, fullLight);

        InternalLightProcessor.generateInternalLighting(chunk);
        assertEquals(fullLight.getLuminance(), chunk.getLight(16, 32, 16));
        assertEquals(fullLight.getLuminance() - 1, chunk.getLight(16, 33, 16));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3ic pos : Diamond3iIterable.shell(new Vector3i(16, 32, 16), i).build()) {
                assertEquals(fullLight.getLuminance() - i, chunk.getLight(pos));
            }
        }
    }

}
