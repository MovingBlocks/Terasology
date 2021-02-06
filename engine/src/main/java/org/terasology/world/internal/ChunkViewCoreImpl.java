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

package org.terasology.world.internal;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.JomlUtil;
import org.terasology.math.TeraMath;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.Chunks;

/**
 */
public class ChunkViewCoreImpl implements ChunkViewCore {

    private static final Logger logger = LoggerFactory.getLogger(ChunkViewCoreImpl.class);

    private Vector3i offset = new Vector3i();
    private BlockRegion chunkRegion = new BlockRegion(BlockRegion.INVALID);
    private BlockRegion blockRegion = new BlockRegion(BlockRegion.INVALID);
    private Chunk[] chunks;

    private Vector3i chunkPower;
    private Vector3i chunkFilterSize;

    private Block defaultBlock;

    public ChunkViewCoreImpl(Chunk[] chunks, BlockRegionc chunkRegion, Vector3ic offset, Block defaultBlock) {
        this.chunkRegion.set(chunkRegion);
        this.chunks = chunks;
        this.offset.set(offset);
        setChunkSize(new Vector3i(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z));
        this.defaultBlock = defaultBlock;
    }

    @Override
    public BlockRegionc getWorldRegion() {
        return blockRegion;
    }

    @Override
    public BlockRegionc getChunkRegion() {
        return chunkRegion;
    }

    @Override
    public Block getBlock(float x, float y, float z) {
        return getBlock(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public Block getBlock(Vector3ic pos) {
        return getBlock(pos.x(), pos.y(), pos.z());
    }

    // TODO: Review
    @Override
    public Block getBlock(int blockX, int blockY, int blockZ) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                return chunk.getBlock(
                        Chunks.toRelative(blockX, chunkFilterSize.x),
                        Chunks.toRelative(blockY, chunkFilterSize.y),
                        Chunks.toRelative(blockZ, chunkFilterSize.z));
            }
        }
        return defaultBlock;
    }

    @Override
    public byte getSunlight(float x, float y, float z) {
        return getSunlight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public byte getSunlight(Vector3i pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getLight(float x, float y, float z) {
        return getLight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public byte getLight(Vector3i pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    @Override
    public byte getSunlight(int blockX, int blockY, int blockZ) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                return chunk.getSunlight(Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()));
            }
        }
        return 0;
    }

    @Override
    public byte getLight(int blockX, int blockY, int blockZ) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                return chunk.getLight(Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()));
            }
        }
        return 0;
    }

    @Override
    public void setBlock(Vector3ic pos, Block type) {
        setBlock(pos.x(), pos.y(), pos.z(), type);
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, Block type) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                chunk.setBlock(Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()), type);
                return;
            }
        }
        logger.warn("Attempt to modify block outside of the view");
    }

    @Override
    public void setLight(Vector3ic pos, byte light) {
        setLight(pos.x(), pos.y(), pos.z(), light);
    }

    @Override
    public void setLight(int blockX, int blockY, int blockZ, byte light) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                chunk.setLight(Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()), light);
                return;
            }
        }
        logger.warn("Attempted to set light at a position not encompassed by the view");
    }

    @Override
    public void setSunlight(Vector3ic pos, byte light) {
        setSunlight(pos.x(), pos.y(), pos.z(), light);
    }

    @Override
    public void setSunlight(int blockX, int blockY, int blockZ, byte light) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                chunk.setSunlight(Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()), light);
                return;
            }
        }
        throw new IllegalStateException("Attempted to set sunlight at a position not encompassed by the view");
    }

    @Override
    public int getExtraData(int index, Vector3ic pos) {
        return getExtraData(index, pos.x(), pos.y(), pos.z());
    }

    @Override
    public int getExtraData(int index, int blockX, int blockY, int blockZ) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                return chunk.getExtraData(index, Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()));
            }
        }
        return 0;
    }

    @Override
    public void setExtraData(int index, Vector3ic pos, int value) {
        setExtraData(index, pos.x(), pos.y(), pos.z(), value);
    }

    @Override
    public void setExtraData(int index, int blockX, int blockY, int blockZ, int value) {
        if (blockRegion.contains(blockX, blockY, blockZ)) {
            Chunk chunk = chunks[relChunkIndex(blockX, blockY, blockZ)];
            if (chunk != null) {
                chunk.setExtraData(index, Chunks.toRelative(blockX, blockY, blockZ, chunkFilterSize, new Vector3i()), value);
            }
        }
        throw new IllegalStateException("Attempted to modify extra data at a position not encompassed by the view");
    }

    @Override
    public void setDirtyAround(Vector3ic blockPos) {
        setDirtyAround(new BlockRegion(blockPos));
    }

    @Override
    public void setDirtyAround(BlockRegionc region) {
        BlockRegion tmp = new BlockRegion(region).expand(1, 1, 1);
        for (Vector3ic pos : Chunks.toChunkRegion(tmp, tmp)) {
            Chunk chunk = chunks[TeraMath.calculate3DArrayIndex(pos.x() + offset.x, pos.y() + offset.y, pos.z() + offset.z, JomlUtil.from(chunkRegion.getSize(new Vector3i())))];
            if (chunk != null) {
                chunk.setDirty(true);
            }
        }
    }

    @Override
    public boolean isValidView() {
        for (Chunk chunk : chunks) {
            if (chunk != null && chunk.isDisposed()) {
                return false;
            }
        }
        return true;
    }

    protected int relChunkIndex(int x, int y, int z) {
        return TeraMath.calculate3DArrayIndex(Chunks.toChunkPos(x, chunkPower.x) + offset.x,
                Chunks.toChunkPos(y, chunkPower.y) + offset.y,
                Chunks.toChunkPos(z, chunkPower.z) + offset.z, JomlUtil.from(chunkRegion.getSize(new Vector3i())));
    }

    public void setChunkSize(Vector3i chunkSize) {
        this.chunkFilterSize = new Vector3i(TeraMath.ceilPowerOfTwo(chunkSize.x) - 1,
                TeraMath.ceilPowerOfTwo(chunkSize.y) - 1,
                TeraMath.ceilPowerOfTwo(chunkSize.z) - 1);
        this.chunkPower = new Vector3i(TeraMath.sizeOfPower(chunkSize.x), TeraMath.sizeOfPower(chunkSize.y), TeraMath.sizeOfPower(chunkSize.z));

        Vector3i blockMin = new Vector3i();
        blockMin.sub(offset);
        blockMin.mul(chunkSize.x, chunkSize.y, chunkSize.z);
        Vector3i blockSize = chunkRegion.getSize(new Vector3i());
        blockSize.mul(chunkSize.x, chunkSize.y, chunkSize.z);
        this.blockRegion.setPosition(blockMin).setSize(blockSize);

    }

    @Override
    public Vector3i toWorldPos(Vector3ic localPos) {
        return new Vector3i(localPos.x() + (offset.x + chunkRegion.minX()) * Chunks.SIZE_X,
                localPos.y() + (offset.y + chunkRegion.minY()) * Chunks.SIZE_Y,
                localPos.z() + (offset.z + chunkRegion.minZ()) * Chunks.SIZE_Z);
    }
}
