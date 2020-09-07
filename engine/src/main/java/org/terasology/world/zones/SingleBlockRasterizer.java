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
package org.terasology.world.zones;

import org.terasology.gestalt.module.sandbox.API;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

/**
 * A rasterizer that will fill the whole area with the given block.
 *
 * This can be used to block-fill a {@link Zone} with the block.
 */
@API
public class SingleBlockRasterizer implements WorldRasterizer {

    private final String blockUri;
    private Block block;
    private BlockManager blockManager;

    public SingleBlockRasterizer(String blockUri) {
        this.blockUri = blockUri;
    }

    @Override
    public void initialize() {
        blockManager = CoreRegistry.get(BlockManager.class);
        block = blockManager.getBlock(blockUri);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        for (int chunkX = 0; chunkX < chunk.getChunkSizeX(); chunkX++) {
            for (int chunkZ = 0; chunkZ < chunk.getChunkSizeZ(); chunkZ++) {
                for (int chunkY = 0; chunkY < chunk.getChunkSizeY(); chunkY++) {
                    chunk.setBlock(chunkX, chunkY, chunkZ, block);
                }
            }
        }
    }
}
