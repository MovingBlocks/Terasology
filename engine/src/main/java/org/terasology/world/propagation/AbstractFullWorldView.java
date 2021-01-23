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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.math.JomlUtil;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.chunks.LitChunk;

/**
 * A base world view implementation sitting on ChunkProvider.
 */
public abstract class AbstractFullWorldView implements PropagatorWorldView {

    private ChunkProvider chunkProvider;

    public AbstractFullWorldView(ChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    /**
     * Get's the chunk for a given position
     *
     * @param pos The position in the world
     * @return The chunk for that position
     */
    private Chunk getChunk(Vector3ic pos) {
        return chunkProvider.getChunk(Chunks.toChunkPos(pos, new Vector3i()));
    }

    @Override
    public byte getValueAt(Vector3ic pos) {
        LitChunk chunk = getChunk(pos);
        if (chunk != null) {
            return getValueAt(chunk, Chunks.toRelative(pos, new Vector3i()));
        }
        return UNAVAILABLE;
    }

    /**
     * Obtains the relevant value from the given chunk
     *
     * @param chunk The chunk containing the position
     * @param pos   The internal position of the chunk to get the value from
     * @return The relevant value for this view
     */
    protected abstract byte getValueAt(LitChunk chunk, Vector3ic pos);

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        setValueAt(getChunk(pos), Chunks.toRelative(pos, new Vector3i()), value);
        BlockRegion chunkRegion = new BlockRegion(pos).expand(1, 1, 1);
        for (Vector3ic affectedChunkPos : Chunks.toChunkRegion(chunkRegion, chunkRegion)) {
            Chunk dirtiedChunk = chunkProvider.getChunk(affectedChunkPos);
            if (dirtiedChunk != null) {
                dirtiedChunk.setDirty(true);
            }
        }
    }

    /**
     * Sets the relevant value for the given chunk
     *
     * @param chunk The chunk containing the position
     * @param pos   The internal position of the chunk to set the value of
     * @param value The new value
     */
    protected abstract void setValueAt(LitChunk chunk, Vector3ic pos, byte value);

    @Override
    public Block getBlockAt(Vector3ic pos) {
        CoreChunk chunk = chunkProvider.getChunk(Chunks.toChunkPos(pos, new Vector3i()));
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, new Vector3i()));
        }
        return null;
    }

}
