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

    @Test
    void testGenerateSingleChunk() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        chunkProvider.update();

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(eventArgumentCaptor.capture());
        Assertions.assertAll("WorldEvents not valid",
                () -> {
                    Event mustBeOnGeneratedEvent = eventArgumentCaptor.getAllValues().get(0);
                    Assertions.assertTrue(mustBeOnGeneratedEvent instanceof OnChunkGenerated,
                            "First world event must be OnChunkGenerated");
                    Assertions.assertEquals(((OnChunkGenerated) mustBeOnGeneratedEvent).getChunkPos(),
                            chunkPosition,
                            "Chunk position at event not expected");
                },
                () -> {
                    Event mustBeOnLoadedEvent = eventArgumentCaptor.getAllValues().get(1);
                    Assertions.assertTrue(mustBeOnLoadedEvent instanceof OnChunkLoaded,
                            "Second world event must be OnChunkLoaded");
                    Assertions.assertEquals(chunkPosition,
                            ((OnChunkLoaded) mustBeOnLoadedEvent).getChunkPos(),
                            "Chunk position at event not expected");
                });
    }

    @Test
    void testGenerateSingleChunkWithBlockLifeCycle() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));
        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        chunkProvider.update();

        final ArgumentCaptor<Event> worldEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(worldEventCaptor.capture());
        Assertions.assertAll("World Events not valid",
                () -> {
                    Event mustBeOnGeneratedEvent = worldEventCaptor.getAllValues().get(0);
                    Assertions.assertTrue(mustBeOnGeneratedEvent instanceof OnChunkGenerated,
                            "First world event must be OnChunkGenerated");
                    Assertions.assertEquals(((OnChunkGenerated) mustBeOnGeneratedEvent).getChunkPos(),
                            chunkPosition,
                            "Chunk position at event not expected");
                },
                () -> {
                    Event mustBeOnLoadedEvent = worldEventCaptor.getAllValues().get(1);
                    Assertions.assertTrue(mustBeOnLoadedEvent instanceof OnChunkLoaded,
                            "Second world event must be OnChunkLoaded");
                    Assertions.assertEquals(chunkPosition,
                            ((OnChunkLoaded) mustBeOnLoadedEvent).getChunkPos(),
                            "Chunk position at event not expected");
                });

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(1)).send(blockEventCaptor.capture());

        Event mustBeOnActivatedBlocks = blockEventCaptor.getAllValues().get(0);
        Assertions.assertTrue(mustBeOnActivatedBlocks instanceof OnActivatedBlocks,
                "First block event must be OnActivatedBlocks");
        Assertions.assertTrue(((OnActivatedBlocks) mustBeOnActivatedBlocks).blockCount() > 0,
                "Block count on activate must be non zero");
    }

    @Test
    void testLoadSingleChunk() throws InterruptedException, ExecutionException, TimeoutException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        Chunk chunk = new ChunkImpl(chunkPosition, blockManager, extraDataManager);
        generator.createChunk(chunk, null);
        storageManager.add(chunk);

        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);
        chunkProvider.update();

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Event mustBeOnLoadedEvent = eventArgumentCaptor.getAllValues().get(0);
        Assertions.assertTrue(mustBeOnLoadedEvent instanceof OnChunkLoaded,
                "Second world event must be OnChunkLoaded");
        Assertions.assertEquals(chunkPosition,
                ((OnChunkLoaded) mustBeOnLoadedEvent).getChunkPos(),
                "Chunk position at event not expected");
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
        chunkProvider.update();

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");


        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Event mustBeOnLoadedEvent = eventArgumentCaptor.getAllValues().get(0);
        Assertions.assertTrue(mustBeOnLoadedEvent instanceof OnChunkLoaded,
                "Second world event must be OnChunkLoaded");
        Assertions.assertEquals(chunkPosition,
                ((OnChunkLoaded) mustBeOnLoadedEvent).getChunkPos(),
                "Chunk position at event not expected");

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(2)).send(blockEventCaptor.capture());
        Assertions.assertAll("Block events not valid",
                () -> {
                    Event mustBeOnAddedBlocks = blockEventCaptor.getAllValues().get(0);
                    Assertions.assertTrue(mustBeOnAddedBlocks instanceof OnAddedBlocks,
                            "First block event must be OnAddedBlocks");
                    Assertions.assertTrue(((OnAddedBlocks) mustBeOnAddedBlocks).blockCount() > 0,
                            "Block count on activate must be non zero");
                },
                () -> {
                    Event mustBeOnActivatedBlocks = blockEventCaptor.getAllValues().get(1);
                    Assertions.assertTrue(mustBeOnActivatedBlocks instanceof OnActivatedBlocks,
                            "First block event must be OnActivatedBlocks");
                    Assertions.assertTrue(((OnActivatedBlocks) mustBeOnActivatedBlocks).blockCount() > 0,
                            "Block count on activate must be non zero");
                });
    }

    @Test
    void testUnloadChunkAndDeactivationBlock() throws InterruptedException, TimeoutException, ExecutionException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));

        requestCreatingOrLoadingArea(chunkPosition).get(WAIT_CHUNK_IS_READY_IN_SECONDS, TimeUnit.SECONDS);

        //Wait BeforeDeactivateBlocks event
        Assertions.assertTimeoutPreemptively(Duration.of(WAIT_CHUNK_IS_READY_IN_SECONDS, ChronoUnit.SECONDS),
                () -> {
                    ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
                    while (!blockEventCaptor.getAllValues()
                            .stream()
                            .filter((e) -> e instanceof BeforeDeactivateBlocks)
                            .map((e) -> (BeforeDeactivateBlocks) e)
                            .findFirst().isPresent()) {
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
                .findFirst();

        Assertions.assertTrue(beforeChunkUnload.isPresent(),
                "World events must have BeforeChunkUnload event when chunk was unload");
        Assertions.assertEquals(chunkPosition,
                beforeChunkUnload.get().getChunkPos(),
                "Chunk position at event not expected");

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
