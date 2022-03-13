// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.chunks.internal;

import org.joml.Vector3i;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;

/**
 * A chunk that has a full set of data, but will be turned into
 * a LOD chunk later.
 */
public class PreLodChunk extends ChunkImpl {
    public PreLodChunk(Vector3i pos, BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        super(pos, blockManager, extraDataManager);
        Vector3i min = Chunks.CHUNK_SIZE.sub(2, 4, 2, new Vector3i()).mul(pos).sub(1, 2, 1);
        region = new BlockRegion(min, min.add(Chunks.CHUNK_SIZE, new Vector3i()));
    }

    @Override
    public int getChunkWorldOffsetX() {
        return chunkPos.x() * (getChunkSizeX() - 2) - 1;
    }

    @Override
    public int getChunkWorldOffsetY() {
        return chunkPos.y() * (getChunkSizeY() - 4) - 2;
    }

    @Override
    public int getChunkWorldOffsetZ() {
        return chunkPos.z() * (getChunkSizeZ() - 2) - 1;
    }
}
