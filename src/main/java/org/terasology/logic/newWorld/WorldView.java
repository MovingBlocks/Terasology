/*
 * Copyright 2012
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

package org.terasology.logic.newWorld;

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.management.BlockManager;

/**
 * @author Immortius
 */
public class WorldView {

    private Vector3i offset;
    private Region3i region;
    private NewChunk[] chunks;

    public static WorldView createLocalView(Vector3i pos, NewChunkProvider chunkProvider) {
        Region3i region = Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1));
        return createWorldView(region, Vector3i.one(), chunkProvider);
    }

    public static WorldView createSubview(Vector3i pos, int extent, NewChunkProvider chunkProvider) {
        Region3i region = TeraMath.getChunkRegionAroundBlockPos(pos, extent);
        return createWorldView(region, new Vector3i(-region.min().x, 0, -region.min().z), chunkProvider);
    }

    public static WorldView createWorldView(Region3i region, Vector3i offset, NewChunkProvider chunkProvider) {
        NewChunk[] chunks = new NewChunk[region.size().x * region.size().z];
        for (Vector3i chunkPos : region) {
            NewChunk chunk = chunkProvider.getChunk(chunkPos);
            if (chunk == null) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new WorldView(chunks, region, offset);
    }

    public WorldView(NewChunk[] chunks, Region3i chunkRegion, Vector3i offset) {
        this.region = chunkRegion;
        this.chunks = chunks;
        this.offset = offset;
    }

    public Block getBlock(float x, float y, float z) {
        return getBlock(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    // TODO: Review
    public Block getBlock(int blockX, int blockY, int blockZ) {
        if (!isYInBounds(blockY)) return BlockManager.getInstance().getBlock((byte)0);

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getBlock(innerBlockPos);
    }

    public byte getSunlight(float x, float y, float z) {
        return getSunlight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    public byte getSunlight(Vector3i pos) {
        return getSunlight(pos.x, pos.y, pos.z);
    }

    public byte getLight(float x, float y, float z) {
        return getLight(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
    }

    public byte getLight(Vector3i pos) {
        return getLight(pos.x, pos.y, pos.z);
    }

    public byte getSunlight(int blockX, int blockY, int blockZ) {
        if (!isYInBounds(blockY)) return 0;

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getSunlight(innerBlockPos);
    }

    public byte getLight(int blockX, int blockY, int blockZ) {
        if (!isYInBounds(blockY)) return 0;

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getLight(innerBlockPos);
    }

    public boolean setBlock(Vector3i pos, Block type, Block oldType) {
        return setBlock(pos.x, pos.y, pos.z, type, oldType);
    }

    public boolean setBlock(int blockX, int blockY, int blockZ, Block type, Block oldType) {
        if (!isYInBounds(blockY)) return false;

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].setBlock(innerBlockPos, type, oldType);
    }

    public void setLight(Vector3i pos, byte light) {
        setLight(pos.x, pos.y, pos.z, light);
    }

    public void setSunlight(Vector3i pos, byte light) {
        setSunlight(pos.x, pos.y, pos.z, light);
    }

    public void setSunlight(int blockX, int blockY, int blockZ, byte light) {
        if (!isYInBounds(blockY)) return;

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        chunks[chunkIndex].setSunlight(innerBlockPos, light);
    }

    public void setLight(int blockX, int blockY, int blockZ, byte light) {
        if (!isYInBounds(blockY)) return;

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        chunks[chunkIndex].setLight(innerBlockPos, light);
    }

    public void setDirtyAround(Vector3i blockPos) {
        for (Vector3i pos : TeraMath.getChunkRegionAroundBlockPos(blockPos, 1)) {
            chunks[relChunkIndex(pos.x, pos.y, pos.z)].setDirty(true);
        }
    }

    public void setDirtyAround(Region3i blockRegion) {
        Vector3i minPos = new Vector3i(blockRegion.min());
        minPos.sub(1,0,1);
        Vector3i maxPos = new Vector3i(blockRegion.max());
        maxPos.add(1,0,1);

        Vector3i minChunk = TeraMath.calcChunkPos(minPos);
        Vector3i maxChunk = TeraMath.calcChunkPos(maxPos);

        for (Vector3i pos : Region3i.createFromMinMax(minChunk, maxChunk)) {
            chunks[relChunkIndex(pos.x, pos.y, pos.z)].setDirty(true);
        }
    }

    public void lock() {
        for (NewChunk chunk : chunks) {
            chunk.lock();
        }
    }

    public void unlock() {
        for (NewChunk chunk : chunks) {
            chunk.unlock();
        }
    }

    private int relChunkIndex(int x, int y, int z) {
        return (x + offset.x) + region.size().x * (z + offset.z);
    }

    private boolean isYInBounds(int y) {
        return y >= 0 && y < NewChunk.SIZE_Y;
    }
}
