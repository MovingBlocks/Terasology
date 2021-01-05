// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.world.internal;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3ic;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.ChunkMath;
import org.terasology.math.JomlUtil;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldChangeListener;
import org.terasology.world.WorldComponent;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionc;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.chunks.LitChunk;
import org.terasology.world.chunks.ManagedChunk;
import org.terasology.world.chunks.RenderableChunk;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
public class WorldProviderCoreImpl implements WorldProviderCore {

    private String title;
    private String customTitle;
    private String seed = "";
    private SimpleUri worldGenerator;

    private ChunkProvider chunkProvider;
    private WorldTime worldTime;
    private EntityManager entityManager;

    private final List<WorldChangeListener> listeners = Lists.newArrayList();

    private final Map<Vector3i, BlockChange> blockChanges = Maps.newHashMap();
    private List<BatchPropagator> propagators = Lists.newArrayList();

    private Block unloadedBlock;

    public WorldProviderCoreImpl(String title, String customTitle, String seed, long time, SimpleUri worldGenerator,
                                 ChunkProvider chunkProvider, Block unloadedBlock, Context context) {
        this.title = (title == null) ? seed : title;
        this.customTitle = customTitle;
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

    public WorldProviderCoreImpl(WorldInfo info, ChunkProvider chunkProvider, Block unloadedBlock,
                                 Context context) {
        this(info.getTitle(), info.getCustomTitle(), info.getSeed(), info.getTime(), info.getWorldGenerator(), chunkProvider,
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
        return new WorldInfo(title, customTitle, seed, worldTime.getMilliseconds(), worldGenerator);
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
    public boolean isRegionRelevant(BlockRegionc region) {
        for (Vector3ic chunkPos : Chunks.toChunkRegion(region, new BlockRegion(BlockRegion.INVALID))) {
            if (!chunkProvider.isChunkReady(chunkPos)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Block setBlock(Vector3i worldPos, Block type) {
       return this.setBlock(JomlUtil.from(worldPos),type);
    }

    @Override
    public Block setBlock(Vector3ic worldPos, Block type) {
        /*
         * Hint: This method has a benchmark available in the BenchmarkScreen, The screen can be opened ingame via the
         * command "showSCreen BenchmarkScreen".
         */
        Vector3i chunkPos = ChunkMath.calcChunkPos(JomlUtil.from(worldPos));
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcRelativeBlockPos(JomlUtil.from(worldPos));
            Block oldBlockType = chunk.setBlock(blockPos, type);
            if (oldBlockType != type) {
                BlockChange oldChange = blockChanges.get(JomlUtil.from(worldPos));
                if (oldChange == null) {
                    blockChanges.put(JomlUtil.from(worldPos), new BlockChange(worldPos, oldBlockType, type));
                } else {
                    oldChange.setTo(type);
                }
                setDirtyChunksNear(JomlUtil.from(worldPos));
                notifyBlockChanged(worldPos, type, oldBlockType);
            }
            return oldBlockType;

        }
        return null;
    }

    @Override
    public Map<Vector3i, Block> setBlocks(Map<Vector3i, Block> blocks) {
        /*
         * Hint: This method has a benchmark available in the BenchmarkScreen, The screen can be opened ingame via the
         * command "showSCreen BenchmarkScreen".
         */
        Set<BlockChange> changedBlocks = new HashSet<>();
        Map<Vector3i, Block> result = new HashMap<>(blocks.size());

        for (Map.Entry<Vector3i, Block> entry : blocks.entrySet()) {
            Vector3i worldPos = entry.getKey();
            Vector3i chunkPos = ChunkMath.calcChunkPos(worldPos);
            CoreChunk chunk = chunkProvider.getChunk(chunkPos);

            if (chunk != null) {
                Block type = entry.getValue();
                Vector3i blockPos = ChunkMath.calcRelativeBlockPos(worldPos);
                Block oldBlockType = chunk.setBlock(blockPos, type);
                if (oldBlockType != type) {
                    BlockChange oldChange = blockChanges.get(worldPos);
                    if (oldChange == null) {
                        blockChanges.put(worldPos, new BlockChange(JomlUtil.from(worldPos), oldBlockType, type));
                    } else {
                        oldChange.setTo(type);
                    }
                    setDirtyChunksNear(worldPos);
                    changedBlocks.add(new BlockChange(JomlUtil.from(worldPos), oldBlockType, type));
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

    private void setDirtyChunksNear(Vector3i pos0) {
        for (Vector3i pos : ChunkMath.getChunkRegionAroundWorldPos(pos0, 1)) {
            RenderableChunk dirtiedChunk = chunkProvider.getChunk(pos);
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
        CoreChunk chunk = chunkProvider.getChunk(ChunkMath.calcChunkPosX(x), ChunkMath.calcChunkPosY(y), ChunkMath.calcChunkPosZ(z));
        if (chunk != null) {
            return chunk.getBlock(ChunkMath.calcBlockPosX(x), ChunkMath.calcBlockPosY(y), ChunkMath.calcBlockPosZ(z));
        }
        return unloadedBlock;
    }

    @Override
    public byte getLight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcRelativeBlockPos(x, y, z);
            return chunk.getLight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getSunlight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcRelativeBlockPos(x, y, z);
            return chunk.getSunlight(blockPos);
        }
        return 0;
    }

    @Override
    public byte getTotalLight(int x, int y, int z) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(x, y, z);
        LitChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcRelativeBlockPos(x, y, z);
            return (byte) Math.max(chunk.getSunlight(blockPos), chunk.getLight(blockPos));
        }
        return 0;
    }

    @Override
    public int getExtraData(int index, int x, int y, int z) {
        CoreChunk chunk = chunkProvider.getChunk(ChunkMath.calcChunkPosX(x), ChunkMath.calcChunkPosY(y), ChunkMath.calcChunkPosZ(z));
        if (chunk != null) {
            return chunk.getExtraData(index, ChunkMath.calcBlockPosX(x), ChunkMath.calcBlockPosY(y), ChunkMath.calcBlockPosZ(z));
        }
        return 0;
    }

    @Override
    public int setExtraData(int index, Vector3i worldPos, int value) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(worldPos);
        CoreChunk chunk = chunkProvider.getChunk(chunkPos);
        if (chunk != null) {
            Vector3i blockPos = ChunkMath.calcRelativeBlockPos(worldPos);
            int oldValue = chunk.getExtraData(index, blockPos.x, blockPos.y, blockPos.z);
            chunk.setExtraData(index, blockPos.x, blockPos.y, blockPos.z, value);
            if (oldValue != value) {
                setDirtyChunksNear(worldPos);
                notifyExtraDataChanged(index, JomlUtil.from(worldPos), value, oldValue);
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
    public Collection<BlockRegion> getRelevantRegions() {
        Collection<Chunk> chunks = chunkProvider.getAllChunks();
        Function<Chunk, BlockRegion> mapping = CoreChunk::getRegion;

        Predicate<Chunk> isReady = ManagedChunk::isReady;

        return FluentIterable.from(chunks).filter(isReady).transform(mapping).toList();
    }
}
