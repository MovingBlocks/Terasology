// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;

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
        Chunk chunk = getChunk(pos);
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
    protected abstract byte getValueAt(Chunk chunk, Vector3ic pos);

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
    protected abstract void setValueAt(Chunk chunk, Vector3ic pos, byte value);

    @Override
    public Block getBlockAt(Vector3ic pos) {
       Chunk chunk = chunkProvider.getChunk(Chunks.toChunkPos(pos, new Vector3i()));
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, new Vector3i()));
        }
        return null;
    }

}
