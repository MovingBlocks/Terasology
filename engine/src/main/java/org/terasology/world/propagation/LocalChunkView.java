/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;

/**
 * Provides a simple view over some chunks using a propagation rule.
 */
public class LocalChunkView implements PropagatorWorldView {
    private PropagationRules rules;
    private Chunk[] chunks;

    private Vector3i topLeft = new Vector3i();

    public LocalChunkView(Chunk[] chunks, PropagationRules rules) {
        this.chunks = chunks;
        this.rules = rules;
        //TODO fix this to not hardcode 13. This is a ugly smell
        topLeft.set(chunks[13].getPosition().x - 1, chunks[13].getPosition().y - 1, chunks[13].getPosition().z - 1);
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
        Chunk chunk = chunks[chunkIndexOf(pos)];
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
