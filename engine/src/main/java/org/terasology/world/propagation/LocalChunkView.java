// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.propagation;

import org.joml.Vector3ic;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

/**
 * Provides a simple view over some chunks using a propagation rule.
 */
public class LocalChunkView implements PropagatorWorldView {

    private PropagationRules rules;
    private Chunk[] chunks;

    private final org.joml.Vector3i topLeft;

    public LocalChunkView(Chunk[] chunks, PropagationRules rules) {
        this.chunks = chunks;
        this.rules = rules;
        topLeft = chunks[0].getPosition(new org.joml.Vector3i());
    }

    /**
     * Gets the index of the chunk in {@link #chunks}
     *
     * @param blockPos The position of the block in world coordinates
     * @return The index of the chunk in the array
     */
    private int chunkIndexOf(Vector3ic blockPos) {
        return Chunks.toChunkPos(blockPos.x(), Chunks.POWER_X) - topLeft.x
                + 3 * (Chunks.toChunkPos(blockPos.y(), Chunks.POWER_Y) - topLeft.y
                + 3 * (Chunks.toChunkPos(blockPos.z(), Chunks.POWER_Z) - topLeft.z));
    }

    @Override
    public byte getValueAt(Vector3ic pos) {
        int index = chunkIndexOf(pos);
        if (index < 0) {
            return UNAVAILABLE;
        }
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return rules.getValue(chunk, Chunks.toRelative(pos, new org.joml.Vector3i()));
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        Chunk chunk = chunks[chunkIndexOf(pos)];
        if (chunk != null) {
            rules.setValue(chunk, Chunks.toRelative(pos, new org.joml.Vector3i()), value);
        }
    }

    @Override
    public Block getBlockAt(Vector3ic pos) {
        int index = chunkIndexOf(pos);
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, new org.joml.Vector3i()));
        }
        return null;
    }
}
