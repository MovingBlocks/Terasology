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

import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.internal.ChunkViewCore;

/**
 * Intermediate abstract class for the propagater world view that handles common functionality.
 * <p>
 * Only provides a view for a single chunk
 *
 * @see AbstractFullWorldView
 */
public abstract class AbstractChunkView implements PropagatorWorldView {

    private ChunkViewCore chunkView;

    public AbstractChunkView(ChunkViewCore chunkView) {
        this.chunkView = chunkView;
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        if (isInBounds(pos)) {
            return getValueAt(chunkView, pos);
        }
        return UNAVAILABLE;
    }

    /**
     * Equivalent to {@link #getValueAt(Vector3i)}
     *
     * @param view The chunk the position is within
     * @param pos  The position of to get the value at
     * @return The value of the propagating data at the given position
     */
    protected abstract byte getValueAt(ChunkViewCore view, Vector3i pos);

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        setValueAt(chunkView, pos, value);
    }

    /**
     * Equivalent to {@link #setValueAt(Vector3i, byte)}
     *
     * @param view  The chunk the position is in
     * @param pos   The position to set the value at
     * @param value The value to set the position to
     */
    protected abstract void setValueAt(ChunkViewCore view, Vector3i pos, byte value);

    @Override
    public Block getBlockAt(Vector3i pos) {
        if (isInBounds(pos)) {
            return chunkView.getBlock(JomlUtil.from(pos));
        }
        return null;
    }

    /**
     * Checks if the position is within the boundaries of the chunk represented by this class
     *
     * @param pos The position to check, in world coordinates
     */
    public boolean isInBounds(Vector3i pos) {
        return chunkView.getWorldRegion().contains(JomlUtil.from(pos));
    }
}
