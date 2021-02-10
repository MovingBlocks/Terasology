/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.world.propagation;

import org.joml.Vector3ic;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.LitChunk;

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
