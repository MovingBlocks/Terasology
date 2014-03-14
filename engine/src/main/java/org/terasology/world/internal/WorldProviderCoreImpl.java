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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.BrownianNoise3D;
import org.terasology.utilities.procedural.Noise3D;
import org.terasology.utilities.procedural.PerlinNoise;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.propagation.BatchPropagator;
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class WorldProviderCoreImpl implements WorldProviderCore {
    private static final Logger logger = LoggerFactory.getLogger(WorldProviderCoreImpl.class);

    private String title;
    private String seed = "";
    private SimpleUri worldGenerator;

    private GeneratingChunkProvider chunkProvider;
    private WorldTime worldTime;

    private Noise3D fogNoise;

    private final List<WorldChangeListener> listeners = Lists.newArrayList();

    private Map<Vector3i, BlockChange> blockChanges = Maps.newHashMap();
    private List<BatchPropagator> propagators = Lists.newArrayList();

    public WorldProviderCoreImpl(String title, String seed, long time, SimpleUri worldGenerator, GeneratingChunkProvider chunkProvider) {
        this.title = (title == null) ? seed : title;
        this.seed = seed;
        this.worldGenerator = worldGenerator;
        this.chunkProvider = chunkProvider;
        this.fogNoise = new BrownianNoise3D(new PerlinNoise(seed.hashCode() + 42 * 42), 8);
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
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

    public WorldProviderCoreImpl(WorldInfo info, GeneratingChunkProvider chunkProvider) {
        this(info.getTitle(), info.getSeed(), info.getTime(), info.getWorldGenerator(), chunkProvider);
    }

    @Override
    public EntityRef getWorldEntity() {
        Iterator<EntityRef> iterator = CoreRegistry.get(EntityManager.class).getEntitiesWith(WorldComponent.class).iterator();
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
        return chunkProvider.isChunkReady(TeraMath.calcChunkPos(x, y, z));
    }

    @Override
    public Block setBlock(Vector3i worldPos, Block type) {
        Vector3i chunkPos = TeraMath.calcChunkPos(worldPos);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(worldPos);
            Block oldBlockType = chunk.setBlock(blockPos, type);
            if (oldBlockType != type) {
                BlockChange oldChange = blockChanges.get(worldPos);
                if (oldChange == null) {
                    blockChanges.put(worldPos, new BlockChange(worldPos, oldBlockType, type));
                } else {
                    oldChange.setTo(type);
                }
                for (Vector3i pos : TeraMath.getChunkRegionAroundWorldPos(worldPos, 1)) {
                    ChunkImpl dirtiedChunk = chunkProvider.getChunk(pos);
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
        // TODO: Could use a read/write lock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBlockChanged(pos, type, oldType);
            }
        }
    }

    @Override
    public boolean setLiquid(int x, int y, int z, LiquidData newState, LiquidData oldState) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            chunk.lock();
            try {
                Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
                LiquidData liquidState = chunk.getLiquid(blockPos);
                if (liquidState.equals(oldState)) {
                    chunk.setLiquid(blockPos, newState);
                    return true;
                }
            } finally {
                chunk.unlock();
            }
        }
        return false;
    }

    @Override
    public LiquidData getLiquid(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLiquid(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via liquid data at {}, {}, {}", x, y, z);
        return new LiquidData();
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getBlock(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via block at {}, {}, {}", x, y, z);
        return BlockManager.getAir();
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getLight(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via light at {}, {}, {}", x, y, z);
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return chunk.getSunlight(blockPos);
        }
        logger.warn("Attempted to access unavailable chunk via sunlight at {}, {}, {}", x, y, z);
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = TeraMath.calcChunkPos(x, y, z);
        ChunkImpl chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = TeraMath.calcBlockPos(x, y, z);
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        logger.warn("Attempted to access unavailable chunk via total light at {}, {}, {}", x, y, z);
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
    public float getFog(float x, float y, float z) {
        return (float) TeraMath.clamp(TeraMath.fastAbs(fogNoise.noise(getTime().getDays() * 0.1f, 0.01f, 0.01f) * 2.0f)) * chunkProvider.getWorldGenerator().getFog(x, y, z);
    }

    @Override
    public float getTemperature(float x, float y, float z) {
        return chunkProvider.getWorldGenerator().getTemperature(x, y, z);
    }

    @Override
    public float getHumidity(float x, float y, float z) {
        return chunkProvider.getWorldGenerator().getHumidity(x, y, z);
    }
}
