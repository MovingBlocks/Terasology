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
package org.terasology.world;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.SymmetricBlockFamilyFactory;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.block.tiles.NullWorldAtlas;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.internal.ChunkImpl;

import static org.junit.Assert.assertEquals;


public class ChunkTest extends TerasologyTestingEnvironment {

    private Chunk chunk;
    private BlockManagerImpl blockManager;
    private Block solid;

    @Before
    public void setup() throws Exception {
        super.setup();
        AssetManager assetManager = CoreRegistry.get(AssetManager.class);
        blockManager = new BlockManagerImpl(new NullWorldAtlas(), assetManager);
        CoreRegistry.put(BlockManager.class, blockManager);

        BiomeManager biomeManager = Mockito.mock(BiomeManager.class);

        chunk = new ChunkImpl(new Vector3i(0, 0, 0), blockManager, biomeManager);

        BlockFamilyDefinitionData solidData = new BlockFamilyDefinitionData();
        solidData.getBaseSection().setDisplayName("Stone");
        solidData.getBaseSection().setShape(assetManager.getAsset("engine:cube", BlockShape.class).get());
        solidData.getBaseSection().setTranslucent(false);
        solidData.setFamilyFactory(new SymmetricBlockFamilyFactory());
        assetManager.loadAsset(new ResourceUrn("engine:stone"), solidData, BlockFamilyDefinition.class);
        solid = blockManager.getBlock(new BlockUri(new ResourceUrn("engine:stone")));
    }

    @Test
    public void testChangeBlock() {
        chunk.setBlock(new Vector3i(1, 2, 3), solid);
        assertEquals(solid, chunk.getBlock(new Vector3i(1, 2, 3)));
    }

    @Test
    public void getAABB() {
        assertEquals(new Vector3f(0, 0, 0), chunk.getAABB().getMin());
        assertEquals(new Vector3f(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z), chunk.getAABB().getMax());
    }

}

