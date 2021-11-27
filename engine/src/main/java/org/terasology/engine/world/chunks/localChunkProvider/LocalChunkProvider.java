// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TShortObjectMap;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.EntityStore;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.monitoring.chunk.ChunkMonitor;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.utilities.concurrency.TaskMaster;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BeforeDeactivateBlocks;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.BlockRegionc;
import org.terasology.engine.world.block.OnActivatedBlocks;
import org.terasology.engine.world.block.OnAddedBlocks;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkBlockIterator;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkGenerated;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.engine.world.chunks.event.PurgeWorldEvent;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.chunks.internal.ChunkRelevanceRegion;
import org.terasology.engine.world.chunks.pipeline.ChunkProcessingPipeline;
import org.terasology.engine.world.chunks.pipeline.stages.ChunkTaskProvider;
import org.terasology.engine.world.generation.impl.EntityBufferImpl;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.ChunkViewCore;
import org.terasology.engine.world.internal.ChunkViewCoreImpl;
import org.terasology.engine.world.propagation.light.InternalLightProcessor;
import org.terasology.engine.world.propagation.light.LightMerger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Scheduler;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Provides chunks. Chunks placed in this JVM. Also generated Chunks if needed.
 * <p>
 * Loading/Unload chunks dependent on {@link RelevanceSystem}
 * <p/>
 * Produces events:
 * <p>
 * {@link OnChunkGenerated} when chunk was generated {@link WorldGenerator}
 * <p>
 * {@link OnChunkLoaded} when chunk was loaded from {@link StorageManager}
 * <p>
 * {@link OnActivatedBlocks} when load/generate chunk and chunk have blocks with lifecycle (?) {@see
 * https://github.com/MovingBlocks/Terasology/issues/3244}
 * <p>
 * {@link OnAddedBlocks} when load/generate chunk and chunk have blocks with lifecycle (?) {@see
 * https://github.com/MovingBlocks/Terasology/issues/3244}
 * <p>
 * {@link BeforeChunkUnload} when chunk ready to remove from provider.
 * <p>
 * {@link BeforeDeactivateBlocks} when chunk ready to remove and have block lifecycle.
 */
public class LocalChunkProvider implements ChunkProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalChunkProvider.class);
    private static final int UNLOAD_PER_FRAME = 64;
    private final EntityManager entityManager;
    private final BlockingQueue<Chunk> readyChunks = Queues.newLinkedBlockingQueue();
    private final BlockingQueue<TShortObjectMap<TIntList>> deactivateBlocksQueue = Queues.newLinkedBlockingQueue();
    private final Map<Vector3ic, Chunk> chunkCache;

    private final Map<Vector3ic, List<EntityStore>> generateQueuedEntities = new ConcurrentHashMap<>();

    private final StorageManager storageManager;
    private final WorldGenerator generator;
    private final BlockManager blockManager;
    private final ExtraBlockDataManager extraDataManager;
    private ChunkProcessingPipeline loadingPipeline;
    private TaskMaster<ChunkUnloadRequest> unloadRequestTaskMaster;
    private EntityRef worldEntity = EntityRef.NULL;
    private BlockEntityRegistry registry;

    private RelevanceSystem relevanceSystem;
    private final List<Vector3ic> chunksInRange = new ArrayList<>();
    private BlockRegion[] lastRegions;

    private volatile boolean shouldComplete = false;
    private final Set<Vector3ic> currentlyProcessing = new HashSet<>();

    public LocalChunkProvider(StorageManager storageManager, EntityManager entityManager, WorldGenerator generator,
                              BlockManager blockManager, ExtraBlockDataManager extraDataManager,
                              Map<Vector3ic, Chunk> chunkCache) {
        this.storageManager = storageManager;
        this.entityManager = entityManager;
        this.generator = generator;
        this.blockManager = blockManager;
        this.extraDataManager = extraDataManager;
        this.unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 4);
        this.chunkCache = chunkCache;
        ChunkMonitor.fireChunkProviderInitialized(this);
    }

    public void setBlockEntityRegistry(BlockEntityRegistry value) {
        this.registry = value;
    }

    @Override
    public ChunkViewCore getSubview(BlockRegionc region, Vector3ic offset) {
        Chunk[] chunks = new Chunk[region.volume()];
        for (Vector3ic chunkPos : region) {
            Chunk chunk = chunkCache.get(chunkPos);
            int index = (chunkPos.x() - region.minX()) + region.getSizeX()
                    * ((chunkPos.z() - region.minZ()) + region.getSizeZ()
                    * (chunkPos.y() - region.minY()));
            chunks[index] = chunk;
        }
        return new ChunkViewCoreImpl(chunks, region, offset, blockManager.getBlock(BlockManager.AIR_ID));
    }

    @Override
    public void setWorldEntity(EntityRef worldEntity) {
        this.worldEntity = worldEntity;
    }


    private void processReadyChunk(final Chunk chunk) {
        Vector3ic chunkPos = chunk.getPosition();
        if (chunkCache.get(chunkPos) != null) {
            return; // TODO move it in pipeline;
        }
        chunkCache.put(new Vector3i(chunkPos), chunk);
        chunk.markReady();
        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        ChunkStore store = this.storageManager.loadChunkStore(chunkPos);
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
            generateQueuedEntities.remove(chunkPos).forEach(this::generateQueuedEntities);
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


            worldEntity.send(new OnChunkGenerated(chunkPos));
        }
        worldEntity.send(new OnChunkLoaded(chunkPos));
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
    public void update() {
        deactivateBlocks();
        checkForUnload();
        Chunk chunk;
        while ((chunk = readyChunks.poll()) != null) {
            processReadyChunk(chunk);
        }
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
        Iterator<Vector3ic> iterator = Iterators.concat(
            Iterators.transform(chunkCache.keySet().iterator(), v -> new Vector3i(v.x(), v.y(), v.z())),
            loadingPipeline.getProcessingPositions().iterator());
        while (iterator.hasNext()) {
            Vector3ic pos = iterator.next();
            boolean keep = relevanceSystem.isChunkInRegions(pos); // TODO: move it to relevance system.
            if (!keep && unloadChunkInternal(pos)) {
                iterator.remove();
                if (++unloaded >= UNLOAD_PER_FRAME) {
                    break;
                }

            }
        }
        if (unloaded > 0) {
            logger.debug("Unload {} chunks", unloaded);
        }
        PerformanceMonitor.endActivity();
    }

    private boolean unloadChunkInternal(Vector3ic pos) {
        if (loadingPipeline.isPositionProcessing(pos)) {
            // Chunk hasn't been finished or changed, so just drop it.
            loadingPipeline.stopProcessingAt(pos);
            return false;
        }
        Chunk chunk = chunkCache.get(pos);
        if (chunk == null) {
            return false;
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
                positionList.add(i.getBlockPos().x());
                positionList.add(i.getBlockPos().y());
                positionList.add(i.getBlockPos().z());
            }
        }
        return batchBlockMap;
    }

    @Override
    public Chunk getChunk(int x, int y, int z) {
        return getChunk(new Vector3i(x, y, z));
    }

    @Override
    public Chunk getChunk(Vector3ic pos) {
        Chunk chunk = chunkCache.get(pos);
        if (isChunkReady(chunk)) {
            return chunk;
        }
        return null;
    }

    @Override
    public Collection<Chunk> getAllChunks() {
        return chunkCache.values();
    }


    @Override
    public void restart() {
        loadingPipeline.restart();
        unloadRequestTaskMaster.restart();
    }

    @Override
    public void shutdown() {
        loadingPipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
    }

    @Override
    public void dispose() {
        shutdown();

        for (Chunk chunk : getAllChunks()) {
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
    public boolean reloadChunk(Vector3ic coords) {
        if (!chunkCache.containsKey(coords)) {
            return false;
        }

        if (unloadChunkInternal(coords)) {
            chunkCache.remove(coords);
            return true;
        }

        return false;
    }

    @Override
    public void purgeWorld() {
        ChunkMonitor.fireChunkProviderDisposed(this);
        loadingPipeline.shutdown();
        unloadRequestTaskMaster.shutdown(new ChunkUnloadRequest(), true);
        getAllChunks().stream().filter(Chunk::isReady).forEach(chunk -> {
            worldEntity.send(new BeforeChunkUnload(chunk.getPosition()));
            storageManager.deactivateChunk(chunk);
            chunk.dispose();
        });
        chunkCache.clear();
        storageManager.deleteWorld();
        worldEntity.send(new PurgeWorldEvent());

        setRelevanceSystem(relevanceSystem);

        unloadRequestTaskMaster = TaskMaster.createFIFOTaskMaster("Chunk-Unloader", 8);
        ChunkMonitor.fireChunkProviderInitialized(this);

        notifyRelevanceChanged();
    }

    @Override
    public boolean isChunkReady(Vector3ic pos) {
        return isChunkReady(chunkCache.get(pos));
    }

    private boolean isChunkReady(Chunk chunk) {
        return chunk != null && chunk.isReady();
    }

    public void notifyRelevanceChanged() {
        loadingPipeline.notifyUpdate();
    }

    private void updateList() {
        chunksInRange.removeIf(x -> !relevanceSystem.isChunkInRegions(x) || isChunkReady(x));
        relevanceSystem.neededChunks()
                .filter(pos -> !chunksInRange.contains(pos))
                .forEach(chunksInRange::add);
        chunksInRange.sort(relevanceSystem.createChunkPosComparator().reversed());
    }

    private boolean checkForUpdate() {
        Collection<ChunkRelevanceRegion> regions = relevanceSystem.getRegions();
        if (lastRegions == null || regions.size() != lastRegions.length) {
            lastRegions = regions.stream().map(ChunkRelevanceRegion::getCurrentRegion).toArray(BlockRegion[]::new);
            return true;
        }
        int i = 0;
        boolean anyChanged = false;
        for (ChunkRelevanceRegion region : regions) {
            if (!lastRegions[i].equals(region.getCurrentRegion())) {
                lastRegions[i].set(region.getCurrentRegion());
                anyChanged = true;
            }
            i++;
        }
        return anyChanged;
    }

    /**
     * Loads a chunk if possible, otherwise generates it.
     *
     * @return The chunk at `pos`, ready for submitting to the ChunkProcessingPipeline.
     */
    private Chunk genChunk(Vector3ic pos) {
        ChunkStore chunkStore = storageManager.loadChunkStore(pos);
        Chunk chunk;
        if (chunkStore == null) {
            EntityBufferImpl buffer = new EntityBufferImpl();
            chunk = new ChunkImpl(pos, blockManager, extraDataManager);
            generator.createChunk(chunk, buffer);
            generateQueuedEntities.put(chunk.getPosition(), buffer.getAll());
        } else {
            chunk = chunkStore.getChunk();
        }
        return chunk;
    }

    /**
     * Computes the next `numChunks` chunks to generate.
     * This must be synchronized.
     */
    private synchronized List<Vector3ic> chunksToGenerate(int numChunks) {
        List<Vector3ic> chunks = new ArrayList<>(numChunks);

        if (checkForUpdate()) {
            updateList();
        }

        while (chunks.size() < numChunks && !chunksInRange.isEmpty()) {
            Vector3ic pos = chunksInRange.remove(chunksInRange.size() - 1);
            if (currentlyProcessing.contains(pos) || loadingPipeline.isPositionProcessing(pos)) {
                continue;
            }

            chunks.add(pos);
            currentlyProcessing.add(pos);
        }

        return chunks;
    }

    /**
     * This method runs once per chunk processing thread to set up the request callback.
     */
    private void onSubscribe(FluxSink<Chunk> sink) {
        sink.onRequest(numChunks -> {
            List<Vector3ic> positionsPending = chunksToGenerate((int) numChunks);

            // Generating the actual chunks can be done completely asynchronously
            for (Vector3ic pos : positionsPending) {
                currentlyProcessing.remove(pos);
                // The first time the onRequest lambda is called, when it submits its last chunk, this call to next() won't return
                // because Reactor puts the event loop logic inside the next() function and the pipeline keeps requesting more chunks.
                // So removing the position from currentlyProcessing and anything else that needs to happen must come before this call.
                sink.next(genChunk(pos));
            }
            if (shouldComplete && chunksInRange.isEmpty()) {
                sink.complete();
            }
        });
    }

    /**
     * Tells the ChunkProcessingPipeline that no more chunks are coming after what's currently queued.
     * Intended for use in tests.
     */
    protected void markComplete() {
        shouldComplete = true;
        loadingPipeline.notifyUpdate();
    }

    public void setRelevanceSystem(RelevanceSystem relevanceSystem) {
        setRelevanceSystem(relevanceSystem, null);
    }

    // TODO: move loadingPipeline initialization into constructor.
    public void setRelevanceSystem(RelevanceSystem relevanceSystem, Scheduler scheduler) {
        if (loadingPipeline != null) {
            loadingPipeline.shutdown();
        }
        this.relevanceSystem = relevanceSystem;
        if (scheduler != null) {
            loadingPipeline = new ChunkProcessingPipeline(this::getChunk, Flux.create(this::onSubscribe), scheduler);
        } else {
            loadingPipeline = new ChunkProcessingPipeline(this::getChunk, Flux.create(this::onSubscribe));
        }
        loadingPipeline.addStage(
                        ChunkTaskProvider.create("Chunk generate internal lightning",
                                (Consumer<Chunk>) InternalLightProcessor::generateInternalLighting))
                .addStage(ChunkTaskProvider.create("Chunk deflate", Chunk::deflate))
                .addStage(ChunkTaskProvider.createMulti("Light merging",
                        chunks -> {
                            Chunk[] localChunks = chunks.toArray(new Chunk[0]);
                            return LightMerger.merge(localChunks);
                        }, LightMerger::requiredChunks
                ))
                .addStage(ChunkTaskProvider.create("Chunk ready", readyChunks::add));
    }
}
