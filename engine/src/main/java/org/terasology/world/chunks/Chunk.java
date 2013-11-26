/*
 * Copyright 2013 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.world.chunks;

import org.terasology.engine.API;
import org.terasology.math.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.liquid.LiquidData;

@API
public interface Chunk {
    Vector3i getPos();

    Block getBlock(Vector3i pos);

    Block getBlock(int x, int y, int z);

    Block setBlock(int x, int y, int z, Block block);

    Block setBlock(Vector3i pos, Block block);

    void setLiquid(Vector3i pos, LiquidData state);

    void setLiquid(int x, int y, int z, LiquidData newState);

    LiquidData getLiquid(Vector3i pos);

    LiquidData getLiquid(int x, int y, int z);

    Vector3i getChunkWorldPos();

    int getChunkWorldPosX();

    int getChunkWorldPosY();

    int getChunkWorldPosZ();

    Vector3i getBlockWorldPos(Vector3i blockPos);

    Vector3i getBlockWorldPos(int x, int y, int z);

    int getBlockWorldPosX(int x);

    int getBlockWorldPosY(int y);

    int getBlockWorldPosZ(int z);

    int getChunkSizeX();

    int getChunkSizeY();

    int getChunkSizeZ();
}
