// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world.internal;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.WorldChangeListener;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.propagation.BatchPropagator;
import org.terasology.engine.world.propagation.BlockChange;
import org.terasology.engine.world.propagation.PropagationRules;
import org.terasology.engine.world.propagation.PropagatorWorldView;
import org.terasology.engine.world.propagation.StandardBatchPropagator;
import org.terasology.engine.world.propagation.SunlightRegenBatchPropagator;
import org.terasology.engine.world.propagation.light.LightPropagationRules;
import org.terasology.engine.world.propagation.light.LightWorldView;
import org.terasology.engine.world.propagation.light.SunlightPropagationRules;
import org.terasology.engine.world.propagation.light.SunlightRegenPropagationRules;
import org.terasology.engine.world.propagation.light.SunlightRegenWorldView;
import org.terasology.engine.world.propagation.light.SunlightWorldView;
import org.terasology.engine.world.time.WorldTime;
import org.terasology.engine.world.time.WorldTimeImpl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class WorldProviderCoreImpl implements WorldProviderCore {

    private String title;
    private String seed = "";
    private SimpleUri worldGenerator;

    private ChunkProvider chunkProvider;
    private WorldTime worldTime;
    private EntityManager entityManager;

    private final List<WorldChangeListener> listeners = Lists.newArrayList();

    private final Map<Vector3i, BlockChange> blockChanges = Maps.newHashMap();
    private List<BatchPropagator> propagators = Lists.newArrayList();

    private Block unloadedBlock;

    public WorldProviderCoreImpl(String title, String seed, long time, SimpleUri worldGenerator,
                                 ChunkProvider chunkProvider, Block unloadedBlock, Context context) {
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
        propagators.add(new SunlightRegenBatchPropagator(new SunlightRegenPropagationRules(), regenWorldView,
                sunlightPropagator, sunlightWorldView));
        propagators.add(sunlightPropagator);
    }

    public WorldProviderCoreImpl(WorldInfo info, ChunkProvider chunkProvider, Block unloadedBlock,
                                 Context context) {
        this(info.getTitle(), info.getSeed(), info.getTime(), info.getWorldGenerator(),
                chunkProvider,
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
    public ChunkViewCore getLocalView(Vector3ic chunkPos) {
        BlockRegion region = new BlockRegion(chunkPos).expand(Chunks.LOCAL_REGION_EXTENTS);
        return chunkProvider.getSubview(region, new Vector3i(1, 1, 1));
    }

    @Override
    public ChunkViewCore getWorldViewAround(Vector3ic chunkPos) {
        return getWorldViewAround(new BlockRegion(chunkPos).expand(Chunks.LOCAL_REGION_EXTENTS));
    }

    @Override
    public ChunkViewCore getWorldViewAround(BlockRegionc region) {
        return chunkProvider.getSubview(region, region.getMin(new Vector3i()).mul(-1));
    }

    @Override
    public boolean isBlockRelevant(int x, int y, int z) {
        return chunkProvider.isChunkReady(Chunks.toChunkPos(x, y, z, new Vector3i()));
    }

    @Override
    public boolean isRegionRelevant(BlockRegionc region) {
        for (Vector3ic chunkPos : Chunks.toChunkRegion(region, new BlockRegion(BlockRegion.INVALID))) {
            if (!chunkProvider.isChunkReady(chunkPos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Block setBlock(Vector3ic worldPos, Block type) {
        /*
         * Hint: This method has a benchmark available in the BenchmarkScreen, The screen can be opened ingame via the
         * command "showSCreen BenchmarkScreen".
         */
        Vector3i chunkPos = Chunks.toChunkPos(worldPos, new Vector3i());
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = Chunks.toRelative(worldPos, new Vector3i());
            Block oldBlockType = chunk.setBlock(blockPos, type);
            if (oldBlockType != type) {
                BlockChange oldChange = blockChanges.get(worldPos);
                if (oldChange == null) {
                    blockChanges.put(new Vector3i(worldPos), new BlockChange(worldPos, oldBlockType, type));
                } else {
                    oldChange.setTo(type);
                }
                setDirtyChunksNear(worldPos);
                notifyBlockChanged(worldPos, type, oldBlockType);
            }
            return oldBlockType;

        }
        return null;
    }

    @Override
    public Map<Vector3ic, Block> setBlocks(Map<? extends Vector3ic, Block> blocks) {
        /*
         * Hint: This method has a benchmark available in the BenchmarkScreen, The screen can be opened ingame via the
         * command "showSCreen BenchmarkScreen".
         */
        Set<BlockChange> changedBlocks = new HashSet<>();
        Map<Vector3ic, Block> result = new HashMap<>(blocks.size());

        Vector3i chunkPos = new Vector3i();
        Vector3i relativePos = new Vector3i();
        for (Map.Entry<? extends Vector3ic, Block> entry : blocks.entrySet()) {
            Vector3ic worldPos = entry.getKey();
            Chunks.toChunkPos(worldPos, chunkPos);
            Chunk chunk = chunkProvider.getChunk(chunkPos);

            if (chunk != null) {
                Block type = entry.getValue();
               Chunks.toRelative(worldPos, relativePos);
                Block oldBlockType = chunk.setBlock(relativePos, type);
                if (oldBlockType != type) {
                    BlockChange oldChange = blockChanges.get(worldPos);
                    if (oldChange == null) {
                        blockChanges.put(new Vector3i(worldPos), new BlockChange(worldPos, oldBlockType, type));
                    } else {
                        oldChange.setTo(type);
                    }
                    setDirtyChunksNear(worldPos);
                    changedBlocks.add(new BlockChange(worldPos, oldBlockType, type));
                }
                result.put(worldPos, oldBlockType);
            } else {
                result.put(worldPos, null);
            }
        }

        for (BlockChange change : changedBlocks) {
            notifyBlockChanged(change.getPosition(), change.getTo(), change.getFrom());
        }

        return result;
    }

    private void setDirtyChunksNear(Vector3ic worldPos) {
        BlockRegion tmpRegion = new BlockRegion(worldPos).expand(1, 1, 1);
        for (Vector3ic pos : Chunks.toChunkRegion(tmpRegion, tmpRegion)) {
            Chunk dirtiedChunk = chunkProvider.getChunk(pos);
            if (dirtiedChunk != null) {
                dirtiedChunk.setDirty(true);
            }
        }
    }

    private void notifyBlockChanged(Vector3ic pos, Block type, Block oldType) {
        // TODO: Could use a read/write writeLock.
        // TODO: Review, should only happen on main thread (as should changes to listeners)
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onBlockChanged(pos, type, oldType);
            }
        }
    }

    private void notifyExtraDataChanged(int index, Vector3ic pos, int newData, int oldData) {
        // TODO: Change to match block , if those changes are made.
        synchronized (listeners) {
            for (WorldChangeListener listener : listeners) {
                listener.onExtraDataChanged(index, pos, newData, oldData);
            }
        }
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        Chunk chunk = chunkProvider.getChunk(Chunks.toChunkPos(x, y, z, new Vector3i()));
        if (chunk != null) {
            return chunk.getBlock(Chunks.toRelativeX(x), Chunks.toRelativeY(y), Chunks.toRelativeZ(z));
        }
        return unloadedBlock;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = Chunks.toChunkPos(x, y, z, new Vector3i());
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = Chunks.toRelative(x, y, z, new Vector3i());
            return chunk.getLight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = Chunks.toChunkPos(x, y, z, new Vector3i());
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = Chunks.toRelative(x, y, z, new Vector3i());
            return chunk.getSunlight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = Chunks.toChunkPos(x, y, z, new Vector3i());
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = Chunks.toRelative(x, y, z, new Vector3i());
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        return 0;
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        Chunk chunk = chunkProvider.getChunk(Chunks.toChunkPos(x, y, z, new Vector3i()));
        if (chunk != null) {
            return chunk.getExtraData(index, Chunks.toRelative(x, y, z, new Vector3i()));
        }
        return 0;
    }

    @Override
    public int setExtraData(int index, Vector3ic worldPos, int value) {
        Vector3i chunkPos = Chunks.toChunkPos(worldPos, new Vector3i());
        Chunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = Chunks.toRelative(worldPos, new Vector3i());
            int oldValue = chunk.getExtraData(index, blockPos.x, blockPos.y, blockPos.z);
            chunk.setExtraData(index, blockPos.x, blockPos.y, blockPos.z, value);
            if (oldValue != value) {
                setDirtyChunksNear(worldPos);
                notifyExtraDataChanged(index, worldPos, value, oldValue);
            }
            return oldValue;
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
    public Collection<BlockRegionc> getRelevantRegions() {
        Collection<Chunk> chunks = chunkProvider.getAllChunks();
        Predicate<Chunk> isReady = Chunk::isReady;

        return FluentIterable.from(chunks).filter(isReady).transform(Chunk::getRegion).toList();
    }
}
