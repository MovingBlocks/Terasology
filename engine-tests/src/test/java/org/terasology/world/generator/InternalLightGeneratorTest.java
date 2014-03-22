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

package org.terasology.world.generator;

import org.junit.Before;
import org.junit.Test;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.math.Diamond3iIterator;
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
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;

import static org.junit.Assert.assertEquals;

/**
 * @author Immortius
 */
public class InternalLightGeneratorTest extends TerasologyTestingEnvironment {

    Block airBlock;
    Block solidBlock;
    Block fullLight;

    @Before
    public void setup() throws Exception {
        super.setup();
        BlockManagerImpl blockManager = new BlockManagerImpl(new NullWorldAtlas(), new DefaultBlockFamilyFactoryRegistry());
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

        fullLight = new Block();
        fullLight.setDisplayName("Torch");
        fullLight.setUri(new BlockUri("engine:torch"));
        fullLight.setId((byte) 2);
        fullLight.setLuminance(ChunkConstants.MAX_LIGHT);
        blockManager.addBlockFamily(new SymmetricFamily(fullLight.getURI(), fullLight), true);
    }

    @Test
    public void unblockedSunlightRegenPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z))) {
            byte expectedRegen = (byte) Math.min(ChunkConstants.SIZE_Y - pos.y - 1, ChunkConstants.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void blockedSunlightRegenPropagationResets() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 60, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 61, 0), new Vector3i(ChunkConstants.SIZE_X, 3, ChunkConstants.SIZE_Z))) {
            byte expectedRegen = (byte) Math.min(ChunkConstants.SIZE_Y - pos.y - 1, ChunkConstants.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 60, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            assertEquals(0, chunk.getSunlightRegen(pos));
        }
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 59, ChunkConstants.SIZE_Z))) {
            byte expectedRegen = (byte) Math.min(60 - pos.y - 1, ChunkConstants.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void blockedAtTopSunlightRegenPropagationResets() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 63, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y - 1, ChunkConstants.SIZE_Z))) {
            byte expectedRegen = (byte) Math.min(ChunkConstants.SIZE_Y - pos.y - 2, ChunkConstants.MAX_SUNLIGHT_REGEN);
            assertEquals(expectedRegen, chunk.getSunlightRegen(pos));
        }
    }

    @Test
    public void unblockedSunlightPropagationAfterHittingMaxRegen() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 15, 0), new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y - 15,
                ChunkConstants.SIZE_Z))) {
            assertEquals(0, chunk.getSunlight(pos));
        }

        for (Vector3i pos : Region3i.createFromMinAndSize(Vector3i.zero(), new Vector3i(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y - ChunkConstants.MAX_SUNLIGHT_REGEN,
                ChunkConstants.SIZE_Z))) {
            byte expectedSunlight = (byte) Math.min(ChunkConstants.SIZE_Y - ChunkConstants.SUNLIGHT_REGEN_THRESHOLD - pos.y - 1, ChunkConstants.MAX_SUNLIGHT);
            assertEquals("Incorrect lighting at " + pos, expectedSunlight, chunk.getSunlight(pos));
        }
    }

    @Test
    public void blockedSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 4, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
            chunk.setBlock(pos, solidBlock);
        }
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 5,
                ChunkConstants.SIZE_Z))) {
            assertEquals("Incorrect lighting at " + pos, 0, chunk.getSunlight(pos));
        }
    }

    @Test
    public void unblockedSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        InternalLightProcessor.generateInternalLighting(chunk);

        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 0, 0), new Vector3i(ChunkConstants.SIZE_X, 15,
                ChunkConstants.SIZE_Z))) {
            assertEquals("Incorrect lighting at " + pos, 15 - pos.y, chunk.getSunlight(pos));
        }
    }

    @Test
    public void horizontalSunlightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        for (Vector3i pos : Region3i.createFromMinAndSize(new Vector3i(0, 4, 0), new Vector3i(ChunkConstants.SIZE_X, 1, ChunkConstants.SIZE_Z))) {
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
    public void lightPropagation() {
        Chunk chunk = new ChunkImpl(0, 0, 0);
        chunk.setBlock(16, 32, 16, fullLight);

        InternalLightProcessor.generateInternalLighting(chunk);
        assertEquals(fullLight.getLuminance(), chunk.getLight(16, 32, 16));
        assertEquals(fullLight.getLuminance() - 1, chunk.getLight(new Vector3i(16, 33, 16)));
        for (int i = 1; i < fullLight.getLuminance(); ++i) {
            for (Vector3i pos : Diamond3iIterator.iterateAtDistance(new Vector3i(16, 32, 16), i)) {
                assertEquals(fullLight.getLuminance() - i, chunk.getLight(pos));
            }
        }
    }

}
