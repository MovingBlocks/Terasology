// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.world.chunks.localChunkProvider;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
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
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.monitoring.chunk.ChunkMonitor;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.StorageManager;
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
import org.terasology.world.chunks.pipeline.ChunkProcessingPipeline;
import org.terasology.world.chunks.pipeline.SupplierChunkTask;
import org.terasology.world.chunks.pipeline.tasks.DeflateChunkTask;
import org.terasology.world.chunks.pipeline.tasks.GenerateInternalLightningChunkTask;
import org.terasology.world.chunks.pipeline.tasks.LightMergerChunkTask;
import org.terasology.world.chunks.pipeline.tasks.NotifyChunkTask;
import org.terasology.world.generation.impl.EntityBufferImpl;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.internal.ChunkViewCore;
import org.terasology.world.internal.ChunkViewCoreImpl;
import org.terasology.world.propagation.light.LightMerger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Supplier;

/**
 *
 */
public class LocalChunkProvider implements GeneratingChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);
    private static final int UNLOAD_PER_FRAME = 64;
    private final EntityManager entityManager;
    private final BlockingQueue<TShortObjectMap<TIntList>> deactivateBlocksQueue = Queues.newLinkedBlockingQueue();
    private final ChunkCache chunkCache;

    private final Map<org.joml.Vector3i, List<EntityStore>> generateQueuedEntities = new HashMap<>();

    private LightMerger<Object> lightMerger;
    private StorageManager storageManager;
    private ChunkProcessingPipeline loadingPipeline;
    private TaskMaster<ChunkUnloadRequest> unloadRequestTaskMaster;
    private WorldGenerator generator;
    private List<ReadyChunkInfo> sortedReadyChunks = Lists.newArrayList();
    private EntityRef worldEntity = EntityRef.NULL;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private BlockEntityRegistry registry;

    private RelevanceSystem relevanceSystem;

    //TODO Remove this old constructor at the end of the chunk overhaul
    public LocalChunkProvider(StorageManager storageManager, EntityManager entityManager, WorldGenerator generator,
                              BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        this(storageManager,
                entityManager,
                generator,
                blockManager,
                extraDataManager,
                new LightMergingChunkFinalizer(),
                LightMergingChunkFinalizer::new,
                new ConcurrentMapChunkCache());
    }

    LocalChunkProvider(StorageManager storageManager, EntityManager entityManager, WorldGenerator generator,
                       BlockManager blockManager, ExtraBlockDataManager extraDataManager,
                       ChunkFinalizer chunkFinalizer, Supplier<ChunkFinalizer> chunkFinalizerSupplier,
                       ChunkCache chunkCache) {
        this.storageManager = storageManager;
        this.entityManager = entityManager;
        this.generator = generator;
        this.blockManager = blockManager;
        this.extraDataManager = extraDataManager;
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 4);
        this.chunkCache = chunkCache;
        this.lightMerger = new LightMerger<Object>(this);
        ChunkMonitor.fireChunkProviderInitialized(this);
    }


    protected void createOrLoadChunk(Vector3i chunkPos) {
        if (getChunkUnready(chunkPos) == null) {
            loadingPipeline.invokeGeneratorTask(new SupplierChunkTask("Create or Load Chunk", () -> {
                ChunkStore chunkStore = storageManager.loadChunkStore(chunkPos);
                Chunk chunk;
                EntityBufferImpl buffer = new EntityBufferImpl();
                if (chunkStore == null) {
                    chunk = new ChunkImpl(chunkPos, blockManager, extraDataManager);
                    generator.createChunk(chunk, buffer);
                    generateQueuedEntities.put(chunk.getPosition(new org.joml.Vector3i()), buffer.getAll());
                } else {
                    chunk = chunkStore.getChunk();
                }
                return chunk;
            }));
        }
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
    public void completeUpdate() {
        //List<ReadyChunkInfo> readyChunkInfos = chunkFinalizer.completeFinalization();
        //readyChunkInfos.forEach(this::processReadyChunk);
    }

    private void processReadyChunk(final Chunk chunk) {
        chunkCache.put(chunk.getPosition(), chunk);
        chunk.markReady();
        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        ChunkStore store = this.storageManager.loadChunkStore(chunk.getPosition());
        TShortObjectMap<TIntList> mappings = createBatchBlockEventMappings(chunk);
        if (store != null) {
            store.restoreEntities();

            PerformanceMonitor.startActivity("Sending OnAddedBlocks");
            mappings.forEachEntry((id, positions) -> {
                if (positions.size() > 0) {
                    blockManager.getBlock(id).getEntity().send(new OnAddedBlocks(positions, registry));
                }
                return true;
            });
            PerformanceMonitor.endActivity();


            // send on activate
            PerformanceMonitor.startActivity("Sending OnActivateBlocks");

            mappings.forEachEntry((id, positions) -> {
                if (positions.size() > 0) {
                    blockManager.getBlock(id).getEntity().send(new OnActivatedBlocks(positions, registry));
                }
                return true;
            });
            PerformanceMonitor.endActivity();
        } else {
            PerformanceMonitor.startActivity("Generating queued Entities");
            generateQueuedEntities.remove(chunk.getPosition(new org.joml.Vector3i())).forEach(this::generateQueuedEntities);
            PerformanceMonitor.endActivity();

            // send on activate
            PerformanceMonitor.startActivity("Sending OnActivateBlocks");

            mappings.forEachEntry((id, positions) -> {
                if (positions.size() > 0) {
                    blockManager.getBlock(id).getEntity().send(new OnActivatedBlocks(positions, registry));
                }
                return true;
            });
            PerformanceMonitor.endActivity();


            worldEntity.send(new OnChunkGenerated(chunk.getPosition()));
        }
        new OnChunkLoaded(chunk.getPosition());
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
        deactivateBlocks();
        checkForUnload();
//        makeChunksAvailable();
    }

    private void deactivateBlocks() {
        List<TShortObjectMap<TIntList>> deactivatedBlockSets =
                Lists.newArrayListWithExpectedSize(deactivateBlocksQueue.size());
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
            boolean keep = relevanceSystem.isChunkInRegions(pos);
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
        storageManager.deactivateChunk(chunk);
        chunk.dispose();

        try {
            unloadRequestTaskMaster.put(new ChunkUnloadRequest(chunk, this));
        } catch (InterruptedException e) {
            logger.error("Failed to enqueue unload request for {}", chunk.getPosition(), e);
        }

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
        loadingPipeline.restart();
        unloadRequestTaskMaster.restart();
        lightMerger.restart();
    }

    @Override
    public void shutdown() {
        loadingPipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
        lightMerger.shutdown();
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
        loadingPipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
        lightMerger.shutdown();
        lightMerger = new LightMerger<>(this);
        chunkCache.getAllChunks().stream().filter(ManagedChunk::isReady).forEach(chunk -> {
            worldEntity.send(new BeforeChunkUnload(chunk.getPosition()));
            storageManager.deactivateChunk(chunk);
            chunk.dispose();
        });
        chunkCache.clear();
        sortedReadyChunks.clear();
        storageManager.deleteWorld();
        worldEntity.send(new PurgeWorldEvent());

        loadingPipeline = new ChunkProcessingPipeline(relevanceSystem.createChunkTaskComporator());
        unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        ChunkMonitor.fireChunkProviderInitialized(this);

        for (ChunkRelevanceRegion chunkRelevanceRegion : relevanceSystem.getRegions()) {
            for (Vector3i pos : chunkRelevanceRegion.getCurrentRegion()) {
                createOrLoadChunk(pos);
            }
            chunkRelevanceRegion.setUpToDate();
        }
    }

    @Override
    public void onChunkIsReady(Chunk chunk) {
        //readyChunks.offer(new ReadyChunkInfo(chunk, createBatchBlockEventMappings(chunk), Collections.emptyList()));
    }

    @Override
    public Chunk getChunkUnready(Vector3i pos) {
        Chunk chunk = chunkCache.get(pos);
        if (chunk == null) {
            chunk = loadingPipeline.getProcessingChunks().get(pos);
        }
        return chunk;
    }

    @Override
    public boolean isChunkReady(Vector3i pos) {
        return isChunkReady(chunkCache.get(pos));
    }

    private boolean isChunkReady(Chunk chunk) {
        return chunk != null && chunk.isReady();
    }

    public void setRelevanceSystem(RelevanceSystem relevanceSystem) {
        this.relevanceSystem = relevanceSystem;
        loadingPipeline = new ChunkProcessingPipeline(relevanceSystem.createChunkTaskComporator());
        loadingPipeline.addStage(GenerateInternalLightningChunkTask::new)
                .addStage(DeflateChunkTask::new)
                .addStage(LightMergerChunkTask.stage(this, lightMerger))
                .addStage(NotifyChunkTask.stage("Notify listeners", this::processReadyChunk));
    }
}
