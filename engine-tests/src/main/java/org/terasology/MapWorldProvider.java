/*
 * Copyright 2017 MovingBlocks
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
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.generation.impl.EntityBufferImpl;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Dummy world provider. Generates chunk and world data on demand.
 */
public class MapWorldProvider implements WorldProviderCore {

    private Map<Vector3ic, Block> blocks = Maps.newHashMap();
    private Map<Vector3ic, Chunk> chunks = Maps.newHashMap();
    private WorldGenerator worldGenerator;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private EntityBufferImpl entityBuffer;

    public MapWorldProvider(WorldGenerator worldGenerator, BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        this.worldGenerator = worldGenerator;
        this.blockManager = blockManager;
        this.extraDataManager = extraDataManager;
        entityBuffer = new EntityBufferImpl();
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
    public boolean isRegionRelevant(BlockRegionc region) {
        return false;
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        return blocks.put(new Vector3i(pos), type);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        Block block = blocks.get(pos);
        if (block != null) {
            return block;
        }

        // TODO block manager
        Vector3i chunkPos = Chunks.toChunkPos(pos, new Vector3i());
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null && worldGenerator != null) {
            chunk = new ChunkImpl(chunkPos, blockManager, extraDataManager);
            worldGenerator.createChunk(chunk, entityBuffer);
            chunks.put(chunkPos, chunk);
        }
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelative(pos, pos));
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
    public ChunkViewCore getLocalView(Vector3ic chunkPos) {
        return null;
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3ic chunk) {
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
    public int setExtraData(int index, Vector3ic pos, int value) {
        return 0;
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
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
    public Collection<BlockRegionc> getRelevantRegions() {
        return Collections.emptySet();
    }
}
