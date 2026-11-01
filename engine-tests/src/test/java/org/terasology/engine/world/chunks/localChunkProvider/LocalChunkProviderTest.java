// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.RenderingConfig;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BeforeDeactivateBlocks;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.block.OnActivatedBlocks;
import org.terasology.engine.world.block.OnAddedBlocks;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkGenerated;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.fixtures.TestBlockManager;
import org.terasology.fixtures.TestChunkStore;
import org.terasology.fixtures.TestStorageManager;
import org.terasology.fixtures.TestWorldGenerator;
import org.terasology.gestalt.entitysystem.event.Event;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalChunkProviderTest {

    private static final int WAIT_CHUNK_IS_READY_IN_SECONDS = 30;

    private LocalChunkProvider chunkProvider;
    private EntityManager entityManager;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private BlockEntityRegistry blockEntityRegistry;
    private EntityRef worldEntity;
    private Config config;
    private Map<Vector3ic, Chunk> chunkCache;
    private Block blockAtBlockManager;
    private TestStorageManager storageManager;
    private TestWorldGenerator generator;

    @BeforeEach
    public void setUp() {
        entityManager = mock(EntityManager.class);
        blockAtBlockManager = new Block();
        blockAtBlockManager.setId((short) 1);
        blockAtBlockManager.setUri(BlockManager.AIR_ID);
        blockAtBlockManager.setEntity(mock(EntityRef.class));
        blockManager = new TestBlockManager(blockAtBlockManager);
        extraDataManager = new ExtraBlockDataManager();
        blockEntityRegistry = mock(BlockEntityRegistry.class);
        worldEntity = mock(EntityRef.class);
        chunkCache = Maps.newConcurrentMap();
        config = mock(Config.class);
        RenderingConfig renderConfig = mock(RenderingConfig.class);
        when(renderConfig.getChunkThreads()).thenReturn(0);
        when(config.getRendering()).thenReturn(renderConfig);
        storageManager = new TestStorageManager();
        generator = new TestWorldGenerator(blockManager);
        chunkProvider = new LocalChunkProvider(storageManager,
                entityManager,
                generator,
                blockManager,
                extraDataManager,
                config,
                chunkCache);
        chunkProvider.setBlockEntityRegistry(blockEntityRegistry);
        chunkProvider.setWorldEntity(worldEntity);
        chunkProvider.setRelevanceSystem(new RelevanceSystem(chunkProvider)); // workaround. initialize loading pipeline
    }

    @AfterEach
    public void tearDown() {
        chunkProvider.shutdown();
    }

    private Future<Chunk> requestCreatingOrLoadingArea(Vector3ic chunkPosition, int radius) {
        Future<Chunk> chunkFuture = chunkProvider.createOrLoadChunk(chunkPosition);
        BlockRegion extentsRegion = new BlockRegion(
                chunkPosition.x() - radius, chunkPosition.y() - radius, chunkPosition.z() - radius,
                chunkPosition.x() + radius, chunkPosition.y() + radius, chunkPosition.z() + radius);

        extentsRegion.iterator().forEachRemaining(pos -> {
            if (!pos.equals(chunkPosition)) { // remove center. we takes future for it already.
                chunkProvider.createOrLoadChunk(pos);
            }
        });
        return chunkFuture;
    }

    private Future<Chunk> requestCreatingOrLoadingArea(Vector3ic chunkPosition) {
        return requestCreatingOrLoadingArea(chunkPosition, 1);
    }

    /**
     * A single {@link LocalChunkProvider#update()} call only processes as many ready chunks as fit in
     * its per-tick time budget. {@link #requestCreatingOrLoadingArea} requests a full 3x3x3 neighbourhood
     * (needed so the centre chunk's own light merging can complete - see LightMerger.requiredChunks),
     * so more than just the centre chunk can end up ready at once; this loops update() until the one
     * this test actually cares about has been processed, rather than assuming one call is enough.
     */
    private void updateUntilChunkReady(Vector3ic chunkPosition) throws TimeoutException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(WAIT_CHUNK_IS_READY_IN_SECONDS);
        while (!chunkProvider.isChunkReady(chunkPosition)) {
            if (System.currentTimeMillis() > deadline) {
                throw new TimeoutException("Chunk " + chunkPosition + " was not ready within "
                        + WAIT_CHUNK_IS_READY_IN_SECONDS + " seconds");
            }
            chunkProvider.update();
        }
    }

    @Test
    void testGenerateSingleChunk() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        updateUntilChunkReady(chunkPosition);

        // requestCreatingOrLoadingArea also requests the surrounding neighbourhood (needed for the
        // centre chunk's own light merging), so other chunks' OnChunkGenerated/OnChunkLoaded events
        // can legitimately be interleaved with the centre's - filter by position rather than assuming
        // a fixed index.
        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(eventArgumentCaptor.capture());
        Assertions.assertAll("WorldEvents not valid",
                () -> {
                    Optional<OnChunkGenerated> onChunkGenerated = eventArgumentCaptor.getAllValues().stream()
                            .filter(OnChunkGenerated.class::isInstance)
                            .map(OnChunkGenerated.class::cast)
                            .filter(e -> e.getChunkPos().equals(chunkPosition))
                            .findFirst();
                    Assertions.assertTrue(onChunkGenerated.isPresent(),
                            "Must have an OnChunkGenerated event for the requested chunk");
                },
                () -> {
                    Optional<OnChunkLoaded> onChunkLoaded = eventArgumentCaptor.getAllValues().stream()
                            .filter(OnChunkLoaded.class::isInstance)
                            .map(OnChunkLoaded.class::cast)
                            .filter(e -> e.getChunkPos().equals(chunkPosition))
                            .findFirst();
                    Assertions.assertTrue(onChunkLoaded.isPresent(),
                            "Must have an OnChunkLoaded event for the requested chunk");
                });
    }

    @Test
    void testGenerateSingleChunkWithBlockLifeCycle() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));
        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        updateUntilChunkReady(chunkPosition);

        // requestCreatingOrLoadingArea also requests the surrounding neighbourhood (needed for the
        // centre chunk's own light merging), so other chunks' OnChunkGenerated/OnChunkLoaded events
        // can legitimately be interleaved with the centre's - filter by position rather than assuming
        // a fixed index.
        final ArgumentCaptor<Event> worldEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(worldEventCaptor.capture());
        Assertions.assertAll("World Events not valid",
                () -> {
                    Optional<OnChunkGenerated> onChunkGenerated = worldEventCaptor.getAllValues().stream()
                            .filter(OnChunkGenerated.class::isInstance)
                            .map(OnChunkGenerated.class::cast)
                            .filter(e -> e.getChunkPos().equals(chunkPosition))
                            .findFirst();
                    Assertions.assertTrue(onChunkGenerated.isPresent(),
                            "Must have an OnChunkGenerated event for the requested chunk");
                },
                () -> {
                    Optional<OnChunkLoaded> onChunkLoaded = worldEventCaptor.getAllValues().stream()
                            .filter(OnChunkLoaded.class::isInstance)
                            .map(OnChunkLoaded.class::cast)
                            .filter(e -> e.getChunkPos().equals(chunkPosition))
                            .findFirst();
                    Assertions.assertTrue(onChunkLoaded.isPresent(),
                            "Must have an OnChunkLoaded event for the requested chunk");
                });

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        // Block lifecycle events carry only in-chunk positions/counts, not which chunk they came from,
        // so with multiple chunks now generating blocks there's no way to attribute a specific event to
        // this chunk - check that activation happened at all rather than assuming it's the first event.
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(1)).send(blockEventCaptor.capture());

        Optional<OnActivatedBlocks> onActivatedBlocks = blockEventCaptor.getAllValues().stream()
                .filter(OnActivatedBlocks.class::isInstance)
                .map(OnActivatedBlocks.class::cast)
                .filter(e -> e.blockCount() > 0)
                .findFirst();
        Assertions.assertTrue(onActivatedBlocks.isPresent(),
                "Must have an OnActivatedBlocks event with a non-zero block count");
    }

    @Test
    void testLoadSingleChunk() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        Chunk chunk = new ChunkImpl(chunkPosition, blockManager, extraDataManager);
        generator.createChunk(chunk, null);
        storageManager.add(chunk);

        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        updateUntilChunkReady(chunkPosition);

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");

        // requestCreatingOrLoadingArea also requests the surrounding neighbourhood (needed for the
        // centre chunk's own light merging); those neighbours are freshly generated rather than
        // loaded (only the centre position was added to storageManager), so filter by both event
        // type and chunk position rather than assuming index 0 is necessarily this chunk's OnChunkLoaded.
        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Optional<OnChunkLoaded> onChunkLoaded = eventArgumentCaptor.getAllValues().stream()
                .filter(OnChunkLoaded.class::isInstance)
                .map(OnChunkLoaded.class::cast)
                .filter(e -> e.getChunkPos().equals(chunkPosition))
                .findFirst();
        Assertions.assertTrue(onChunkLoaded.isPresent(),
                "Must have an OnChunkLoaded event for the requested chunk");
    }

    @Test
    void testLoadSingleChunkWithBlockLifecycle() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        Chunk chunk = new ChunkImpl(chunkPosition, blockManager, extraDataManager);
        generator.createChunk(chunk, null);
        storageManager.add(chunk);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));

        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        updateUntilChunkReady(chunkPosition);

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");

        // requestCreatingOrLoadingArea also requests the surrounding neighbourhood (needed for the
        // centre chunk's own light merging); those neighbours are freshly generated rather than
        // loaded (only the centre position was added to storageManager), so filter by both event
        // type and chunk position rather than assuming index 0 is necessarily this chunk's OnChunkLoaded.
        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Optional<OnChunkLoaded> onChunkLoaded = eventArgumentCaptor.getAllValues().stream()
                .filter(OnChunkLoaded.class::isInstance)
                .map(OnChunkLoaded.class::cast)
                .filter(e -> e.getChunkPos().equals(chunkPosition))
                .findFirst();
        Assertions.assertTrue(onChunkLoaded.isPresent(),
                "Must have an OnChunkLoaded event for the requested chunk");

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        // Block lifecycle events carry only in-chunk positions/counts, not which chunk they came from,
        // so with multiple chunks now loading/generating blocks there's no way to attribute a specific
        // event to this chunk - check that both kinds happened at all rather than assuming fixed indices.
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(2)).send(blockEventCaptor.capture());
        Assertions.assertAll("Block events not valid",
                () -> {
                    Optional<OnAddedBlocks> onAddedBlocks = blockEventCaptor.getAllValues().stream()
                            .filter(OnAddedBlocks.class::isInstance)
                            .map(OnAddedBlocks.class::cast)
                            .filter(e -> e.blockCount() > 0)
                            .findFirst();
                    Assertions.assertTrue(onAddedBlocks.isPresent(),
                            "Must have an OnAddedBlocks event with a non-zero block count");
                },
                () -> {
                    Optional<OnActivatedBlocks> onActivatedBlocks = blockEventCaptor.getAllValues().stream()
                            .filter(OnActivatedBlocks.class::isInstance)
                            .map(OnActivatedBlocks.class::cast)
                            .filter(e -> e.blockCount() > 0)
                            .findFirst();
                    Assertions.assertTrue(onActivatedBlocks.isPresent(),
                            "Must have an OnActivatedBlocks event with a non-zero block count");
                });
    }

    @Test
    void testUnloadChunkAndDeactivationBlock() throws InterruptedException, TimeoutException, ExecutionException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));

        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);

        // requestCreatingOrLoadingArea also requests the surrounding neighbourhood (needed for the
        // centre chunk's own light merging), and with an empty RelevanceSystem every loaded chunk gets
        // unloaded - so wait for the REQUESTED chunk's own BeforeChunkUnload specifically (it carries a
        // position, so it's filterable), not just the first unload from any chunk in the batch.
        Assertions.assertTimeoutPreemptively(Duration.of(WAIT_CHUNK_IS_READY_IN_SECONDS, ChronoUnit.SECONDS),
                () -> {
                    ArgumentCaptor<Event> worldEventCaptor = ArgumentCaptor.forClass(Event.class);
                    while (worldEventCaptor.getAllValues()
                            .stream()
                            .filter(BeforeChunkUnload.class::isInstance)
                            .map(BeforeChunkUnload.class::cast)
                            .noneMatch(e -> e.getChunkPos().equals(chunkPosition))) {
                        chunkProvider.update();
                        worldEventCaptor = ArgumentCaptor.forClass(Event.class);
                        verify(worldEntity, atLeast(1)).send(worldEventCaptor.capture());
                    }
                }
        );

        // BeforeDeactivateBlocks carries no chunk position (see the TODO/issue #3244 below), so once
        // the requested chunk's own unload has started, keep updating until deactivation for *some*
        // chunk with lifecycle blocks has happened - the best that's attributable given the event's shape.
        Assertions.assertTimeoutPreemptively(Duration.of(WAIT_CHUNK_IS_READY_IN_SECONDS, ChronoUnit.SECONDS),
                () -> {
                    ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
                    while (blockEventCaptor.getAllValues()
                            .stream()
                            .noneMatch(BeforeDeactivateBlocks.class::isInstance)) {
                        chunkProvider.update();
                        blockEventCaptor = ArgumentCaptor.forClass(Event.class);
                        verify(blockAtBlockManager.getEntity(), atLeast(1)).send(blockEventCaptor.capture());
                    }
                }
        );

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Optional<BeforeChunkUnload> beforeChunkUnload = eventArgumentCaptor.getAllValues()
                .stream()
                .filter((e) -> e instanceof BeforeChunkUnload)
                .map((e) -> (BeforeChunkUnload) e)
                .filter(e -> e.getChunkPos().equals(chunkPosition))
                .findFirst();

        Assertions.assertTrue(beforeChunkUnload.isPresent(),
                "World events must have a BeforeChunkUnload event for the requested chunk");

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(2)).send(blockEventCaptor.capture());
        Optional<BeforeDeactivateBlocks> beforeDeactivateBlocks = blockEventCaptor.getAllValues()
                .stream()
                .filter((e) -> e instanceof BeforeDeactivateBlocks)
                .map((e) -> (BeforeDeactivateBlocks) e)
                .findFirst();
        Assertions.assertTrue(beforeDeactivateBlocks.isPresent(),
                "World events must have BeforeDeactivateBlocks event when chunk with lifecycle blocks was unload");
        Assertions.assertTrue(beforeDeactivateBlocks.get().blockCount() > 0,
                "BeforeDeactivateBlocks must have block count more then zero");
    }
}
