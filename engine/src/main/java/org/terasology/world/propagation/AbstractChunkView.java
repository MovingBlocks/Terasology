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

import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

/**
 * @author Immortius
 */
public abstract class AbstractChunkView implements PropagatorWorldView {

    private ChunkView chunkView;

    public AbstractChunkView(ChunkView chunkView) {
        this.chunkView = chunkView;
    }

    @Override
    public byte getValueAt(Vector3i pos) {
        if (isInBounds(pos)) {
            return getValueAt(chunkView, pos);
        }
        return UNAVAILABLE;
    }

    protected abstract byte getValueAt(ChunkView view, Vector3i pos);

    @Override
    public void setValueAt(Vector3i pos, byte value) {
        setValueAt(chunkView, pos, value);
    }

    protected abstract void setValueAt(ChunkView view, Vector3i pos, byte value);

    @Override
    public Block getBlockAt(Vector3i pos) {
        if (isInBounds(pos)) {
            return chunkView.getBlock(pos);
        }
        return null;
    }

    public boolean isInBounds(Vector3i pos) {
        return chunkView.getWorldRegion().encompasses(pos);
    }
}
