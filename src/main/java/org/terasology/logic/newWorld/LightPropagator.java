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

import com.google.common.collect.Lists;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.model.blocks.Block;

import java.util.List;

/**
 * @author Immortius
 */
public class LightPropagator {

    private static final Vector3i[] HORIZONTAL_LIGHT_DIRECTIONS = {
            new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
    };

    private static final Vector3i[] ALL_LIGHT_DIRECTIONS = {
            new Vector3i(1, 0, 0), new Vector3i(-1, 0, 0),
            new Vector3i(0, 1, 0), new Vector3i(0, -1, 0),
            new Vector3i(0, 0, 1), new Vector3i(0, 0, -1)
    };

    private Region3i region;
    private NewChunk[] chunks;

    public LightPropagator(NewChunkProvider chunkProvider, Region3i chunkRegion) {
        this.region = chunkRegion;
        this.chunks = new NewChunk[chunkRegion.size().x * chunkRegion.size().z];
        for (Vector3i pos : region) {
            NewChunk chunk = chunkProvider.getChunk(pos);
            int index = absChunkIndex(pos.x, pos.y, pos.z);
            chunks[index] = chunk;
        }
    }

    public void propagateOutOfChunk(Vector3i chunkPos) {
        int maxX = NewChunk.CHUNK_DIMENSION_X - 1;
        int maxZ = NewChunk.CHUNK_DIMENSION_Z - 1;
        Vector3i baseChunkPos = new Vector3i(NewChunk.CHUNK_DIMENSION_X * (chunkPos.x - region.min().x), 0, NewChunk.CHUNK_DIMENSION_Z *(chunkPos.z - region.min().z));
        // Iterate over the blocks on the horizontal sides
        for (int y = 0; y < NewChunk.CHUNK_DIMENSION_Y; y++) {
            for (int x = 0; x < maxX; x++) {
                propagateFrom(x + baseChunkPos.x, y, 0);
                propagateFrom(x + baseChunkPos.x + 1, y, maxZ);
            }
            for (int z = 0; z < maxZ; z++) {
                propagateFrom(0, y, z + baseChunkPos.z + 1);
                propagateFrom(maxX, y, z + baseChunkPos.z);
            }
        }
    }

    private void propagateFrom(int blockX, int blockY, int blockZ) {
        propagateSunlightFrom(blockX, blockY, blockZ);
        propagateLightFrom(blockX, blockY, blockZ);
    }

    private void propagateSunlightFrom(int blockX, int blockY, int blockZ) {
        byte lightLevel = getSunlight(blockX, blockY, blockZ);
        if (lightLevel <= 1) return;

        for (Vector3i adjDir : HORIZONTAL_LIGHT_DIRECTIONS) {
            int adjX = blockX + adjDir.x;
            int adjZ = blockZ + adjDir.z;

            byte adjLightValue = getSunlight(adjX, blockY, adjZ);
            if (adjLightValue < lightLevel - 1 && getBlock(adjX, blockY, adjZ).isTranslucent()) {
                setSunlight(adjX, blockY, adjZ, (byte)(lightLevel - 1));
                propagateSunlightFrom(adjX, blockY, adjZ);
            }
        }

        if (blockY > 0) {
            byte lowerLight = getSunlight(blockX, blockY - 1, blockZ);
            if (lightLevel == NewChunk.MAX_LIGHT) {
                if (lowerLight < lightLevel) {
                    setSunlight(blockX, blockY - 1, blockZ, lightLevel);
                    propagateSunlightFrom(blockX, blockY - 1, blockZ);
                }
            } else {
                if (lowerLight < lightLevel - 1) {
                    setSunlight(blockX, blockY - 1, blockZ, (byte)(lightLevel - 1));
                    propagateSunlightFrom(blockX, blockY - 1, blockZ);
                }
            }
        }
        if (blockY < NewChunk.CHUNK_DIMENSION_Y - 1) {
            byte upperLight = getSunlight(blockX, blockY + 1, blockZ);
            if (upperLight < lightLevel - 1) {
                setSunlight(blockX, blockY + 1, blockZ, (byte)(lightLevel - 1));
                propagateSunlightFrom(blockX, blockY + 1, blockZ);
            }
        }
    }

    private void propagateLightFrom(int blockX, int blockY, int blockZ) {
        byte lightLevel = getLight(blockX, blockY, blockZ);
        if (lightLevel <= 1) return;

        for (Vector3i adjDir : HORIZONTAL_LIGHT_DIRECTIONS) {
            int adjX = blockX + adjDir.x;
            int adjZ = blockZ + adjDir.z;

            byte adjLightValue = getLight(adjX, blockY, adjZ);
            if (adjLightValue < lightLevel - 1 && getBlock(adjX, blockY, adjZ).isTranslucent()) {
                setLight(adjX, blockY, adjZ, (byte)(lightLevel - 1));
                propagateLightFrom(adjX, blockY, adjZ);
            }
        }

        if (blockY > 0) {
            byte lowerLight = getLight(blockX, blockY - 1, blockZ);
            if (lowerLight < lightLevel - 1) {
                setLight(blockX, blockY - 1, blockZ, (byte)(lightLevel - 1));
                propagateLightFrom(blockX, blockY - 1, blockZ);
            }
        }
        if (blockY < NewChunk.CHUNK_DIMENSION_Y - 1) {
            byte upperLight = getLight(blockX, blockY + 1, blockZ);
            if (upperLight < lightLevel - 1) {
                setLight(blockX, blockY + 1, blockZ, (byte)(lightLevel - 1));
                propagateLightFrom(blockX, blockY + 1, blockZ);
            }
        }
    }

    private Block getBlock(int blockX, int blockY, int blockZ) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getBlock(innerBlockPos);
    }

    private byte getSunlight(int blockX, int blockY, int blockZ) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getSunlight(innerBlockPos);
    }

    private byte getLight(int blockX, int blockY, int blockZ) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        return chunks[chunkIndex].getLight(innerBlockPos);
    }

    private void setSunlight(int blockX, int blockY, int blockZ, byte light) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        chunks[chunkIndex].setSunlight(innerBlockPos, light);
    }

    private void setLight(int blockX, int blockY, int blockZ, byte light) {
        Vector3i chunkPos = TeraMath.calcChunkPos(blockX, blockY, blockZ);
        int chunkIndex = relChunkIndex(chunkPos.x, chunkPos.y, chunkPos.z);
        Vector3i innerBlockPos = TeraMath.calcBlockPos(blockX, blockY, blockZ, chunkPos);
        chunks[chunkIndex].setLight(innerBlockPos, light);
    }



    private int absChunkIndex(int x, int y, int z) {
        return relChunkIndex(x - region.min().x, y - region.min().y, z - region.min().z);
    }

    private int relChunkIndex(int x, int y, int z) {
        return x + region.size().x * z;
    }
}
