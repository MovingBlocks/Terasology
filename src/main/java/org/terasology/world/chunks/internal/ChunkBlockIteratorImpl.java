package org.terasology.world.chunks.internal;

import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.blockdata.TeraArray;

/**
 * @author Immortius
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
        pos.x ++;
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
        block = blockManager.getBlock((byte) data.get(pos.x, pos.y, pos.z));
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
