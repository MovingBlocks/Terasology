/*
 * Copyright 2018 MovingBlocks
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.StorageManager;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.BeforeDeactivateBlocks;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.OnActivatedBlocks;
import org.terasology.world.block.OnAddedBlocks;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkBlockIterator;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.ChunkRegionListener;
import org.terasology.world.chunks.ManagedChunk;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
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
import org.terasology.world.generation.impl.EntityBufferImpl;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.InternalLightProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 */
public class LocalChunkProvider implements GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);
    private static final int UNLOAD_PER_FRAME = 64;
    private static final Vector3i UNLOAD_LEEWAY = Vector3i.one();

    private StorageManager storageManager;
    private final EntityManager entityManager;

    private ChunkGenerationPipeline pipeline;
    private TaskMaster<ChunkUnloadRequest> unloadRequestTaskMaster;
    private WorldGenerator generator;

    private Map<EntityRef, ChunkRelevanceRegion> regions = Maps.newHashMap();


    private final Set<Vector3i> preparingChunks = Sets.newHashSet();
    private final BlockingQueue<ReadyChunkInfo> readyChunks = Queues.newLinkedBlockingQueue();
    private List<ReadyChunkInfo> sortedReadyChunks = Lists.newArrayList();
    private final BlockingQueue<TShortObjectMap<TIntList>> deactivateBlocksQueue = Queues.newLinkedBlockingQueue();

    private EntityRef worldEntity = EntityRef.NULL;

    private ReadWriteLock regionLock = new ReentrantReadWriteLock();

    private BlockManager blockManager;
    private BiomeManager biomeManager;
    private ExtraBlockDataManager extraDataManager;
    private final ChunkCache chunkCache;
    private final Supplier<ChunkFinalizer> chunkFinalizerSupplier;
    private BlockEntityRegistry registry;

    private ChunkFinalizer chunkFinalizer;

    //TODO Remove this old constructor at the end of the chunk overhaul
    public LocalChunkProvider(StorageManager storageManager, EntityManager entityManager, WorldGenerator generator,
                              BlockManager blockManager, BiomeManager biomeManager, ExtraBlockDataManager extraDataManager) {
        this(storageManager,
                entityManager,
                generator,
                blockManager,
                biomeManager,
                extraDataManager,
                new LightMergingChunkFinalizer(),
                LightMergingChunkFinalizer::new,
                new ConcurrentMapChunkCache());
    }

    LocalChunkProvider(StorageManager storageManager, EntityManager entityManager, WorldGenerator generator,
                       BlockManager blockManager, BiomeManager biomeManager, ExtraBlockDataManager extraDataManager,
                       ChunkFinalizer chunkFinalizer, Supplier<ChunkFinalizer> chunkFinalizerSupplier,
                       ChunkCache chunkCache) {
        this.storageManager = storageManager;
        this.entityManager = entityManager;
        this.generator = generator;
        this.blockManager = blockManager;
        this.biomeManager = biomeManager;
        this.extraDataManager = extraDataManager;
        this.pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 4);
        this.chunkFinalizer = chunkFinalizer;
        this.chunkCache = chunkCache;
        chunkFinalizer.initialize(this);
        this.chunkFinalizerSupplier = chunkFinalizerSupplier;
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
        Region3i region = ChunkMath.getChunkRegionAroundWorldPos(blockPos, extent);
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
            Chunk chunk = chunkCache.get(chunkPos);
            if (chunk == null) {
                return null;
            }
            chunkPos.sub(region.minX(), region.minY(), region.minZ());
            int index = TeraMath.calculate3DArrayIndex(chunkPos, region.size());
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset, blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Override
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
                region.checkIfChunkIsRelevant(chunk);
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
    public void completeUpdate() {
        ReadyChunkInfo readyChunkInfo = chunkFinalizer.completeFinalization();
        if (readyChunkInfo != null) {
            processReadyChunk(readyChunkInfo);
        }
    }

    private void processReadyChunk(final ReadyChunkInfo readyChunkInfo) {
        updateChunkReadinessState(readyChunkInfo);
        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        if (readyChunkInfo.isNewChunk()) {
            generateQueuedEntities(readyChunkInfo);
            sendOnActivatedBlocks(readyChunkInfo);
            sendOnChunkGenerated(readyChunkInfo);
        } else {
            readyChunkInfo.getChunkStore().restoreEntities();
            sendOnAddedBlocks(readyChunkInfo);
            sendOnActivatedBlocks(readyChunkInfo);
        }
        sendOnChunkLoaded(readyChunkInfo);
    }

    private OnChunkLoaded sendOnChunkLoaded(final ReadyChunkInfo readyChunkInfo) {
        return worldEntity.send(new OnChunkLoaded(readyChunkInfo.getPos()));
    }

    private void sendOnChunkGenerated(final ReadyChunkInfo readyChunkInfo) {
        worldEntity.send(new OnChunkGenerated(readyChunkInfo.getPos()));
    }

    private void sendOnActivatedBlocks(final ReadyChunkInfo readyChunkInfo) {
        PerformanceMonitor.startActivity("Sending OnActivateBlocks");
        readyChunkInfo.getBlockPositionMapppings().forEachEntry((id, positions) -> {
            if (positions.size() > 0) {
                blockManager.getBlock(id).getEntity().send(new OnActivatedBlocks(positions, registry));
            }
            return true;
        });
        PerformanceMonitor.endActivity();
    }

    private void sendOnAddedBlocks(final ReadyChunkInfo readyChunkInfo) {
        PerformanceMonitor.startActivity("Sending OnAddedBlocks");
        readyChunkInfo.getBlockPositionMapppings().forEachEntry((id, positions) -> {
            if (positions.size() > 0) {
                blockManager.getBlock(id).getEntity().send(new OnAddedBlocks(positions, registry));
            }
            return true;
        });
        PerformanceMonitor.endActivity();
    }

    private void generateQueuedEntities(final ReadyChunkInfo readyChunkInfo) {
        PerformanceMonitor.startActivity("Generating queued Entities");
        readyChunkInfo.getEntities().forEach(this::generateQueuedEntities);
        PerformanceMonitor.endActivity();
    }

    private void updateChunkReadinessState(final ReadyChunkInfo readyChunkInfo) {
        Chunk chunk = readyChunkInfo.getChunk();
        chunk.markReady();
    }

    private void generateQueuedEntities(EntityStore store) {
        Prefab prefab = store.getPrefab();
        EntityRef entity;
        if (prefab != null) {
            entity = entityManager.create(prefab);
        } else {
            entity = entityManager.create();
        }
        for (Component component : store.iterateComponents()) {
            entity.addComponent(component);
        }
    }

    @Override
    public void beginUpdate() {
        regionLock.readLock().lock();
        try {
            updateRelevance();
            deactivateBlocks();
            checkForUnload();
            makeChunksAvailable();
        } finally {
            regionLock.readLock().unlock();
        }
    }

    private void makeChunksAvailable() {
        List<ReadyChunkInfo> newReadyChunks = Lists.newArrayListWithExpectedSize(readyChunks.size());
        readyChunks.drainTo(newReadyChunks);
        for (ReadyChunkInfo readyChunkInfo : newReadyChunks) {
            chunkCache.put(readyChunkInfo.getPos(), readyChunkInfo.getChunk());
            preparingChunks.remove(readyChunkInfo.getPos());
        }
        updateRelevanceRegionsWithNewChunks(newReadyChunks);
        if (!newReadyChunks.isEmpty()) {
            sortedReadyChunks.addAll(newReadyChunks);
            Collections.sort(sortedReadyChunks, new ReadyChunkRelevanceComparator());
        }
        if (!sortedReadyChunks.isEmpty()) {
            boolean loaded = false;
            for (int i = sortedReadyChunks.size() - 1; i >= 0 && !loaded; i--) {
                ReadyChunkInfo chunkInfo = sortedReadyChunks.get(i);
                PerformanceMonitor.startActivity("Make Chunk Available");
                if (makeChunkAvailable(chunkInfo)) {
                    sortedReadyChunks.remove(i);
                    loaded = true;
                }
                PerformanceMonitor.endActivity();
            }
        }
    }

    private void updateRelevanceRegionsWithNewChunks(List<ReadyChunkInfo> newReadyChunks) {
        for (ReadyChunkInfo readyChunkInfo : newReadyChunks) {
            for (ChunkRelevanceRegion region : regions.values()) {
                region.checkIfChunkIsRelevant(readyChunkInfo.getChunk());
            }
        }
    }

    private void deactivateBlocks() {
        List<TShortObjectMap<TIntList>> deactivatedBlockSets = Lists.newArrayListWithExpectedSize(deactivateBlocksQueue.size());
        deactivateBlocksQueue.drainTo(deactivatedBlockSets);
        for (TShortObjectMap<TIntList> deactivatedBlockSet : deactivatedBlockSets) {
            deactivatedBlockSet.forEachEntry((id, positions) -> {
                if (positions.size() > 0) {
                    blockManager.getBlock(id).getEntity().send(new BeforeDeactivateBlocks(positions, registry));
                }
                return true;
            });
        }
    }

    private void checkForUnload() {
        PerformanceMonitor.startActivity("Unloading irrelevant chunks");
        int unloaded = 0;
        logger.debug("Compacting cache");
        Iterator<Vector3i> iterator = chunkCache.iterateChunkPositions();
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
                if (unloadChunkInternal(pos)) {
                    iterator.remove();
                    if (++unloaded >= UNLOAD_PER_FRAME) {
                        break;
                    }
                }
            }
        }
        PerformanceMonitor.endActivity();
    }

    private boolean unloadChunkInternal(Vector3i pos) {
        Chunk chunk = chunkCache.get(pos);
        if (!chunk.isReady()) {
            // Chunk hasn't been finished or changed, so just drop it.
            Iterator<ReadyChunkInfo> infoIterator = sortedReadyChunks.iterator();
            while (infoIterator.hasNext()) {
                ReadyChunkInfo next = infoIterator.next();
                if (next.getPos().equals(chunk.getPosition())) {
                    infoIterator.remove();
                    break;
                }
            }
            return true;
        }
        worldEntity.send(new BeforeChunkUnload(pos));
        for (ChunkRelevanceRegion region : regions.values()) {
            region.chunkUnloaded(pos);
        }
        storageManager.deactivateChunk(chunk);
        chunk.dispose();

        try {
            unloadRequestTaskMaster.put(new ChunkUnloadRequest(chunk, this));
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue unload request for {}", chunk.getPosition(), e);
        }

        return true;
    }

    private boolean areAdjacentChunksReady(Chunk chunk) {
        for (Chunk adjacentChunk : listAdjacentChunks(chunk)) {
            if (!adjacentChunk.isReady()) {
                return false;
            }
        }
        return true;
    }

    private List<Chunk> listAdjacentChunks(Chunk chunk) {
        final Vector3i centerChunkPosition = chunk.getPosition();
        List<Chunk> adjacentChunks = new ArrayList<>(6);
        for (Side side : Side.getAllSides()) {
            final Vector3i adjacentChunkPosition = side.getAdjacentPos(centerChunkPosition);
            final Chunk adjacentChunk = chunkCache.get(adjacentChunkPosition);
            if (adjacentChunk != null) {
                adjacentChunks.add(adjacentChunk);
            }
        }
        return adjacentChunks;
    }

    private void updateRelevance() {
        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            chunkRelevanceRegion.update();
            if (chunkRelevanceRegion.isDirty()) {
                for (Vector3i pos : chunkRelevanceRegion.getNeededChunks()) {
                    Chunk chunk = chunkCache.get(pos);
                    if (chunk != null) {
                        chunkRelevanceRegion.checkIfChunkIsRelevant(chunk);
                    } else {
                        createOrLoadChunk(pos);
                    }
                }
                chunkRelevanceRegion.setUpToDate();
            }
        }
    }

    private boolean makeChunkAvailable(final ReadyChunkInfo readyChunkInfo) {
        final Chunk chunk = chunkCache.get(readyChunkInfo.getPos());
        if (chunk == null) {
            return false;
        }
        for (Vector3i pos : Region3i.createFromCenterExtents(readyChunkInfo.getPos(), 1)) {
            if (chunkCache.get(pos) == null) {
                return false;
            }
        }
        chunkFinalizer.beginFinalization(chunk, readyChunkInfo);
        return true;
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
        blockManager.listRegisteredBlocks().stream().filter(Block::isLifecycleEventsRequired).forEach(block ->
                batchBlockMap.put(block.getId(), new TIntArrayList()));

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
        Chunk chunk = chunkCache.get(pos);
        if (isChunkReady(chunk)) {
            return chunk;
        }
        return null;
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return chunkCache.getAllChunks();
    }


    @Override
    public void restart() {
        pipeline.restart();
        unloadRequestTaskMaster.restart();
        chunkFinalizer.restart();
    }

    @Override
    public void shutdown() {
        pipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
        chunkFinalizer.shutdown();
    }

    @Override
    public void dispose() {
        shutdown();

        for (Chunk chunk : chunkCache.getAllChunks()) {
            unloadChunkInternal(chunk.getPosition());
            chunk.dispose();
        }
        chunkCache.clear();
        /*
         * The chunk monitor needs to clear chunk references, so it's important
         * that no new chunk get created
         */
        ChunkMonitor.fireChunkProviderDisposed(this);
    }

    @Override
    public boolean reloadChunk(Vector3i coords) {
        if (!chunkCache.containsChunkAt(coords)) {
            return false;
        }

        if (unloadChunkInternal(coords)) {
            chunkCache.removeChunkAt(coords);
            createOrLoadChunk(coords);
            return true;
        }

        return false;
    }

    @Override
    public void purgeWorld() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        pipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
        chunkFinalizer.shutdown();

        chunkCache.getAllChunks().stream().filter(ManagedChunk::isReady).forEach(chunk -> {
            worldEntity.send(new BeforeChunkUnload(chunk.getPosition()));
            storageManager.deactivateChunk(chunk);
            chunk.dispose();
        });
        chunkCache.clear();
        readyChunks.clear();
        sortedReadyChunks.clear();
        storageManager.deleteWorld();
        preparingChunks.clear();
        worldEntity.send(new PurgeWorldEvent());

        pipeline = new ChunkGenerationPipeline(new ChunkTaskRelevanceComparator());
        unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        chunkFinalizer = chunkFinalizerSupplier.get();
        chunkFinalizer.initialize(this);
        chunkFinalizer.restart();
        ChunkMonitor.fireChunkProviderInitialized(this);

        for (ChunkRelevanceRegion chunkRelevanceRegion : regions.values()) {
            for (Vector3i pos : chunkRelevanceRegion.getCurrentRegion()) {
                createOrLoadChunk(pos);
            }
            chunkRelevanceRegion.setUpToDate();
        }
    }

    private void createOrLoadChunk(Vector3i chunkPos) {
        Chunk chunk = chunkCache.get(chunkPos);
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
                    EntityBufferImpl buffer = new EntityBufferImpl();
                    if (chunkStore == null) {
                        chunk = new ChunkImpl(getPosition(), blockManager, biomeManager, extraDataManager);
                        generator.createChunk(chunk, buffer);
                    } else {
                        chunk = chunkStore.getChunk();
                    }

                    InternalLightProcessor.generateInternalLighting(chunk);
                    chunk.deflate();
                    TShortObjectMap<TIntList> mappings = createBatchBlockEventMappings(chunk);
                    readyChunks.offer(new ReadyChunkInfo(chunk, mappings, chunkStore, buffer.getAll()));
                }
            });
        }
    }


    @Override
    public void onChunkIsReady(Chunk chunk) {
        readyChunks.offer(new ReadyChunkInfo(chunk, createBatchBlockEventMappings(chunk), Collections.emptyList()));
    }

    @Override
    public Chunk getChunkUnready(Vector3i pos) {
        return chunkCache.get(pos);
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        return isChunkReady(chunkCache.get(pos));
    }

    private boolean isChunkReady(Chunk chunk) {
        return chunk != null && chunk.isReady();
    }

    private class ChunkTaskRelevanceComparator implements Comparator<ChunkTask> {

        @Override
        public int compare(ChunkTask o1, ChunkTask o2) {
            return score(o1) - score(o2);
        }

        private int score(ChunkTask task) {
            if (task.isTerminateSignal()) {
                return -1;
            }
            return score(task.getPosition());
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
