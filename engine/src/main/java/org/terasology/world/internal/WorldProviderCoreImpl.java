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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.chunks.ManagedChunk;
import org.terasology.world.chunks.RenderableChunk;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.BiomeChange;
import org.terasology.world.propagation.BlockChange;
import org.terasology.world.propagation.PropagationRules;
import org.terasology.world.propagation.PropagatorWorldView;
import org.terasology.world.propagation.StandardBatchPropagator;
import org.terasology.world.propagation.SunlightRegenBatchPropagator;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.LightWorldView;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.world.propagation.light.SunlightRegenWorldView;
import org.terasology.world.propagation.light.SunlightWorldView;
import org.terasology.world.time.WorldTime;
import org.terasology.world.time.WorldTimeImpl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 */
public class WorldProviderCoreImpl implements WorldProviderCore {

    private String title;
    private String seed = "";
    private SimpleUri worldGenerator;

    private GeneratingChunkProvider chunkProvider;
    private WorldTime worldTime;
    private EntityManager entityManager;

    private final List<WorldChangeListener> listeners = Lists.newArrayList();

    private Map<Vector3i, BlockChange> blockChanges = Maps.newHashMap();
    private Map<Vector3i, BiomeChange> biomeChanges = Maps.newHashMap();
    private List<BatchPropagator> propagators = Lists.newArrayList();

    private Block unloadedBlock;

    public WorldProviderCoreImpl(String title, String seed, long time, SimpleUri worldGenerator,
                                 GeneratingChunkProvider chunkProvider, Block unloadedBlock, Context context) {
        this.title = (title == null) ? seed : title;
        this.seed = seed;
        this.worldGenerator = worldGenerator;
        this.chunkProvider = chunkProvider;
        this.unloadedBlock = unloadedBlock;
        this.entityManager = context.get(EntityManager.class);
        context.put(ChunkProvider.class, chunkProvider);

        this.worldTime = new WorldTimeImpl();
        worldTime.setMilliseconds(time);

        propagators.add(new StandardBatchPropagator(new LightPropagationRules(), new LightWorldView(chunkProvider)));
        PropagatorWorldView regenWorldView = new SunlightRegenWorldView(chunkProvider);
        PropagationRules sunlightRules = new SunlightPropagationRules(regenWorldView);
        PropagatorWorldView sunlightWorldView = new SunlightWorldView(chunkProvider);
        BatchPropagator sunlightPropagator = new StandardBatchPropagator(sunlightRules, sunlightWorldView);
        propagators.add(new SunlightRegenBatchPropagator(new SunlightRegenPropagationRules(), regenWorldView, sunlightPropagator, sunlightWorldView));
        propagators.add(sunlightPropagator);
    }

    public WorldProviderCoreImpl(WorldInfo info, GeneratingChunkProvider chunkProvider, Block unloadedBlock,
                                 Context context) {
        this(info.getTitle(), info.getSeed(), info.getTime(), info.getWorldGenerator(), chunkProvider,
                unloadedBlock, context);
    }

