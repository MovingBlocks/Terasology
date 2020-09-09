// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.internal;

import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkBlockIterator;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
public class ChunkBlockIteratorImpl implements ChunkBlockIterator {

    private final Vector3i worldOffset;
    private final Vector3i endPos;
    private final Vector3i pos = new Vector3i(-1, 0, 0);

    private final TeraArray data;

    private final Vector3i blockPos = new Vector3i();
    private final BlockManager blockManager;
    private Block block;

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
