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

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;

/**
 * A base world view implementation sitting on ChunkProvider.
 * @author Immortius
 */
public abstract class AbstractFullWorldView implements PropagatorWorldView {

    private ChunkProvider chunkProvider;

    public AbstractFullWorldView(ChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    private Chunk getChunk(Vector3i pos) {

        return chunkProvider.getChunk(TeraMath.calcChunkPos(pos));
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        if (pos.y < 0 || pos.y >= Chunk.SIZE_Y) {
            return UNAVAILABLE;
        }
        Chunk chunk = getChunk(pos);
        if (chunk != null) {
            return getValueAt(chunk, TeraMath.calcBlockPos(pos.x, pos.y, pos.z));
        }
        return UNAVAILABLE;
    }

    /**
     * Obtains the relevant value from the given chunk
     * @param chunk
     * @param pos The internal position of the chunk to get the value from
     * @return The relevant value for this view
     */
    protected abstract byte getValueAt(Chunk chunk, Vector3i pos);

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        setValueAt(getChunk(pos), TeraMath.calcBlockPos(pos.x, pos.y, pos.z), value);
        for (Vector3i affectedChunkPos : TeraMath.getChunkRegionAroundWorldPos(pos, 1)) {
            Chunk dirtiedChunk = chunkProvider.getChunk(affectedChunkPos);
            if (dirtiedChunk != null) {
                dirtiedChunk.setDirty(true);
            }
        }
    }

    /**
     * Sets the relevant value for the given chunk
     * @param chunk
     * @param pos The internal position of the chunk to set the value of
     * @param value The new value
     */
    protected abstract void setValueAt(Chunk chunk, Vector3i pos, byte value);

    @Override
    public Block getBlockAt(Vector3i pos) {
        if (pos.y < 0 || pos.y >= Chunk.SIZE_Y) {
            return null;
        }
        Chunk chunk = chunkProvider.getChunk(TeraMath.calcChunkPos(pos));
        if (chunk != null) {
            return chunk.getBlock(TeraMath.calcBlockPos(pos));
        }
        return null;
    }

}
