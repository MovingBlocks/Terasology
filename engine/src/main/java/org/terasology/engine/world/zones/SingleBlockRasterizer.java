// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.zones;

import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.context.annotation.API;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;

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
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        for (int chunkX = 0; chunkX < chunk.getChunkSizeX(); chunkX++) {
            for (int chunkZ = 0; chunkZ < chunk.getChunkSizeZ(); chunkZ++) {
                for (int chunkY = 0; chunkY < chunk.getChunkSizeY(); chunkY++) {
                    chunk.setBlock(chunkX, chunkY, chunkZ, block);
                }
            }
        }
    }
}
