/*
 * Copyright 2017 MovingBlocks
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
package org.terasology;

import org.mockito.Mockito;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.BlockFamilyDefinition;
import org.terasology.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.world.block.shapes.BlockShape;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.internal.WorldProviderWrapper;

/**
 * Environment with a MapWorldProvider and BlockManager. Useful to get headless environment with a generated world.
 * TODO: get rid of CoreRegistry usage, doublecheck block/biome manager usage
 */
public class WorldProvidingHeadlessEnvironment extends HeadlessEnvironment {
    private BlockManagerImpl blockManager;
    private AssetManager assetManager;
    private short nextBlockId = 100;

    public WorldProvidingHeadlessEnvironment(Name... modules) {
        super(modules);
    }

    public void setupWorldProvider(WorldGenerator generator) {
        generator.initialize();
        WorldProviderCore stub = new MapWorldProvider(generator, blockManager, Mockito.mock(BiomeManager.class));
        WorldProvider world = new WorldProviderWrapper(stub);
        CoreRegistry.put(WorldProvider.class, world);
        CoreRegistry.put(BlockEntityRegistry.class, new EntityAwareWorldProvider(stub, context));
    }


    public Block registerBlock(String uri, Block block, boolean penetrable) {
        block.setPenetrable(penetrable);
        return registerBlock(uri, block);
    }

    public Block registerBlock(String uri, Block block) {

        // TODO double-check this works as intended
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.getBaseSection().setShape(assetManager.getAsset(uri, BlockShape.class).get());
        block.setId(nextBlockId);
        block.setUri(new BlockUri(uri));
        assetManager.loadAsset(new ResourceUrn(uri), data, BlockFamilyDefinition.class);
        block = blockManager.getBlock(new BlockUri(uri));


        nextBlockId++;

        return block;
    }
}
