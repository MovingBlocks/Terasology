// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.internal;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkBlockIterator;

/**
 */
public class ChunkBlockIteratorImpl implements ChunkBlockIterator {

    private final Vector3i worldOffset = new Vector3i();
    private final Vector3i endPos;
    private final Vector3i pos = new Vector3i(-1, 0, 0);

    private final TeraArray data;

    private final Vector3i blockPos = new Vector3i();
    private Block block;

    private final BlockManager blockManager;

    public ChunkBlockIteratorImpl(BlockManager blockManager, Vector3ic worldOffset, TeraArray data) {
        this.blockManager = blockManager;
        this.worldOffset.set(worldOffset);
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
    public Vector3ic getBlockPos() {
        return blockPos;
    }
}
