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

package org.terasology.world.chunks.localChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import gnu.trove.procedure.TShortObjectProcedure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.ChunkMonitor;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.StorageManager;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BeforeDeactivateBlocks;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.OnActivatedBlocks;
import org.terasology.world.block.OnAddedBlocks;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkGenerated;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.event.PurgeWorldEvent;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.world.chunks.internal.GeneratingChunkProvider;
import org.terasology.world.chunks.internal.ReadyChunkInfo;
import org.terasology.world.chunks.pipeline.AbstractChunkTask;
import org.terasology.world.chunks.pipeline.ChunkGenerationPipeline;
import org.terasology.world.chunks.pipeline.ChunkTask;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.BatchPropagator;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.LightPropagationRules;
import org.terasology.world.propagation.light.LightWorldView;
import org.terasology.world.propagation.light.SunlightPropagationRules;
import org.terasology.world.propagation.light.SunlightWorldView;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements ChunkProvider, GeneratingChunkProvider {

    // TODO: Dynamically calculate this
    private static final int CACHE_SIZE = (int) (1.5 * Runtime.getRuntime().maxMemory() / 1048576);

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);

    private StorageManager storageManager;

    private ChunkGenerationPipeline pipeline;
    private TaskMaster<ChunkUnloadRequest> unloadRequestTaskMaster;
    private WorldGenerator generator;

    private Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();

    private ConcurrentMap<Vector3i, ChunkImpl> nearCache = Maps.newConcurrentMap();

    private final Set<Vector3i> preparingChunks = Sets.newSetFromMap(Maps.<Vector3i, Boolean>newConcurrentMap());
    private final BlockingQueue<ReadyChunkInfo> readyChunks = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<TShortObjectMap<TIntList>> deactivateBlocksQueue = Queues.newLinkedBlockingQueue();

    private EntityRef worldEntity = EntityRef.NULL;

    private ReadWriteLock regionLock = new ReentrantReadWriteLock();

    private BlockManager blockManager;
    private BlockEntityRegistry registry;

    private boolean forceCleanup;

    private List<BatchPropagator> loadEdgePropagators = Lists.newArrayList();

    public LocalChunkProvider(StorageManager storageManager, WorldGenerator generator) {
        blockManager = CoreRegistry.get(BlockManager.class);
        this.storageManager = storageManager;
        this.generator = generator;
        this.pipeline = new ChunkGenerationPipeline(this, generator, new ChunkTaskRelevanceComparator());
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        ChunkMonitor.fireChunkProviderInitialized(this);

        logger.info("CACHE_SIZE = {} for nearby chunks", CACHE_SIZE);

        loadEdgePropagators.add(new BatchPropagator(new LightPropagationRules(), new LightWorldView(this)));
        loadEdgePropagators.add(new BatchPropagator(new SunlightPropagationRules(), new SunlightWorldView(this)));
    }

    public void setBlockEntityRegistry(BlockEntityRegistry value) {
        this.registry = value;
    }

    public void requestCleanup() {
        forceCleanup = true;
    }

    @Override
    public ChunkViewCore getLocalView(Vector3i centerChunkPos) {
        Region3i region = Region3i.createFromCenterExtents(centerChunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(centerChunkPos) != null) {
            return createWorldView(region, Vector3i.one());
        }
        return null;
    }

    @Override
    public ChunkViewCore getSubviewAroundBlock(Vector3i blockPos, int extent) {
        Region3i region = TeraMath.getChunkRegionAroundWorldPos(blockPos, extent);
        return createWorldView(region, new Vector3i(-region.min().x, 0, -region.min().z));
    }

    @Override
    public ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos) {
        Region3i region = Region3i.createFromCenterExtents(chunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(chunkPos) != null) {
            return createWorldView(region, new Vector3i(-region.min().x, 0, -region.min().z));
        }
        return null;
    }

    private ChunkViewCore createWorldView(Region3i region, Vector3i offset) {
        ChunkImpl[] chunks = new ChunkImpl[region.size().x * region.size().y * region.size().z];
        for (Vector3i chunkPos : region) {
            ChunkImpl chunk = nearCache.get(chunkPos);
            if (chunk == null || chunk.getChunkState() != ChunkImpl.State.COMPLETE) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset);
    }

    public void setWorldEntity(EntityRef worldEntity) {
        this.worldEntity = worldEntity;
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance) {
        addRelevanceEntity(entity, distance, null);
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, int distance, ChunkRegionListener listener) {
        if (!entity.exists()) {
            return;
        }
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance + ChunkConstants.FULL_GENERATION_DISTANCE);
                return;
            }
        } finally {
            regionLock.readLock().unlock();
        }

        ChunkRelevanceRegion region = new ChunkRelevanceRegion(entity, distance + ChunkConstants.FULL_GENERATION_DISTANCE);
        if (listener != null) {
            region.setListener(listener);
        }
        regionLock.writeLock().lock();
        try {
            regions.put(entity, region);
        } finally {
            regionLock.writeLock().unlock();
        }
        for (Vector3i pos : region.getRegion()) {
            ChunkImpl chunk = getChunk(pos);
            if (chunk != null) {
                region.chunkReady(chunk);
            }
        }
        pipeline.requestProduction(region.getRegion().expand(new Vector3i(2, 0, 2)));
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, int distance) {
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance + ChunkConstants.FULL_GENERATION_DISTANCE);
            }
        } finally {
            regionLock.readLock().unlock();
        }
    }

    @Override
    public void removeRelevanceEntity(EntityRef entity) {
        regionLock.writeLock().lock();
        try {
            regions.remove(entity);
        } finally {
            regionLock.writeLock().unlock();
        }
    }

    @Override
    public void update() {
        regionLock.readLock().lock();
        try {
            updateRelevance();
            makeChunksAvailable();
            checkForUnload();
            deactivateBlocks();
        } finally {
            regionLock.readLock().unlock();
        }
    }

    private void makeChunksAvailable() {
        ReadyChunkInfo readyChunkInfo = readyChunks.poll();
        if (readyChunkInfo != null) {
            makeChunkAvailable(readyChunkInfo);
            for (BatchPropagator propagator : loadEdgePropagators) {
                propagator.process();
            }
        }
    }

    private void deactivateBlocks() {
        List<TShortObjectMap<TIntList>> deactivatedBlockSets = Lists.newArrayListWithExpectedSize(deactivateBlocksQueue.size());
        deactivateBlocksQueue.drainTo(deactivatedBlockSets);
        for (TShortObjectMap<TIntList> deactivatedBlockSet : deactivatedBlockSets) {
            deactivatedBlockSet.forEachEntry(new TShortObjectProcedure<TIntList>() {
                @Override
                public boolean execute(short id, TIntList positions) {
                    if (positions.size() > 0) {
                        blockManager.getBlock(id).getEntity().send(new BeforeDeactivateBlocks(positions, registry));
                    }
                    return true;
                }
            });
        }
    }

    private void checkForUnload() {
        PerformanceMonitor.startActivity("Review cache size");
        if (nearCache.size() > CACHE_SIZE || forceCleanup) {
            forceCleanup = false;
            logger.debug("Compacting cache");
            Iterator<Vector3i> iterator = nearCache.keySet().iterator();
            while (iterator.hasNext()) {
                Vector3i pos = iterator.next();
                boolean keep = false;
                for (ChunkRelevanceRegion region : regions.values()) {
                    if (region.getRegion().expand(new Vector3i(4, 0, 4)).encompasses(pos)) {
                        keep = true;
                        break;
                    }
                }
                if (!keep) {
                    // TODO: need some way to not dispose chunks being edited or processed (or do so safely)
                    // Note: Above won't matter if all changes are on the main thread
                    ChunkImpl chunk = nearCache.get(pos);
                    if (chunk.isLocked()) {
                        continue;
                    }
                    chunk.lock();
                    try {
                        if (chunk.getChunkState() == ChunkImpl.State.COMPLETE && !chunk.isReady()) {
                            // Chunk is complete, but hasn't had events sent out/entities loaded.
                            continue;
                        }
                        if (chunk.getChunkState() == ChunkImpl.State.COMPLETE) {
                            worldEntity.send(new BeforeChunkUnload(pos));
                            for (ChunkRelevanceRegion region : regions.values()) {
                                region.chunkUnloaded(pos);
                            }
                        }
                        ChunkStore store = storageManager.createChunkStoreForSave(chunk);
                        store.storeAllEntities();
                        store.save();

                        chunk.dispose();

                        try {
                            unloadRequestTaskMaster.put(new ChunkUnloadRequest(chunk, this));
                        } catch (InterruptedException e) {
                            logger.error("Failed to enqueue unload request for {}", chunk.getPos(), e);
                        }
                        iterator.remove();
                    } finally {
                        chunk.unlock();
                    }
                }
            }
        }
        PerformanceMonitor.endActivity();
    }

    private void updateRelevance() {
        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            chunkRelevanceRegion.update();
            if (chunkRelevanceRegion.isDirty()) {
                boolean produceChunks = false;
                for (Vector3i pos : chunkRelevanceRegion.getNeededChunks()) {
                    ChunkImpl chunk = nearCache.get(pos);
                    if (chunk != null && chunk.getChunkState() == ChunkImpl.State.COMPLETE) {
                        chunkRelevanceRegion.chunkReady(chunk);
                    } else {
                        produceChunks = true;
                    }
                }
                if (produceChunks) {
                    pipeline.requestProduction(chunkRelevanceRegion.getRegion().expand(new Vector3i(2, 0, 2)));
                }
                chunkRelevanceRegion.setUpToDate();
            }
        }
    }

    private void makeChunkAvailable(ReadyChunkInfo readyChunkInfo) {
        ChunkImpl chunk = getChunk(readyChunkInfo.getPos());
        if (chunk == null) {
            return;
        }
        chunk.lock();
        try {
            chunk.markReady();
            boolean loaded = chunk.isInitialGenerationComplete();
            if (!loaded) {
                chunk.setInitialGenerationComplete();
                PerformanceMonitor.startActivity("Generating Block Entities");
                generateBlockEntities(chunk);
                PerformanceMonitor.endActivity();
            }
            if (readyChunkInfo.getChunkStore() != null) {
                readyChunkInfo.getChunkStore().restoreEntities();
            }

            if (!loaded) {
                PerformanceMonitor.startActivity("Sending OnAddedBlocks");
                readyChunkInfo.getBlockPositionMapppings().forEachEntry(new TShortObjectProcedure<TIntList>() {
                    @Override
                    public boolean execute(short id, TIntList positions) {
                        if (positions.size() > 0) {
                            blockManager.getBlock(id).getEntity().send(new OnAddedBlocks(positions, registry));
                        }
                        return true;
                    }
                });
                PerformanceMonitor.endActivity();
            }

            PerformanceMonitor.startActivity("Sending OnActivateBlocks");
            readyChunkInfo.getBlockPositionMapppings().forEachEntry(new TShortObjectProcedure<TIntList>() {
                @Override
                public boolean execute(short id, TIntList positions) {
                    if (positions.size() > 0) {
                        blockManager.getBlock(id).getEntity().send(new OnActivatedBlocks(positions, registry));
                    }
                    return true;
                }
            });
            PerformanceMonitor.endActivity();

            for (Side side : Side.horizontalSides()) {
                Vector3i adjChunkPos = side.getAdjacentPos(readyChunkInfo.getPos());
                ChunkImpl adjChunk = getChunk(adjChunkPos);
                if (adjChunk != null) {
                    for (BatchPropagator propagator : loadEdgePropagators) {
                        propagator.propagateBetween(chunk, adjChunk, side);
                    }
                }
            }

            if (!loaded) {
                worldEntity.send(new OnChunkGenerated(readyChunkInfo.getPos()));
            }
            worldEntity.send(new OnChunkLoaded(readyChunkInfo.getPos()));
            for (ChunkRelevanceRegion region : regions.values()) {
                region.chunkReady(chunk);
            }
        } finally {
            chunk.unlock();
        }
    }

    // Generates all non-temporary block entities
    private void generateBlockEntities(ChunkImpl chunk) {
        ChunkBlockIterator i = chunk.getBlockIterator();
        while (i.next()) {
            if (i.getBlock().isKeepActive()) {
                registry.getBlockEntityAt(i.getBlockPos());
            }
        }
    }

    void gatherBlockPositionsForDeactivate(ChunkImpl chunk) {
        try {
            deactivateBlocksQueue.put(createBatchBlockEventMappings(chunk));
        } catch (InterruptedException e) {
            logger.error("Failed to queue deactivation of blocks for {}", chunk.getPos());
        }
    }

    private TShortObjectMap<TIntList> createBatchBlockEventMappings(ChunkImpl chunk) {
        TShortObjectMap<TIntList> batchBlockMap = new TShortObjectHashMap<>();
        for (Block block : blockManager.listRegisteredBlocks()) {
            if (block.isLifecycleEventsRequired()) {
                batchBlockMap.put(block.getId(), new TIntArrayList());
            }
        }

        ChunkBlockIterator i = chunk.getBlockIterator();
        while (i.next()) {
            if (i.getBlock().isLifecycleEventsRequired()) {
                TIntList positionList = batchBlockMap.get(i.getBlock().getId());
                positionList.add(i.getBlockPos().x);
                positionList.add(i.getBlockPos().y);
                positionList.add(i.getBlockPos().z);
            }
        }
        return batchBlockMap;
    }

    @Override
    public ChunkImpl getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public ChunkImpl getChunk(Vector3i pos) {
        ChunkImpl chunk = nearCache.get(pos);
        if (isChunkReady(chunk)) {
            return chunk;
        }
        return null;
    }

    @Override
    public void dispose() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        pipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);

        for (ChunkImpl chunk : nearCache.values()) {
            chunk.dispose();
            ChunkStore store = storageManager.createChunkStoreForSave(chunk);
            store.storeAllEntities();
            store.save();
        }
        nearCache.clear();

    }

    @Override
    public void purgeChunks() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        pipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);

        for (ChunkImpl chunk : nearCache.values()) {
            if (chunk.getChunkState() == ChunkImpl.State.COMPLETE && chunk.isReady()) {
                worldEntity.send(new BeforeChunkUnload(chunk.getPos()));
            }
            chunk.dispose();
            ChunkStore store = storageManager.createChunkStoreForSave(chunk);
            store.storeAllEntities();
            store.save();
        }
        nearCache.clear();
        storageManager.purgeChunks();
        
        worldEntity.send(new PurgeWorldEvent());

        this.pipeline = new ChunkGenerationPipeline(this, generator, new ChunkTaskRelevanceComparator());
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        ChunkMonitor.fireChunkProviderInitialized(this);

        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            pipeline.requestProduction(chunkRelevanceRegion.getRegion().expand(new Vector3i(2, 0, 2)));
            chunkRelevanceRegion.setUpToDate();
        }
    }

    @Override
    public ChunkViewCore getViewAround(Vector3i pos) {
        Region3i region = Region3i.createFromCenterExtents(pos, new Vector3i(1, 0, 1));
        ChunkImpl[] chunks = new ChunkImpl[region.size().x * region.size().z];
        for (Vector3i chunkPos : region) {
            ChunkImpl chunk = getChunkForProcessing(chunkPos);
            if (chunk == null) {
                return null;
            }
            int index = (chunkPos.x - region.min().x) + region.size().x * (chunkPos.z - region.min().z);
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, Vector3i.one());
    }

    @Override
    public ChunkImpl getChunkForProcessing(Vector3i pos) {
        return nearCache.get(pos);
    }

    @Override
    public void createOrLoadChunk(Vector3i chunkPos) {
        ChunkImpl chunk = nearCache.get(chunkPos);
        if (chunk == null) {
            PerformanceMonitor.startActivity("Check chunk in cache");
            if (preparingChunks.add(chunkPos)) {
                if (nearCache.get(chunkPos) != null) {
                    // This happens if the chunk is completed right before checking if it is in preparing chunks. Fun.
                    preparingChunks.remove(chunkPos);
                } else if (storageManager.containsChunkStoreFor(chunkPos)) {
                    pipeline.doTask(new AbstractChunkTask(pipeline, chunkPos, this) {
                        @Override
                        public String getName() {
                            return "Load Chunk";
                        }

                        @Override
                        public void run() {
                            ChunkStore chunkStore = storageManager.loadChunkStore(getPosition());
                            ChunkImpl chunk = chunkStore.getChunk();

                            try {
                                chunk.lock();

                                if (nearCache.putIfAbsent(getPosition(), chunkStore.getChunk()) != null) {
                                    logger.warn("Chunk {} is already in the near cache", getPosition());
                                }
                                preparingChunks.remove(getPosition());
                                if (chunk.getChunkState() == ChunkImpl.State.INTERNAL_LIGHT_GENERATION_PENDING) {
                                    InternalLightProcessor.generateInternalLighting(chunk);
                                    chunk.deflate();
                                    chunk.setChunkState(ChunkImpl.State.COMPLETE);
                                    readyChunks.offer(new ReadyChunkInfo(chunk.getPos(), createBatchBlockEventMappings(chunk), chunkStore));
                                } else {
                                    pipeline.requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
                                }
                            } finally {
                                chunk.unlock();
                            }
                        }
                    });
                } else {
                    pipeline.doTask(new AbstractChunkTask(pipeline, chunkPos, this) {

                        @Override
                        public String getName() {
                            return "Generate Chunk";
                        }

                        @Override
                        public void run() {
                            ChunkImpl chunk = new ChunkImpl(getPosition());
                            generator.createChunk(chunk);
                            if (nearCache.putIfAbsent(getPosition(), chunk) != null) {
                                logger.warn("Chunk {} is already in the near cache", getPosition());
                            }
                            preparingChunks.remove(getPosition());
                            pipeline.requestReview(Region3i.createFromCenterExtents(getPosition(), ChunkConstants.LOCAL_REGION_EXTENTS));
                        }
                    });
                }
            }
            PerformanceMonitor.endActivity();
        }
    }

    @Override
    public void onChunkIsReady(Vector3i position) {
        readyChunks.offer(new ReadyChunkInfo(position, createBatchBlockEventMappings(nearCache.get(position))));
    }

    @Override
    public WorldGenerator getWorldGenerator() {
        return generator;
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        return isChunkReady(nearCache.get(pos));
    }

    private boolean isChunkReady(ChunkImpl chunk) {
        return chunk != null && chunk.getChunkState() == ChunkImpl.State.COMPLETE;
    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1.getPosition()) - score(o2.getPosition());
        }

        private int score(Vector3i chunk) {
            int score = Integer.MAX_VALUE;

            regionLock.readLock().lock();
            try {
                for (ChunkRelevanceRegion region : regions.values()) {
                    int dist = distFromRegion(chunk, region.getCenter());
                    if (dist < score) {
                        score = dist;
                    }
                }
                return score;
            } finally {
                regionLock.readLock().unlock();
            }
        }

        private int distFromRegion(Vector3i pos, Vector3i regionCenter) {
            return pos.gridDistance(regionCenter);
        }
    }
}