    @Override
    public EntityRef getWorldEntity() {
        Iterator<EntityRef> iterator = entityManager.getEntitiesWith(WorldComponent.class).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return EntityRef.NULL;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSeed() {
        return seed;
    }

    @Override
    public WorldInfo getWorldInfo() {
        return new WorldInfo(title, seed, worldTime.getMilliseconds(), worldGenerator);
    }

    @Override
    public void processPropagation() {
        for (BatchPropagator propagator : propagators) {
            propagator.process(blockChanges.values());
        }
        blockChanges.clear();
    }

    @Override
    public void registerListener(WorldChangeListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void unregisterListener(WorldChangeListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public ChunkViewCore getLocalView(Vector3i chunkPos) {
        return chunkProvider.getLocalView(chunkPos);
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3i chunk) {
        return chunkProvider.getSubviewAroundChunk(chunk);
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return chunkProvider.isChunkReady(ChunkMath.calcChunkPos(x, y, z));
    }

    @Override
    public boolean isRegionRelevant(Region3i region) {
        for (Vector3i chunkPos : ChunkMath.calcChunkPos(region)) {
            if (!chunkProvider.isChunkReady(chunkPos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Block setBlock(Vector3i worldPos, Block type) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(worldPos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(worldPos);
            chunk.writeLock();
            Block oldBlockType = chunk.setBlock(blockPos, type);
            chunk.writeUnlock();
            if (oldBlockType != type) {
                BlockChange oldChange = blockChanges.get(worldPos);
                if (oldChange == null) {
                    blockChanges.put(worldPos, new BlockChange(worldPos, oldBlockType, type));
                } else {
                    oldChange.setTo(type);
                }
                for (Vector3i pos : ChunkMath.getChunkRegionAroundWorldPos(worldPos, 1)) {
                    RenderableChunk dirtiedChunk = chunkProvider.getChunk(pos);
                    if (dirtiedChunk != null) {
                        dirtiedChunk.setDirty(true);
                    }
                }
                notifyBlockChanged(worldPos, type, oldBlockType);
            }
            return oldBlockType;

        }
        return null;
    }

    private void notifyBlockChanged(Vector3i pos, Block type, Block oldType) {
        // TODO: Could use a read/write writeLock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBlockChanged(pos, type, oldType);
            }
        }
    }

    private void notifyBiomeChanged(Vector3i pos, Biome newBiome, Biome originalBiome) {
        // TODO: Could use a read/write writeLock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBiomeChanged(pos, newBiome, originalBiome);
            }
        }
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            chunk.writeLock();
            try {
                Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
                LiquidData liquidState = chunk.getLiquid(blockPos);
                if (liquidState.equals(oldState)) {
                    chunk.setLiquid(blockPos, newState);
                    return true;
                }
            } finally {
                chunk.writeUnlock();
            }
        }
        return false;
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
            return chunk.getLiquid(blockPos);
        }
        return new LiquidData();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
            return chunk.getBlock(blockPos);
        }
        return unloadedBlock;
    }

    @Override
    public Biome getBiome(Vector3i pos) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(pos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(pos);
            return chunk.getBiome(blockPos.x, blockPos.y, blockPos.z);
        }
        return BiomeManager.getUnknownBiome();
    }

    @Override
    public Biome setBiome(Vector3i worldPos, Biome biome) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(worldPos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(worldPos);
            chunk.writeLock();
            Biome oldBiomeType = chunk.setBiome(blockPos.x, blockPos.y, blockPos.z, biome);
            chunk.writeUnlock();
            if (oldBiomeType != biome) {
                BiomeChange oldChange = biomeChanges.get(worldPos);
                if (oldChange == null) {
                    biomeChanges.put(worldPos, new BiomeChange(worldPos, oldBiomeType, biome));
                } else {
                    oldChange.setTo(biome);
                }
                for (Vector3i pos : ChunkMath.getChunkRegionAroundWorldPos(worldPos, 1)) {
                    RenderableChunk dirtiedChunk = chunkProvider.getChunk(pos);
                    if (dirtiedChunk != null) {
                        dirtiedChunk.setDirty(true);
                    }
                }
                notifyBiomeChanged(worldPos, biome, oldBiomeType);
            }
            return oldBiomeType;

        }
        return null;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
            return chunk.getLight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
            return chunk.getSunlight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcBlockPos(x, y, z);
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        return 0;
    }

    @Override
    public void dispose() {
        chunkProvider.dispose();

    }

    @Override
    public WorldTime getTime() {
        return worldTime;
    }

    @Override
    public Collection<Region3i> getRelevantRegions() {
        Collection<Chunk> chunks = chunkProvider.getAllChunks();
        Function<Chunk, Region3i> mapping = CoreChunk::getRegion;

        Predicate<Chunk> isReady = ManagedChunk::isReady;

        return FluentIterable.from(chunks).filter(isReady).transform(mapping).toList();
    }
}
