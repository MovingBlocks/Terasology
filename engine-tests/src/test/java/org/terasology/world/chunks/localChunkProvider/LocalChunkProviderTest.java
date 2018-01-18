/*
 * Copyright 2017 MovingBlocks
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

import gnu.trove.map.hash.TShortObjectHashMap;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.event.OnChunkGenerated;
import org.terasology.world.chunks.event.OnChunkLoaded;
import org.terasology.world.chunks.internal.ReadyChunkInfo;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class LocalChunkProviderTest {

    private LocalChunkProvider chunkProvider;
    private ChunkFinalizer chunkFinalizer;
    private EntityManager entityManager;

    @Before
    public void setUp() throws Exception {
        entityManager = mock(EntityManager.class);
        chunkFinalizer = mock(ChunkFinalizer.class);
        chunkProvider = new LocalChunkProvider(null,
                entityManager, null, null, null, chunkFinalizer, null);
    }

    @Test
    public void testCompleteUpdateHandlesFinalizedChunkIfReady() throws Exception {
        final EntityRef worldEntity = mock(EntityRef.class);
        chunkProvider.setWorldEntity(worldEntity);
        final Chunk chunk = mock(Chunk.class);
        when(chunk.getPosition()).thenReturn(new Vector3i(0, 0, 0));
        final ReadyChunkInfo readyChunkInfo = new ReadyChunkInfo(chunk, new TShortObjectHashMap<>(), Collections.emptyList());
        when(chunkFinalizer.completeFinalization()).thenReturn(readyChunkInfo);

        chunkProvider.completeUpdate();

        final InOrder inOrder = inOrder(worldEntity);
        inOrder.verify(worldEntity).send(any(OnChunkGenerated.class));
        inOrder.verify(worldEntity).send(any(OnChunkLoaded.class));
    }

    @Test
    public void testCompleteUpdateGeneratesStoredEntities() throws Exception {
        final EntityRef worldEntity = mock(EntityRef.class);
        chunkProvider.setWorldEntity(worldEntity);
        final Chunk chunk = mock(Chunk.class);
        when(chunk.getPosition()).thenReturn(new Vector3i(0, 0, 0));
        final EntityStore entityStore = new EntityStore();
        final ChunkProviderTestComponent testComponent = new ChunkProviderTestComponent();
        entityStore.addComponent(testComponent);
        final List<EntityStore> entityStores = Collections.singletonList(entityStore);
        final ReadyChunkInfo readyChunkInfo = new ReadyChunkInfo(chunk, new TShortObjectHashMap<>(), entityStores);
        when(chunkFinalizer.completeFinalization()).thenReturn(readyChunkInfo);
        final EntityRef mockEntity = mock(EntityRef.class);
        when(entityManager.create()).thenReturn(mockEntity);

        chunkProvider.completeUpdate();

        verify(entityManager).create();
        verify(mockEntity).addComponent(eq(testComponent));
    }
    @Test
    public void testCompleteUpdateGeneratesStoredEntitiesFromPrefab() throws Exception {
        final EntityRef worldEntity = mock(EntityRef.class);
        chunkProvider.setWorldEntity(worldEntity);
        final Chunk chunk = mock(Chunk.class);
        when(chunk.getPosition()).thenReturn(new Vector3i(0, 0, 0));
        final Prefab prefab = mock(Prefab.class);
        final EntityStore entityStore = new EntityStore(prefab);
        final ChunkProviderTestComponent testComponent = new ChunkProviderTestComponent();
        entityStore.addComponent(testComponent);
        final List<EntityStore> entityStores = Collections.singletonList(entityStore);
        final ReadyChunkInfo readyChunkInfo = new ReadyChunkInfo(chunk, new TShortObjectHashMap<>(), entityStores);
        when(chunkFinalizer.completeFinalization()).thenReturn(readyChunkInfo);
        final EntityRef mockEntity = mock(EntityRef.class);
        when(entityManager.create(any(Prefab.class))).thenReturn(mockEntity);

        chunkProvider.completeUpdate();

        verify(entityManager).create(eq(prefab));
        verify(mockEntity).addComponent(eq(testComponent));
    }

    private static class ChunkProviderTestComponent implements Component{

    }
}
