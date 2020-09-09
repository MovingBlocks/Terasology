// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.LitChunk;

/**
 * Provides a view over a single chunk using a given propagation rule.
 */
public class SingleChunkView implements PropagatorWorldView {

    private final PropagationRules rules;
    private final LitChunk chunk;

    public SingleChunkView(PropagationRules rules, LitChunk chunk) {
        this.rules = rules;
        this.chunk = chunk;
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        if (ChunkConstants.CHUNK_REGION.encompasses(pos)) {
            return rules.getValue(chunk, pos);
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        rules.setValue(chunk, pos, value);
    }

    @Override
    public Block getBlockAt(Vector3i pos) {
        if (ChunkConstants.CHUNK_REGION.encompasses(pos)) {
            return chunk.getBlock(pos);
        }
        return null;
    }
}
