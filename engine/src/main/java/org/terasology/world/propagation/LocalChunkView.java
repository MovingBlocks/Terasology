// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;

/**
 * Provides a simple view over some chunks using a propagation rule.
 */
public class LocalChunkView implements PropagatorWorldView {
    private static final Logger logger = LoggerFactory.getLogger(LocalChunkView.class);

    private PropagationRules rules;
    private Chunk[] chunks;

    private final Vector3i topLeft;

    public LocalChunkView(Chunk[] chunks, PropagationRules rules) {
        this.chunks = chunks;
        this.rules = rules;
        topLeft = chunks[0].getPosition();
    }

    /**
     * Gets the index of the chunk in {@link #chunks}
     *
     * @param blockPos The position of the block in world coordinates
     * @return The index of the chunk in the array
     */
    private int chunkIndexOf(Vector3i blockPos) {
        return ChunkMath.calcChunkPos(blockPos.x, ChunkConstants.POWER_X) - topLeft.x
                + 3 * (ChunkMath.calcChunkPos(blockPos.y, ChunkConstants.POWER_Y) - topLeft.y
                + 3 * (ChunkMath.calcChunkPos(blockPos.z, ChunkConstants.POWER_Z) - topLeft.z));
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        int index = chunkIndexOf(pos);
        if (index < 0) {
            return UNAVAILABLE;
        }
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return rules.getValue(chunk, ChunkMath.calcRelativeBlockPos(pos));
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        Chunk chunk = chunks[chunkIndexOf(pos)];
        if (chunk != null) {
            rules.setValue(chunk, ChunkMath.calcRelativeBlockPos(pos), value);
        }
    }

    @Override
    public Block getBlockAt(Vector3i pos) {
        int index = chunkIndexOf(pos);
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return chunk.getBlock(ChunkMath.calcRelativeBlockPos(pos));
        }
        return null;
    }
}
