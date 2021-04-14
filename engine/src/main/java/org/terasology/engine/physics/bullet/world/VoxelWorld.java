// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.physics.bullet.world;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;

import java.nio.ShortBuffer;

public interface VoxelWorld {
    void setBlock(int x, int y, int z, Block block);
    void registerBlock(Block block);
    void loadChunk(Chunk chunk, ShortBuffer buffer);
    void unloadChunk(Vector3ic position);
}
