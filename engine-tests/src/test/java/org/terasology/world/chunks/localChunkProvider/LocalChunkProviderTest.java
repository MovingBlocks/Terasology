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
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TShortObjectHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.entitySystem.event.Event;
import org.terasology.entitySystem.prefab.Prefab;
import org.joml.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.OnActivatedBlocks;
import org.terasology.world.block.OnAddedBlocks;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.event.OnChunkGenerated;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.internal.ReadyChunkInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LocalChunkProviderTest {

    private LocalChunkProvider chunkProvider;
    private ChunkFinalizer chunkFinalizer;
    private EntityManager entityManager;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private BlockEntityRegistry blockEntityRegistry;
    private EntityRef worldEntity;
    private ChunkCache chunkCache;

    @BeforeEach
    public void setUp() {
        entityManager = mock(EntityManager.class);
        chunkFinalizer = mock(ChunkFinalizer.class);
        blockManager = mock(BlockManager.class);
        extraDataManager = new ExtraBlockDataManager();
        blockEntityRegistry = mock(BlockEntityRegistry.class);
        worldEntity = mock(EntityRef.class);
        chunkCache = new ConcurrentMapChunkCache();
        chunkProvider = new LocalChunkProvider(null,
                entityManager, null, blockManager, extraDataManager, chunkFinalizer, null, chunkCache);
        chunkProvider.setBlockEntityRegistry(blockEntityRegistry);
        chunkProvider.setWorldEntity(worldEntity);
    }

    @Test
    public void testCompleteUpdateMarksChunkReady() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForNewChunk(chunk, new TShortObjectHashMap<>(), Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));

        chunkProvider.completeUpdate();

        verify(chunk).markReady();
    }

    @Test
    public void testCompleteUpdateHandlesFinalizedChunkIfReady() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForNewChunk(chunk, new TShortObjectHashMap<>(), Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));

        chunkProvider.completeUpdate();

        final InOrder inOrderVerification = inOrder(worldEntity);
        inOrderVerification.verify(worldEntity).send(any(OnChunkGenerated.class));
        inOrderVerification.verify(worldEntity).send(any(OnChunkLoaded.class));
    }

    @Test
    public void testCompleteUpdateGeneratesStoredEntities() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final ChunkProviderTestComponent testComponent = new ChunkProviderTestComponent();
        final EntityStore entityStore = createEntityStoreWithComponents(testComponent);
        final List<EntityStore> entityStores = Collections.singletonList(entityStore);
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForNewChunk(chunk, new TShortObjectHashMap<>(), entityStores);
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));
        final EntityRef mockEntity = mock(EntityRef.class);
        when(entityManager.create()).thenReturn(mockEntity);

        chunkProvider.completeUpdate();

        verify(mockEntity).addComponent(eq(testComponent));
    }

    @Test
    public void testCompleteUpdateGeneratesStoredEntitiesFromPrefab() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final Prefab prefab = mock(Prefab.class);
        final ChunkProviderTestComponent testComponent = new ChunkProviderTestComponent();
        final EntityStore entityStore = createEntityStoreWithPrefabAndComponents(prefab, testComponent);
        final List<EntityStore> entityStores = Collections.singletonList(entityStore);
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForNewChunk(chunk, new TShortObjectHashMap<>(), entityStores);
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));
        final EntityRef mockEntity = mock(EntityRef.class);
        when(entityManager.create(any(Prefab.class))).thenReturn(mockEntity);

        chunkProvider.completeUpdate();

        verify(entityManager).create(eq(prefab));
        verify(mockEntity).addComponent(eq(testComponent));
    }

    @Test
    public void testCompleteUpdateRestoresEntitiesForRestoredChunks() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final ChunkStore chunkStore = mock(ChunkStore.class);
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForRestoredChunk(chunk, new TShortObjectHashMap<>(), chunkStore, Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));

        chunkProvider.completeUpdate();

        verify(chunkStore).restoreEntities();
    }

    @Test
    public void testCompleteUpdateSendsBlockAddedEvents() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final short blockId = 42;
        final EntityRef blockEntity = mock(EntityRef.class);
        registerBlockWithIdAndEntity(blockId, blockEntity, blockManager);
        final TShortObjectHashMap<TIntList> blockPositionMappings = new TShortObjectHashMap<>();
        blockPositionMappings.put(blockId, withPositions(new Vector3i(1, 2, 3)));
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForRestoredChunk(chunk, blockPositionMappings, mock(ChunkStore.class), Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));

        chunkProvider.completeUpdate();

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockEntity, atLeastOnce()).send(eventArgumentCaptor.capture());
        final Event event = eventArgumentCaptor.getAllValues().get(0);
        assertTrue(event instanceof OnAddedBlocks);
        Iterable<Vector3i> positions = ((OnAddedBlocks) event).getBlockPositions();
        assertTrue(Lists.newArrayList(positions).contains(new Vector3i(1, 2, 3)));
    }

    @Test
    public void testCompleteUpdateSendsBlockActivatedEvents() throws Exception {
        final Chunk chunk = mockChunkAt(0, 0, 0);
        final TShortObjectHashMap<TIntList> blockPositionMappings = new TShortObjectHashMap<>();
        final short blockId = 42;
        final EntityRef blockEntity = mock(EntityRef.class);
        registerBlockWithIdAndEntity(blockId, blockEntity, blockManager);
        blockPositionMappings.put(blockId, withPositions(new Vector3i(1, 2, 3)));
        final ReadyChunkInfo readyChunkInfo = ReadyChunkInfo.createForRestoredChunk(chunk, blockPositionMappings, mock(ChunkStore.class), Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(Collections.singletonList(readyChunkInfo));

        chunkProvider.completeUpdate();

        final ArgumentCaptor<Event> eventArgumentCaptor = ArgumentCaptor.forClass(Event.class);
        verify(blockEntity, atLeastOnce()).send(eventArgumentCaptor.capture());
        final Event event = eventArgumentCaptor.getAllValues().get(1);
        assertTrue(event instanceof OnActivatedBlocks);
        Iterable<Vector3i> positions = ((OnActivatedBlocks) event).getBlockPositions();
        assertTrue(Lists.newArrayList(positions).contains(new Vector3i(1, 2, 3)));
    }

    private static void markAllChunksAsReady(final ChunkCache chunkCache) {
        markAllChunksAsReadyExcludingPosition(chunkCache, null);
    }

    private static void markAllChunksAsReadyExcludingPosition(final ChunkCache chunkCache, final Vector3i positionToExclude) {
        chunkCache.getAllChunks().stream()
                .filter(chunk -> !chunk.getPosition().equals(positionToExclude))
                .forEach(c -> when(c.isReady()).thenReturn(true));
    }

    private static void generateMockChunkCubeWithSideWidthAround(final Vector3i position, final int sideWidth, final ChunkCache chunkCache) {
        for (int x = position.x() - sideWidth; x <= position.x() + sideWidth; x++) {
            for (int y = position.y() - sideWidth; y <= position.y() + sideWidth; y++) {
                for (int z = position.z() - sideWidth; z <= position.z() + sideWidth; z++) {
                    if (x == position.x() && y == position.y() && z == position.z()) {
                        //dont override the inner chunk
                        continue;
                    }
                    chunkCache.put(new Vector3i(x, y, z), mockChunkAt(x, y, z));
                }
            }
        }
    }

    private static EntityStore createEntityStoreWithComponents(Component... components) {
        return createEntityStoreWithPrefabAndComponents(null, components);
    }

    private static EntityStore createEntityStoreWithPrefabAndComponents(Prefab prefab, Component... components) {
        final EntityStore entityStore = new EntityStore(prefab);
        for (Component component : components) {
            entityStore.addComponent(component);
        }
        return entityStore;
    }

    private static Chunk mockChunkAt(final int x, final int y, final int z) {
        final Chunk chunk = mock(Chunk.class);
        when(chunk.getPosition()).thenReturn(new Vector3i(x, y, z));
        return chunk;
    }


    private static Chunk mockChunkWithReadinessStateAt(final int x, final int y, final int z) {
        final Chunk chunk = mockChunkAt(x, y, z);
        AtomicBoolean chunkReady = new AtomicBoolean();
        when(chunk.isReady()).thenAnswer(i -> chunkReady.get());
        doAnswer(i -> {
            chunkReady.set(true);
            return null;
        }).when(chunk).markReady();
        return chunk;
    }

    private static TIntArrayList withPositions(final Vector3i position) {
        final TIntArrayList positions = new TIntArrayList();
        positions.add(position.x);
        positions.add(position.y);
        positions.add(position.z);
        return positions;
    }

    private static void registerBlockWithIdAndEntity(final short blockId, final EntityRef blockEntity, final BlockManager blockManager) {
        final Block block = new Block();
        block.setEntity(blockEntity);
        when(blockManager.getBlock(eq(blockId))).thenReturn(block);
    }

    private static class ChunkProviderTestComponent implements Component {

    }
}
