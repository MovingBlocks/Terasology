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
package org.terasology.world.chunks.internal;

import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 */
public class ChunkBlockIteratorImpl implements ChunkBlockIterator {

    private final Vector3i worldOffset;
    private final Vector3i endPos;
    private final Vector3i pos = new Vector3i(-1, 0, 0);

    private final TeraArray data;

    private final Vector3i blockPos = new Vector3i();
    private Block block;

    private final BlockManager blockManager;

    public ChunkBlockIteratorImpl(BlockManager blockManager, Vector3i worldOffset, TeraArray data) {
        this.blockManager = blockManager;
        this.worldOffset = worldOffset;
        this.endPos = new Vector3i(data.getSizeX(), data.getSizeY(), data.getSizeZ());
        this.data = data;
    }

    @Override
    public boolean next() {
        pos.x++;
        if (pos.x >= endPos.x) {
            pos.x = 0;
            pos.y++;
            if (pos.y >= endPos.y) {
                pos.y = 0;
                pos.z++;
                if (pos.z >= endPos.z) {
                    return false;
                }
            }
        }
        blockPos.set(pos.x + worldOffset.x, pos.y + worldOffset.y, pos.z + worldOffset.z);
        block = blockManager.getBlock((short) data.get(pos.x, pos.y, pos.z));
        return true;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public Vector3i getBlockPos() {
        return blockPos;
    }
}
