// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.propagation;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;

/**
 * Provides a simple view over some chunks using a propagation rule.
 */
public class LocalChunkView implements PropagatorWorldView {

    /** This view spans a fixed 3x3x3 neighbourhood of chunks. */
    private static final int LOCAL_CHUNKS_SIDE_LENGTH = 3;

    private PropagationRules rules;
    private Chunk[] chunks;

    private final Vector3i topLeft = new Vector3i();

    public LocalChunkView(Chunk[] chunks, PropagationRules rules) {
        this.chunks = chunks;
        this.rules = rules;
        topLeft.set(chunks[0].getPosition());
    }

    /**
     * Gets the index of the chunk in {@link #chunks}
     *
     * @param blockPos The position of the block in world coordinates
     * @return The index of the chunk in the array, or -1 if the position lies outside this view
     */
    private int chunkIndexOf(Vector3ic blockPos) {
        return indexOf(
                Chunks.toChunkPos(blockPos.x(), Chunks.POWER_X) - topLeft.x,
                Chunks.toChunkPos(blockPos.y(), Chunks.POWER_Y) - topLeft.y,
                Chunks.toChunkPos(blockPos.z(), Chunks.POWER_Z) - topLeft.z);
    }

    /**
     * Index into {@link #chunks} for a chunk offset within this view, or -1 if outside it.
     * <p>
     * The stride order has to match how the caller laid the array out. {@code LightMerger.merge} sorts
     * by x, then y, then z before constructing this view, so x is the slowest-varying coordinate and z
     * the fastest - the same order its own {@code indexOf(Side)} assumes. This previously used the
     * opposite convention, making x fastest, which silently transposed x and z: a lookup for the +X
     * neighbour returned the +Z one. Nothing failed, because reads and writes shared the same wrong
     * mapping and the centre (1,1,1) is invariant under the swap - so light merging simply propagated
     * into the wrong neighbours.
     * <p>
     * Bounds are checked per axis rather than on the flat index. Clamping the index alone is not
     * enough: an offset like (3, 0, 0) is out of view but still lands inside the array, aliasing onto
     * a real but unrelated chunk.
     */
    private static int indexOf(int chunkX, int chunkY, int chunkZ) {
        if (chunkX < 0 || chunkX >= LOCAL_CHUNKS_SIDE_LENGTH
                || chunkY < 0 || chunkY >= LOCAL_CHUNKS_SIDE_LENGTH
                || chunkZ < 0 || chunkZ >= LOCAL_CHUNKS_SIDE_LENGTH) {
            return -1;
        }
        return chunkZ + LOCAL_CHUNKS_SIDE_LENGTH * (chunkY + LOCAL_CHUNKS_SIDE_LENGTH * chunkX);
    }

    @Override
    public byte getValueAt(Vector3ic pos) {
        int index = chunkIndexOf(pos);
        if (index < 0) {
            return UNAVAILABLE;
        }
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return rules.getValue(chunk, Chunks.toRelative(pos, new Vector3i()));
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        int index = chunkIndexOf(pos);
        if (index < 0) {
            // Propagation routinely reaches one step past the edge of this view; silently dropping
            // it here matches getValueAt's UNAVAILABLE for the same case, rather than the
            // ArrayIndexOutOfBoundsException this used to throw. Not logged: this is the ordinary
            // case at every boundary of every merge, not a fault to surface.
            return;
        }
        Chunk chunk = chunks[index];
        if (chunk != null) {
            rules.setValue(chunk, Chunks.toRelative(pos, new Vector3i()), value);
            // Mark only what actually changed, as AbstractFullWorldView does for runtime block
            // changes. This used to mark nothing: light merging ran before a chunk was ready, so its
            // first mesh was still to come and picked the result up for free. Now that merging
            // happens on live chunks, the caller would otherwise have to mark whole neighbourhoods
            // dirty speculatively - which re-meshes chunks whose light never moved, and a chunk sits
            // in 27 different neighbourhoods.
            chunk.setDirty(true);
        }
    }

    @Override
    public Block getBlockAt(Vector3ic pos) {
        int index = chunkIndexOf(pos);
        if (index < 0) {
            return null;
        }
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, new Vector3i()));
        }
        return null;
    }
}
