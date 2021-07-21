// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.localChunkProvider;

import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BeforeDeactivateBlocks;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
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

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

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
    private Map<Vector3ic, Chunk> chunkCache;
    private Block blockAtBlockManager;
    private TestStorageManager storageManager;
    private TestWorldGenerator generator;
    private RelevanceSystem relevanceSystem;
    private EntityRef playerEntity;

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
        storageManager = new TestStorageManager();
        generator = new TestWorldGenerator(blockManager);
        chunkProvider = new LocalChunkProvider(storageManager,
                entityManager,
                generator,
                blockManager,
                extraDataManager,
                chunkCache);
        chunkProvider.setBlockEntityRegistry(blockEntityRegistry);
        chunkProvider.setWorldEntity(worldEntity);
        relevanceSystem = new RelevanceSystem(chunkProvider);
        chunkProvider.setRelevanceSystem(relevanceSystem); // workaround. initialize loading pipeline
    }

    @AfterEach
    public void tearDown() {
        chunkProvider.shutdown();
    }

    private void requestCreatingOrLoadingArea(Vector3ic chunkPosition, int radius) {
        playerEntity = mock(EntityRef.class);
        when(playerEntity.exists()).thenReturn(true);
        when(playerEntity.getComponent(LocationComponent.class)).thenReturn(new LocationComponent(new Vector3f(chunkPosition)));
        Vector3i distance = new Vector3i(radius, radius, radius);
        relevanceSystem.addRelevanceEntity(playerEntity, distance, null);
        chunkProvider.notifyRelevanceChanged();
    }

    private void requestCreatingOrLoadingArea(Vector3ic chunkPosition) {
        requestCreatingOrLoadingArea(chunkPosition, 2);
    }

    private void waitForChunks() throws InterruptedException {
        chunkProvider.testMarkCompleteAndWait(WAIT_CHUNK_IS_READY_IN_SECONDS * 1000);
    }

    @Test
    void testGenerateSingleChunk() throws InterruptedException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        requestCreatingOrLoadingArea(chunkPosition);
        waitForChunks();
        chunkProvider.update();

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(eventArgumentCaptor.capture());
        Assertions.assertAll("World Events not valid",
                () -> {
                    Assertions.assertTrue(eventArgumentCaptor.getAllValues().stream().anyMatch(x ->
                        x instanceof OnChunkGenerated && ((OnChunkGenerated) x).getChunkPos().equals(chunkPosition)
                    ), "Must be OnChunkGenerated event for chunk");
                },
                () -> {
                    Assertions.assertTrue(eventArgumentCaptor.getAllValues().stream().anyMatch(x ->
                            x instanceof OnChunkLoaded && ((OnChunkLoaded) x).getChunkPos().equals(chunkPosition)
                    ), "Must be OnChunkLoaded event for chunk");
                });
    }

    @Test
    void testGenerateSingleChunkWithBlockLifeCycle() throws InterruptedException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));
        requestCreatingOrLoadingArea(chunkPosition);
        waitForChunks();
        chunkProvider.update();

        final ArgumentCaptor<Event> worldEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(2)).send(worldEventCaptor.capture());
        Assertions.assertAll("World Events not valid",
                () -> {
                    Assertions.assertTrue(worldEventCaptor.getAllValues().stream().anyMatch(x ->
                        x instanceof OnChunkGenerated && ((OnChunkGenerated) x).getChunkPos().equals(chunkPosition)
                    ), "Must be OnChunkGenerated event for chunk");
                },
                () -> {
                    Assertions.assertTrue(worldEventCaptor.getAllValues().stream().anyMatch(x ->
                            x instanceof OnChunkLoaded && ((OnChunkLoaded) x).getChunkPos().equals(chunkPosition)
                    ), "Must be OnChunkLoaded event for chunk");
                });

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(1)).send(blockEventCaptor.capture());

        Assertions.assertTrue(blockEventCaptor.getAllValues().stream().anyMatch(x ->
                x instanceof OnActivatedBlocks && ((OnActivatedBlocks) x).blockCount() > 0
        ), "Must be OnActivatedBlocks event for chunk");
    }

    @Test
    void testLoadSingleChunk() throws InterruptedException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        Chunk chunk = new ChunkImpl(chunkPosition, blockManager, extraDataManager);
        generator.createChunk(chunk, null);
        storageManager.add(chunk);

        requestCreatingOrLoadingArea(chunkPosition);
        waitForChunks();
        chunkProvider.update();

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Assertions.assertTrue(eventArgumentCaptor.getAllValues().stream().anyMatch(x ->
                x instanceof OnChunkLoaded && ((OnChunkLoaded) x).getChunkPos().equals(chunkPosition)
        ), "Must be OnChunkLoaded event for chunk");
    }

    @Test
    void testLoadSingleChunkWithBlockLifecycle() throws InterruptedException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        Chunk chunk = new ChunkImpl(chunkPosition, blockManager, extraDataManager);
        generator.createChunk(chunk, null);
        storageManager.add(chunk);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));

        requestCreatingOrLoadingArea(chunkPosition);
        waitForChunks();
        chunkProvider.update();

        Assertions.assertTrue(((TestChunkStore) storageManager.loadChunkStore(chunkPosition)).isEntityRestored(),
                "Entities must be restored by loading");


        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(worldEntity, atLeast(1)).send(eventArgumentCaptor.capture());
        Assertions.assertTrue(eventArgumentCaptor.getAllValues().stream().anyMatch(x ->
                x instanceof OnChunkLoaded && ((OnChunkLoaded) x).getChunkPos().equals(chunkPosition)
        ), "Must be OnChunkLoaded event for chunk");

        //TODO, it is not clear if the activate/addedBlocks event logic is correct.
        //See https://github.com/MovingBlocks/Terasology/issues/3244
        final ArgumentCaptor<Event> blockEventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockAtBlockManager.getEntity(), atLeast(2)).send(blockEventCaptor.capture());
        Assertions.assertAll("Block events not valid",
                () -> {
                    Assertions.assertTrue(blockEventCaptor.getAllValues().stream().anyMatch(x ->
                        x instanceof OnAddedBlocks && ((OnAddedBlocks) x).blockCount() > 0
                    ), "Must be OnAddedBlocks event for chunk");
                },
                () -> {
                    Assertions.assertTrue(blockEventCaptor.getAllValues().stream().anyMatch(x ->
                            x instanceof OnActivatedBlocks && ((OnActivatedBlocks) x).blockCount() > 0
                    ), "Must be OnActivatedBlocks event for chunk");
                });
    }

    @Test
    void testUnloadChunkAndDeactivationBlock() throws InterruptedException {
        Vector3i chunkPosition = new Vector3i(0, 0, 0);
        blockAtBlockManager.setLifecycleEventsRequired(true);
        blockAtBlockManager.setEntity(mock(EntityRef.class));

        requestCreatingOrLoadingArea(chunkPosition);
        waitForChunks();
        relevanceSystem.removeRelevanceEntity(playerEntity);
        chunkProvider.notifyRelevanceChanged();

        // Wait for BeforeDeactivateBlocks event
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
        Assertions.assertTrue(eventArgumentCaptor.getAllValues()
                .stream()
                .filter((e) -> e instanceof BeforeChunkUnload)
                .map((e) -> (BeforeChunkUnload) e)
                .anyMatch(x -> x.getChunkPos().equals(chunkPosition)), "World events must have BeforeChunkUnload event when chunk was unload");

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
