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
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.BeforeDeactivateBlocks;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.OnActivatedBlocks;
import org.terasology.world.block.OnAddedBlocks;
import org.terasology.world.chunks.Chunk;
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
import org.terasology.world.generation.World;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Immortius
 */
public class LocalChunkProvider implements ChunkProvider, GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);
    private static final int LOAD_PER_FRAME = 2;
    private static final int UNLOAD_PER_FRAME = 64;
    private static final Vector3i UNLOAD_LEEWAY = Vector3i.one();

    private StorageManager storageManager;

    private ChunkGenerationPipeline pipeline;
    private TaskMaster<ChunkUnloadRequest> unloadRequestTaskMaster;
    private World generator;

    private Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();

    private Map<Vector3i, Chunk> nearCache = Maps.newConcurrentMap();

    private final Set<Vector3i> preparingChunks = Sets.newHashSet();
    private final BlockingQueue<ReadyChunkInfo> readyChunks = Queues.newLinkedBlockingQueue();
    private List<ReadyChunkInfo> sortedReadyChunks = Lists.newArrayList();
    private final BlockingQueue<TShortObjectMap<TIntList>> deactivateBlocksQueue = Queues.newLinkedBlockingQueue();

    private EntityRef worldEntity = EntityRef.NULL;

    private ReadWriteLock regionLock = new ReentrantReadWriteLock();

    private BlockManager blockManager;
    private BlockEntityRegistry registry;

    private LightMerger lightMerger = new LightMerger(this);

    public LocalChunkProvider(StorageManager storageManager, World generator) {
        blockManager = CoreRegistry.get(BlockManager.class);
        this.storageManager = storageManager;
        this.generator = generator;
        this.pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 4);
        ChunkMonitor.fireChunkProviderInitialized(this);
    }

    public void setBlockEntityRegistry(BlockEntityRegistry value) {
        this.registry = value;
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
        return createWorldView(region, new Vector3i(-region.min().x, -region.min().y, -region.min().z));
    }

    @Override
    public ChunkViewCore getSubviewAroundChunk(Vector3i chunkPos) {
        Region3i region = Region3i.createFromCenterExtents(chunkPos, ChunkConstants.LOCAL_REGION_EXTENTS);
        if (getChunk(chunkPos) != null) {
            return createWorldView(region, new Vector3i(-region.min().x, -region.min().y, -region.min().z));
        }
        return null;
    }

    private ChunkViewCore createWorldView(Region3i region, Vector3i offset) {
        Chunk[] chunks = new Chunk[region.sizeX() * region.sizeY() * region.sizeZ()];
        for (Vector3i chunkPos : region) {
            Chunk chunk = nearCache.get(chunkPos);
            if (chunk == null || !chunk.isReady()) {
                return null;
            }
            chunkPos.sub(region.minX(), region.minY(), region.minZ());
            int index = TeraMath.calculate3DArrayIndex(chunkPos, region.size());
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset);
    }

    public void setWorldEntity(EntityRef worldEntity) {
        this.worldEntity = worldEntity;
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, Vector3i distance) {
        addRelevanceEntity(entity, distance, null);
    }

    @Override
    public void addRelevanceEntity(EntityRef entity, Vector3i distance, ChunkRegionListener listener) {
        if (!entity.exists()) {
            return;
        }
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance);
                return;
            }
        } finally {
            regionLock.readLock().unlock();
        }

        ChunkRelevanceRegion region = new ChunkRelevanceRegion(entity, distance);
        if (listener != null) {
            region.setListener(listener);
        }
        regionLock.writeLock().lock();
        try {
            regions.put(entity, region);
        } finally {
            regionLock.writeLock().unlock();
        }
        for (Vector3i pos : region.getCurrentRegion()) {
            Chunk chunk = getChunk(pos);
            if (chunk != null) {
                region.chunkReady(chunk);
            } else {
                createOrLoadChunk(pos);
            }
        }
    }

    @Override
    public void updateRelevanceEntity(EntityRef entity, Vector3i distance) {
        regionLock.readLock().lock();
        try {
            ChunkRelevanceRegion region = regions.get(entity);
            if (region != null) {
                region.setRelevanceDistance(distance);
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
            deactivateBlocks();
            checkForUnload();
        } finally {
            regionLock.readLock().unlock();
        }
    }

    private void makeChunksAvailable() {
        List<ReadyChunkInfo> newReadyChunks = Lists.newArrayListWithExpectedSize(readyChunks.size());
        readyChunks.drainTo(newReadyChunks);
        for (ReadyChunkInfo readyChunkInfo : newReadyChunks) {
            nearCache.put(readyChunkInfo.getPos(), readyChunkInfo.getChunk());
            preparingChunks.remove(readyChunkInfo.getPos());
        }
        if (!newReadyChunks.isEmpty()) {
            sortedReadyChunks.addAll(newReadyChunks);
            Collections.sort(sortedReadyChunks, new ReadyChunkRelevanceComparator());
        }
        if (!sortedReadyChunks.isEmpty()) {
            int loaded = 0;
            for (int i = sortedReadyChunks.size() - 1; i >= 0 && loaded < LOAD_PER_FRAME; i--) {
                ReadyChunkInfo chunkInfo = sortedReadyChunks.get(i);
                PerformanceMonitor.startActivity("Make Chunk Available");
                if (makeChunkAvailable(chunkInfo)) {
                    sortedReadyChunks.remove(i);
                    loaded++;
                }
                PerformanceMonitor.endActivity();
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
        PerformanceMonitor.startActivity("Unloading irrelevant chunks");
        int unloaded = 0;
        logger.debug("Compacting cache");
        Iterator<Vector3i> iterator = nearCache.keySet().iterator();
        while (iterator.hasNext()) {
            Vector3i pos = iterator.next();
            boolean keep = false;
            for (ChunkRelevanceRegion region : regions.values()) {
                if (region.getCurrentRegion().expand(UNLOAD_LEEWAY).encompasses(pos)) {
                    keep = true;
                    break;
                }
            }
            if (!keep) {
                // TODO: need some way to not dispose chunks being edited or processed (or do so safely)
                // Note: Above won't matter if all changes are on the main thread
                Chunk chunk = nearCache.get(pos);
                if (chunk.isLocked()) {
                    continue;
                }
                chunk.lock();
                try {
                    if (!chunk.isReady()) {
                        // Chunk hasn't been finished or changed, so just drop it.
                        iterator.remove();
                        Iterator<ReadyChunkInfo> infoIterator = sortedReadyChunks.iterator();
                        while (infoIterator.hasNext()) {
                            ReadyChunkInfo next = infoIterator.next();
                            if (next.getPos().equals(chunk.getPosition())) {
                                infoIterator.remove();
                                break;
                            }
                        }
                        continue;
                    }
                    worldEntity.send(new BeforeChunkUnload(pos));
                    for (ChunkRelevanceRegion region : regions.values()) {
                        region.chunkUnloaded(pos);
                    }
                    ChunkStore store = storageManager.createChunkStoreForSave(chunk);
                    store.storeAllEntities();
                    store.save();

                    chunk.dispose();
                    try {
                        unloadRequestTaskMaster.put(new ChunkUnloadRequest(chunk, this));
                    } catch (InterruptedException e) {
                        logger.error("Failed to enqueue unload request for {}", chunk.getPosition(), e);
                    }
                    iterator.remove();
                    if (++unloaded >= UNLOAD_PER_FRAME) {
                        break;
                    }
                } finally {
                    chunk.unlock();
                }
            }
        }
        PerformanceMonitor.endActivity();
    }

    private void updateRelevance() {
        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            chunkRelevanceRegion.update();
            if (chunkRelevanceRegion.isDirty()) {
                for (Vector3i pos : chunkRelevanceRegion.getNeededChunks()) {
                    Chunk chunk = nearCache.get(pos);
                    if (chunk != null && chunk.isReady()) {
                        chunkRelevanceRegion.chunkReady(chunk);
                    } else if (chunk == null) {
                        createOrLoadChunk(pos);
                    }
                }
                chunkRelevanceRegion.setUpToDate();
            }
        }
    }

    private boolean makeChunkAvailable(ReadyChunkInfo readyChunkInfo) {
        Chunk chunk = nearCache.get(readyChunkInfo.getPos());
        if (chunk == null) {
            return false;
        }
        for (Vector3i pos : Region3i.createFromCenterExtents(readyChunkInfo.getPos(), 1)) {
            if (nearCache.get(pos) == null) {
                return false;
            }
        }

        chunk.lock();
        try {
            chunk.markReady();
            if (!readyChunkInfo.isNewChunk()) {
                PerformanceMonitor.startActivity("Generating Block Entities");
                generateBlockEntities(chunk);
                PerformanceMonitor.endActivity();
            }
            if (readyChunkInfo.getChunkStore() != null) {
                readyChunkInfo.getChunkStore().restoreEntities();
            }

            if (!readyChunkInfo.isNewChunk()) {
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

            lightMerger.merge(chunk);

            if (!readyChunkInfo.isNewChunk()) {
                worldEntity.send(new OnChunkGenerated(readyChunkInfo.getPos()));
            }
            worldEntity.send(new OnChunkLoaded(readyChunkInfo.getPos()));
            for (ChunkRelevanceRegion region : regions.values()) {
                region.chunkReady(chunk);
            }
        } finally {
            chunk.unlock();
        }
        return true;
    }

    // Generates all non-temporary block entities
    private void generateBlockEntities(Chunk chunk) {
        ChunkBlockIterator i = chunk.getBlockIterator();
        while (i.next()) {
            if (i.getBlock().isKeepActive()) {
                registry.getBlockEntityAt(i.getBlockPos());
            }
        }
    }

    void gatherBlockPositionsForDeactivate(Chunk chunk) {
        try {
            deactivateBlocksQueue.put(createBatchBlockEventMappings(chunk));
        } catch (InterruptedException e) {
            logger.error("Failed to queue deactivation of blocks for {}", chunk.getPosition());
        }
    }

    private TShortObjectMap<TIntList> createBatchBlockEventMappings(Chunk chunk) {
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
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3i pos) {
        Chunk chunk = nearCache.get(pos);
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

        for (Chunk chunk : nearCache.values()) {
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

        for (Chunk chunk : nearCache.values()) {
            if (chunk.isReady()) {
                worldEntity.send(new BeforeChunkUnload(chunk.getPosition()));
                chunk.dispose();
                ChunkStore store = storageManager.createChunkStoreForSave(chunk);
                store.storeAllEntities();
                store.save();
            }
        }
        nearCache.clear();
        storageManager.purgeChunks();

        worldEntity.send(new PurgeWorldEvent());

        this.pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        ChunkMonitor.fireChunkProviderInitialized(this);

        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            for (Vector3i pos : chunkRelevanceRegion.getCurrentRegion()) {
                createOrLoadChunk(pos);
            }
            chunkRelevanceRegion.setUpToDate();
        }
    }

    private void createOrLoadChunk(Vector3i chunkPos) {
        Chunk chunk = nearCache.get(chunkPos);
        if (chunk == null && !preparingChunks.contains(chunkPos)) {
            preparingChunks.add(chunkPos);
            pipeline.doTask(new AbstractChunkTask(chunkPos) {
                @Override
                public String getName() {
                    return "Create or Load Chunk";
                }

                @Override
                public void run() {
                    ChunkStore chunkStore = storageManager.loadChunkStore(getPosition());
                    Chunk chunk;
                    if (chunkStore == null) {
                        chunk = new ChunkImpl(getPosition());
                        generator.rasterizeChunk(chunk);
                    } else {
                        chunk = chunkStore.getChunk();
                    }

                    InternalLightProcessor.generateInternalLighting(chunk);
                    chunk.deflate();
                    readyChunks.offer(new ReadyChunkInfo(chunk, createBatchBlockEventMappings(chunk), chunkStore));
                }
            });
        }
    }


    @Override
    public void onChunkIsReady(Chunk chunk) {
        readyChunks.offer(new ReadyChunkInfo(chunk, createBatchBlockEventMappings(chunk)));
    }

    @Override
    public World getWorldGenerator() {
        return generator;
    }

    @Override
    public Chunk getChunkUnready(Vector3i pos) {
        return nearCache.get(pos);
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        return isChunkReady(nearCache.get(pos));
    }

    private boolean isChunkReady(Chunk chunk) {
        return chunk != null && chunk.isReady();
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

private class ReadyChunkRelevanceComparator implements Comparator<ReadyChunkInfo> {

    @Override
    public int compare(ReadyChunkInfo o1, ReadyChunkInfo o2) {
        return score(o2.getPos()) - score(o1.getPos());
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
