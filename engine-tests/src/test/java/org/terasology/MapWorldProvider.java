/*
 * Copyright 2015 MovingBlocks
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
package org.terasology;

import com.google.common.collect.Maps;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Dummy world provider. Generates chunk and world data on demand.
 */
public class MapWorldProvider implements WorldProviderCore {
    private Map<Vector3i, Block> blocks = Maps.newHashMap();
    private Map<Vector3i, Chunk> chunks = Maps.newHashMap();
    private WorldGenerator worldGenerator;

    public MapWorldProvider(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    @Override
    public EntityRef getWorldEntity() {
        return null;
    }

    @Override
    public void processPropagation() {
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return false;
    }

    @Override
    public boolean isRegionRelevant(Region3i region) {
        return false;
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        return blocks.put(pos, type);
    }

    @Override
    public Biome setBiome(Vector3i pos, Biome biome) {
        return null;
    }

    @Override
    public Biome getBiome(Vector3i pos) {
        return null;
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        Block block = blocks.get(pos);
        if (block != null) {
            return block;
        }
        Vector3i chunkPos = TeraMath.calcChunkPos(pos);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null && worldGenerator != null) {
            chunk = new ChunkImpl(chunkPos);
            worldGenerator.createChunk(chunk);
            chunks.put(chunkPos, chunk);
        }
        if (chunk != null) {
            return chunk.getBlock(TeraMath.calcBlockPos(pos.x, pos.y, pos.z));
        }
        return null;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public String getSeed() {
        return "1";
    }

    @Override
    public WorldInfo getWorldInfo() {
        return null;

    }

    @Override
    public ChunkViewCore getLocalView(Vector3i chunkPos) {
        return null;
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3i chunk) {
        return null;
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newData, LiquidData oldData) {
        return false;
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        return null;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        return 0;
    }

    @Override
    public WorldTime getTime() {
        return new WorldTimeImpl();
    }

    @Override
    public void dispose() {
    }

    @Override
    public Collection<Region3i> getRelevantRegions() {
        return Collections.emptySet();
    }
}
