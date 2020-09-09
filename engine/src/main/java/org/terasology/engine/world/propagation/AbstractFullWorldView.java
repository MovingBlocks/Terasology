// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.terasology.engine.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.chunks.LitChunk;

/**
 * A base world view implementation sitting on ChunkProvider.
 */
public abstract class AbstractFullWorldView implements PropagatorWorldView {

    private final ChunkProvider chunkProvider;

    public AbstractFullWorldView(ChunkProvider chunkProvider) {
        this.chunkProvider = chunkProvider;
    }

    /**
     * Get's the chunk for a given position
     *
     * @param pos The position in the world
     * @return The chunk for that position
     */
    private Chunk getChunk(Vector3i pos) {

        return chunkProvider.getChunk(ChunkMath.calcChunkPos(pos));
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        LitChunk chunk = getChunk(pos);
        if (chunk != null) {
            return getValueAt(chunk, ChunkMath.calcRelativeBlockPos(pos.x, pos.y, pos.z));
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
    protected abstract byte getValueAt(LitChunk chunk, Vector3i pos);

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        setValueAt(getChunk(pos), ChunkMath.calcRelativeBlockPos(pos.x, pos.y, pos.z), value);
        for (Vector3i affectedChunkPos : ChunkMath.getChunkRegionAroundWorldPos(pos, 1)) {
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
    protected abstract void setValueAt(LitChunk chunk, Vector3i pos, byte value);

    @Override
    public Block getBlockAt(Vector3i pos) {
        CoreChunk chunk = chunkProvider.getChunk(ChunkMath.calcChunkPos(pos));
        if (chunk != null) {
            return chunk.getBlock(ChunkMath.calcRelativeBlockPos(pos));
        }
        return null;
    }

}
