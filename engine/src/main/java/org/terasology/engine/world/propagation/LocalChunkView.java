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
            return rules.getValue(chunk, Chunks.toRelative(pos, new Vector3i()));
        }
        return UNAVAILABLE;
    }

    @Override
    public void setValueAt(Vector3ic pos, byte value) {
        Chunk chunk = chunks[chunkIndexOf(pos)];
        if (chunk != null) {
            Vector3i relative = Chunks.toRelative(pos, new Vector3i());
            rules.setValue(chunk, relative, value);
            chunk.setDirty(true);
            // Only a write within a block of a face can affect a neighbour's mesh, and in a 32x64x32
            // chunk the overwhelming majority of writes are interior. This is the innermost loop of
            // batch propagation - running the neighbourhood scan unconditionally here costs real
            // frame time for nothing, so it is guarded by six comparisons on a value already needed
            // for the write above.
            if (isOnChunkBoundary(relative)) {
                markDirtyAcrossBoundary(pos);
            }
        }
    }

    private static boolean isOnChunkBoundary(Vector3ic relative) {
        return relative.x() == 0 || relative.x() == Chunks.SIZE_X - 1
                || relative.y() == 0 || relative.y() == Chunks.SIZE_Y - 1
                || relative.z() == 0 || relative.z() == Chunks.SIZE_Z - 1;
    }

    /**
     * Mark the neighbouring chunks whose mesh depends on a light value written on a shared boundary,
     * matching what {@link AbstractFullWorldView#setValueAt(Vector3ic, byte)} does for runtime block
     * changes. Only called for boundary writes; see {@link #setValueAt}.
     * <p>
     * Marking just the chunk holding {@code pos} is not enough. A chunk's mesh samples its
     * neighbours' light to shade the faces along the shared boundary, so a write one block inside
     * chunk A can leave chunk B's mesh stale. Propagation does not necessarily fix that by writing
     * into B as well - if B's side of the boundary is opaque no light is written there at all, yet
     * B's mesh is exactly what shades that solid face. That is the ordinary case at a terrain
     * surface.
     * <p>
     * Mostly this only shortens the life of a transient artifact, since B is corrected anyway once it
     * is merged as a centre in its own right. It matters at the edge of the loaded world, where a
     * chunk whose neighbourhood never completes is never merged as a centre and would otherwise keep
     * a stale face indefinitely - which is precisely the case this whole change exists to handle.
     * <p>
     * The expansion is one block and no more: a write reaches at most 2, 4 or 8 chunks. Marking all
     * 27 speculatively from the caller instead is what exhausted the heap in mesh generation.
     */
    private void markDirtyAcrossBoundary(Vector3ic pos) {
        // Indices relative to this view, rather than a BlockRegion per write as AbstractFullWorldView
        // builds - this runs per light value written, inside batch propagation.
        int minX = Math.max(Chunks.toChunkPos(pos.x() - 1, Chunks.POWER_X) - topLeft.x, 0);
        int maxX = Math.min(Chunks.toChunkPos(pos.x() + 1, Chunks.POWER_X) - topLeft.x, 2);
        int minY = Math.max(Chunks.toChunkPos(pos.y() - 1, Chunks.POWER_Y) - topLeft.y, 0);
        int maxY = Math.min(Chunks.toChunkPos(pos.y() + 1, Chunks.POWER_Y) - topLeft.y, 2);
        int minZ = Math.max(Chunks.toChunkPos(pos.z() - 1, Chunks.POWER_Z) - topLeft.z, 0);
        int maxZ = Math.min(Chunks.toChunkPos(pos.z() + 1, Chunks.POWER_Z) - topLeft.z, 2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Chunk affected = chunks[x + 3 * (y + 3 * z)];
                    if (affected != null) {
                        affected.setDirty(true);
                    }
                }
            }
        }
    }

    @Override
    public Block getBlockAt(Vector3ic pos) {
        int index = chunkIndexOf(pos);
        Chunk chunk = chunks[index];
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, new Vector3i()));
        }
        return null;
    }
}
