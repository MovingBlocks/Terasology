// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;

/**
 * Provides a view over a single chunk using a given propagation rule.
 */
public class SingleChunkView implements PropagatorWorldView {

    private final PropagationRules rules;
    private final Chunk chunk;

    public SingleChunkView(PropagationRules rules, Chunk chunk) {
        this.rules = rules;
        this.chunk = chunk;
    }

    @Override
    public byte getValueAt(Vector3ic pos) {
        if (Chunks.CHUNK_REGION.contains(pos)) {
            return rules.getValue(chunk, pos);
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        rules.setValue(chunk, pos, value);
    }

    @Override
    public Block getBlockAt(Vector3ic pos) {
        if (Chunks.CHUNK_REGION.contains(pos)) {
            return chunk.getBlock(pos);
        }
        return null;
    }
}
