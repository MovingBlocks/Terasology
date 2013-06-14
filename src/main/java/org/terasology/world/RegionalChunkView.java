/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.block.management.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.liquid.LiquidData;

/**
 * @author Immortius
 */
public class RegionalChunkView implements ChunkView {

    private static final Logger logger = LoggerFactory.getLogger(RegionalChunkView.class);

    private Vector3i offset;
    private Region3i chunkRegion;
    private Region3i blockRegion;
    private Chunk[] chunks;

    private Vector3i chunkPower;
    private Vector3i chunkSize;
    private Vector3i chunkFilterSize;

    private ThreadLocal<Boolean> locked = new ThreadLocal<Boolean>();

    public RegionalChunkView(Chunk[] chunks, Region3i chunkRegion, Vector3i offset) {
        locked.set(false);
        this.chunkRegion = chunkRegion;
        this.chunks = chunks;
        this.offset = offset;
        setChunkSize(new Vector3i(Chunk.SIZE_X, Chunk.SIZE_Y, Chunk.SIZE_Z));
    }

    @Override
    public Region3i getChunkRegion() {
        return chunkRegion;
    }

    @Override
    public Block getBlock(float x, float y, float z) {
        return getBlock(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    @Override
    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    // TODO: Review
    @Override
    public Block getBlock(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return BlockManager.getAir();
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getBlock(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize));
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
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return 0;
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getSunlight(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize));
    }

    @Override
    public byte getLight(int blockX, int blockY, int blockZ) {
        if (!blockRegion.encompasses(blockX, blockY, blockZ)) {
            return 0;
        }

        int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
        return chunks[chunkIndex].getLight(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize));
    }

    @Override
    public void setBlock(Vector3i pos, Block type) {
        setBlock(pos.x, pos.y, pos.z, type);
    }

    @Override
    public void setBlock(int blockX, int blockY, int blockZ, Block type) {
        if (!locked.get()) {
            throw new IllegalStateException("Attempted to modify block though an unlocked view");
        } else if (blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setBlock(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), type);
        } else {
            logger.warn("Attempt to modify block outside of the view");
        }
    }

    @Override
    public LiquidData getLiquid(Vector3i pos) {
        return getLiquid(pos.x, pos.y, pos.z);
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        if (!blockRegion.encompasses(x, y, z)) {
            return new LiquidData();
        }

        int chunkIndex = relChunkIndex(x, y, z);
        return chunks[chunkIndex].getLiquid(TeraMath.calcBlockPos(x, y, z, chunkFilterSize));
    }

    @Override
    public void setLiquid(Vector3i pos, LiquidData newState) {
        setLiquid(pos.x, pos.y, pos.z, newState);
    }

    @Override
    public void setLiquid(int x, int y, int z, LiquidData newState) {
        if (locked.get() && blockRegion.encompasses(x, y, z)) {
            int chunkIndex = relChunkIndex(x, y, z);
            chunks[chunkIndex].setLiquid(TeraMath.calcBlockPos(x, y, z, chunkFilterSize), newState);
        } else {
            throw new IllegalStateException("Attempted to modify liquid data though an unlocked view");
        }
    }

    @Override
    public void setLight(Vector3i pos, byte light) {
        setLight(pos.x, pos.y, pos.z, light);
    }

    @Override
    public void setLight(int blockX, int blockY, int blockZ, byte light) {
        if (locked.get() && blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setLight(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), light);
        } else if (!locked.get()) {
            throw new IllegalStateException("Attempted to modify light though an unlocked view");
        } else {
            logger.warn("Attempted to set light at a position not encompassed by the view");
        }
    }

    @Override
    public void setSunlight(Vector3i pos, byte light) {
        setSunlight(pos.x, pos.y, pos.z, light);
    }

    @Override
    public void setSunlight(int blockX, int blockY, int blockZ, byte light) {
        if (locked.get() && blockRegion.encompasses(blockX, blockY, blockZ)) {
            int chunkIndex = relChunkIndex(blockX, blockY, blockZ);
            chunks[chunkIndex].setSunlight(TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkFilterSize), light);
        } else {
            throw new IllegalStateException("Attempted to modify sunlight though an unlocked view");
        }
    }

    @Override
    public void setDirtyAround(Vector3i blockPos) {
        for (Vector3i pos : TeraMath.getChunkRegionAroundBlockPos(blockPos, 1)) {
            chunks[pos.x + offset.x + chunkRegion.size().x * (pos.z + offset.z)].setDirty(true);
        }
    }

    @Override
    public void setDirtyAround(Region3i blockRegion) {
        Vector3i minPos = new Vector3i(blockRegion.min());
        minPos.sub(1, 0, 1);
        Vector3i maxPos = new Vector3i(blockRegion.max());
        maxPos.add(1, 0, 1);

        Vector3i minChunk = TeraMath.calcChunkPos(minPos, chunkPower);
        Vector3i maxChunk = TeraMath.calcChunkPos(maxPos, chunkPower);

        for (Vector3i pos : Region3i.createFromMinMax(minChunk, maxChunk)) {
            chunks[pos.x + offset.x + chunkRegion.size().x * (pos.z + offset.z)].setDirty(true);
        }
    }

    @Override
    public void lock() {
        if (!locked.get()) {
            for (Chunk chunk : chunks) {
                chunk.lock();
            }
            locked.set(true);
        }
    }

    @Override
    public void unlock() {
        if (locked.get()) {
            locked.set(false);
            for (Chunk chunk : chunks) {
                chunk.unlock();
            }
        }
    }

    @Override
    public boolean isLocked() {
        return locked.get();
    }

    @Override
    public boolean isValidView() {
        for (Chunk chunk : chunks) {
            if (chunk.isDisposed()) {
                return false;
            }
        }
        return true;
    }

    int relChunkIndex(int x, int y, int z) {
        return TeraMath.calcChunkPosX(x, chunkPower.x) + offset.x + chunkRegion.size().x * (TeraMath.calcChunkPosZ(z, chunkPower.z) + offset.z);
    }

    public void setChunkSize(Vector3i chunkSize) {
        this.chunkSize = chunkSize;
        this.chunkFilterSize = new Vector3i(TeraMath.ceilPowerOfTwo(chunkSize.x) - 1, 0, TeraMath.ceilPowerOfTwo(chunkSize.z) - 1);
        this.chunkPower = new Vector3i(TeraMath.sizeOfPower(chunkSize.x), 0, TeraMath.sizeOfPower(chunkSize.z));

        Vector3i blockMin = new Vector3i();
        blockMin.sub(offset);
        blockMin.mult(chunkSize.x, 0, chunkSize.z);
        Vector3i blockSize = chunkRegion.size();
        blockSize.mult(chunkSize.x, chunkSize.y, chunkSize.z);
        this.blockRegion = Region3i.createFromMinAndSize(blockMin, blockSize);
    }

    @Override
    public Vector3i toWorldPos(Vector3i localPos) {
        return new Vector3i(localPos.x + (offset.x + chunkRegion.min().x) * Chunk.SIZE_X, localPos.y, localPos.z + (offset.z + chunkRegion.min().z) * Chunk.SIZE_Z);
    }
}
