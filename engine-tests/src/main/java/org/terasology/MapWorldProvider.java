// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology;

import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.math.JomlUtil;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.generation.impl.EntityBufferImpl;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Dummy world provider. Generates chunk and world data on demand.
 */
public class MapWorldProvider implements WorldProviderCore {

    private final Map<Vector3i, Block> blocks = Maps.newHashMap();
    private final Map<Vector3i, Chunk> chunks = Maps.newHashMap();
    private final WorldGenerator worldGenerator;
    private final BlockManager blockManager;
    private final ExtraBlockDataManager extraDataManager;
    private final EntityBufferImpl entityBuffer;

    public MapWorldProvider(WorldGenerator worldGenerator, BlockManager blockManager,
                            ExtraBlockDataManager extraDataManager) {
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
    public boolean isRegionRelevant(Region3i region) {
        return false;
    }

    @Override
    public Block setBlock(Vector3i pos, Block type) {
        return blocks.put(pos, type);
    }

    @Override
    public Block setBlock(Vector3ic pos, Block type) {
        return blocks.put(JomlUtil.from(pos), type);
    }


    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i pos = new Vector3i(x, y, z);
        Block block = blocks.get(pos);
        if (block != null) {
            return block;
        }

        // TODO block manager
        Vector3i chunkPos = ChunkMath.calcChunkPos(pos);
        Chunk chunk = chunks.get(chunkPos);
        if (chunk == null && worldGenerator != null) {
            chunk = new ChunkImpl(chunkPos, blockManager, extraDataManager);
            worldGenerator.createChunk(chunk, entityBuffer);
            chunks.put(chunkPos, chunk);
        }
        if (chunk != null) {
            return chunk.getBlock(ChunkMath.calcRelativeBlockPos(pos.x, pos.y, pos.z));
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
    public int setExtraData(int index, Vector3i pos, int value) {
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
    public Collection<Region3i> getRelevantRegions() {
        return Collections.emptySet();
    }
}
