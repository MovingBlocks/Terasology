// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.fixtures;

import org.terasology.engine.SimpleUri;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.World;
import org.terasology.world.generator.WorldConfigurator;
import org.terasology.world.generator.WorldGenerator;

public class TestWorldGenerator implements WorldGenerator {
    private final BlockManager blockManager;

    public TestWorldGenerator(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @Override
    public SimpleUri getUri() {
        return null;
    }

    @Override
    public String getWorldSeed() {
        return null;
    }

    @Override
    public void setWorldSeed(String seed) {

    }

    @Override
    public void createChunk(CoreChunk chunk, EntityBuffer buffer) {
        for (int x = 0; x < chunk.getChunkSizeX(); x++) {
            for (int y = 0; y < chunk.getChunkSizeY(); y++) {
                for (int z = 0; z < chunk.getChunkSizeZ(); z++) {
                    chunk.setBlock(x, y, z, blockManager.getBlock(BlockManager.AIR_ID));
                }
            }
        }
    }

    @Override
    public void initialize() {

    }

    @Override
    public WorldConfigurator getConfigurator() {
        return null;
    }

    @Override
    public World getWorld() {
        return null;
    }
}
