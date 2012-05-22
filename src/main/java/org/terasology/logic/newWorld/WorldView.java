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

    public static WorldView CreateLocalView(Vector3i pos, NewChunkProvider chunkProvider) {
        Region3i region = Region3i.createFromCenterExtents(pos, new Vector3i(1,0,1));
        NewChunk[] chunks = new NewChunk[region.size().x * region.size().z];
        for (Vector3i chunkPos : region) {
            NewChunk chunk = chunkProvider.getChunk(chunkPos);
            if (chunk == null) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new WorldView(chunks, region, Vector3i.one());
    }

    public WorldView(NewChunk[] chunks, Region3i chunkRegion, Vector3i offset) {
        this.region = chunkRegion;
        this.chunks = chunks;
        this.offset = offset;
    }

    public Block getBlock(float x, float y, float z) {
        return getBlock(TeraMath.floorToInt(x + 0.5f), TeraMath.floorToInt(y + 0.5f), TeraMath.floorToInt(z + 0.5f));
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

    // TODO: Review
    public Block getBlock(int blockX, int blockY, int blockZ) {
        if (!isYInBounds(blockY)) return BlockManager.getInstance().getBlock((byte)0);

        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getBlock(innerBlockPos);
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

    private int relChunkIndex(int x, int y, int z) {
        return (x + offset.x) + region.size().x * (z + offset.z);
    }

    private boolean isYInBounds(int y) {
        return y >= 0 && y < NewChunk.CHUNK_DIMENSION_Y;
    }
}
