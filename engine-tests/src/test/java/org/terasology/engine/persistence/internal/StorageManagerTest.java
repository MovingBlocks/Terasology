// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.NonPlayerVisibleSubsystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.integrationenvironment.jupiter.IntegrationEnvironment;
import org.terasology.engine.integrationenvironment.jupiter.MTEExtension;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.persistence.PlayerStore;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.joml.geom.AABBfc;
import org.terasology.unittest.stubs.EntityRefComponent;
import org.terasology.unittest.stubs.StringComponent;

import java.nio.file.Path;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("MteTest")
@ExtendWith({MockitoExtension.class, MTEExtension.class})
@IntegrationEnvironment(
        networkMode = NetworkMode.NONE,  // A mode with a LocalPlayer.
        subsystem = StorageManagerTest.EnableWritingSaveGames.class
)
public class StorageManagerTest {
    public static final Vector3ic CHUNK_POS = new Vector3i(1, 2, 3);

    @In
    EngineEntityManager entityManager;

    private String playerId;
    private ModuleEnvironment moduleEnvironment;
    private Block testBlock;
    private Block testBlock2;
    private EntityRef character;
    private Path savePath;
    private RecordAndReplaySerializer recordAndReplaySerializer;
    private RecordAndReplayUtils recordAndReplayUtils;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @BeforeEach
    void setupLocals(
            StorageManager storageManager, LocalPlayer player, NetworkSystem network, BlockManager blockManager,
            ModuleManager moduleManager, Config config, Context context) {
        assertThat(storageManager).isInstanceOf(ReadWriteStorageManager.class);
        context.put(ReadWriteStorageManager.class, (ReadWriteStorageManager) storageManager);

        savePath = getSavePath(config);

        assertWithMessage("Leftover files in %s", savePath)
                .that(savePath.resolve("global.dat").toFile().exists())
                .isFalse();

        character = player.getCharacterEntity();
        assertThat(character).isNotEqualTo(EntityRef.NULL);

        var clients = ImmutableList.copyOf(network.getPlayers());
        assertThat(clients).hasSize(1);
        playerId = clients.get(0).getId();

        testBlock = blockManager.getBlock("test:testblock");
        testBlock2 = blockManager.getBlock("test:testblock2");

        moduleEnvironment = moduleManager.getEnvironment();
    }

    Path getSavePath(Config config) {
        // TODO: add a more direct way of inspecting StorageManager's path.
        //   This way of getting the game title (and thus the path) is a bit brittle,
        //   because world title and game title are not _required_ to be identical.
        String gameTitle = config.getWorldGeneration().getWorldTitle();
        return PathManager.getInstance().getSavePath(gameTitle);
    }

    @Test
    @Order(1)
    public void testGetUnstoredPlayerReturnsNewStor(StorageManager esm) {
        PlayerStore store = esm.loadPlayerStore(playerId);
        assertNotNull(store);
        assertEquals(new Vector3f(), store.getRelevanceLocation());
        assertFalse(store.hasCharacter());
        assertEquals(playerId, store.getId());
    }

    @Test
    public void testStoreAndRestoreOfPlayerWithoutCharacter(StorageManager esm) {
        // remove character from player:
        character.destroy();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restoredStore = esm.loadPlayerStore(playerId);
        assertNotNull(restoredStore);
        assertFalse(restoredStore.hasCharacter());
        assertEquals(new Vector3f(), restoredStore.getRelevanceLocation());
    }

    @Test
    public void testPlayerRelevanceLocationSurvivesStorage(StorageManager esm) {
        Vector3f loc = new Vector3f(1, 2, 3);
        character.addComponent(new LocationComponent(loc));

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(playerId);
        assertEquals(loc, restored.getRelevanceLocation());
    }

    @Test
    public void testCharacterSurvivesStorage(StorageManager esm) {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(playerId);
        restored.restoreEntities();
        assertTrue(restored.hasCharacter());
        assertEquals(character, restored.getCharacter());
    }

    @Test
    public void testGlobalEntitiesStoredAndRestored(
            ReadWriteStorageManager esm, BlockManager blockManager, ExtraBlockDataManager extraDataManager, Context context) throws Exception {
        EntityRef entity = entityManager.create(new StringComponent("Test"));
        long entityId = entity.getId();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);

        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, esm.isStoreChunksInZips(), recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        List<EntityRef> entities = Lists.newArrayList(newEntityManager.getEntitiesWith(StringComponent.class));
        assertEquals(1, entities.size());
        assertEquals(entityId, entities.get(0).getId());
    }


    @Test
    public void testReferenceRemainsValidOverStorageRestoral(
            StorageManager esm, BlockManager blockManager, ExtraBlockDataManager extraDataManager, Context context) throws Exception {
        EntityRef someEntity = entityManager.create();
        character.addComponent(new EntityRefComponent(someEntity));

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, false, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        PlayerStore restored = newSM.loadPlayerStore(playerId);
        restored.restoreEntities();
        assertTrue(restored.getCharacter().getComponent(EntityRefComponent.class).entityRef.exists());
    }

    @Test
    public void testGetUnstoredChunkReturnsNothing(StorageManager esm) {
        assertThat(esm.loadChunkStore(CHUNK_POS)).isNull();
    }

    @Test
    public void testStoreAndRestoreChunkStore(
            StorageManager esm, BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        Chunk chunk = new ChunkImpl(CHUNK_POS, blockManager, extraDataManager);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(List.of(chunk));
        CoreRegistry.put(ChunkProvider.class, chunkProvider);

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        ChunkStore restored = esm.loadChunkStore(CHUNK_POS);
        assertNotNull(restored);
        assertEquals(CHUNK_POS, restored.getChunkPosition());
        assertNotNull(restored.getChunk());
        assertEquals(testBlock, restored.getChunk().getBlock(0, 0, 0));
    }

    @Test
    public void testChunkSurvivesStorageSaveAndRestore(
            ReadWriteStorageManager esm, BlockManager blockManager, ExtraBlockDataManager extraDataManager,
            Context context) throws Exception {
        Chunk chunk = new ChunkImpl(CHUNK_POS, blockManager, extraDataManager);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.setBlock(0, 4, 2, testBlock2);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(List.of(chunk));
        CoreRegistry.put(ChunkProvider.class, chunkProvider);

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, esm.isStoreChunksInZips(), recordAndReplaySerializer, recordAndReplayUtils,
                recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        assertNotNull(restored);
        assertEquals(CHUNK_POS, restored.getChunkPosition());
        assertNotNull(restored.getChunk());
        assertEquals(testBlock, restored.getChunk().getBlock(0, 0, 0));
        assertEquals(testBlock2, restored.getChunk().getBlock(0, 4, 2));
    }

    @Test
    public void testEntitySurvivesStorageInChunkStore(
            ReadWriteStorageManager esm, BlockManager blockManager, ExtraBlockDataManager extraDataManager,
            Context context) throws Exception {
        Chunk chunk = new ChunkImpl(CHUNK_POS, blockManager, extraDataManager);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(List.of(chunk));
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        EntityRef entity = entityManager.create();
        long id = entity.getId();
        LocationComponent locationComponent = new LocationComponent();
        AABBfc aabb = chunk.getAABB();
        Vector3f positionInChunk = new Vector3f(aabb.minX(), aabb.minY(), aabb.minZ());
        positionInChunk.x += 1;
        positionInChunk.y += 1;
        positionInChunk.z += 1;
        locationComponent.setWorldPosition(positionInChunk);
        entity.addComponent(locationComponent);
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, esm.isStoreChunksInZips(), recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        restored.restoreEntities();
        EntityRef ref = newEntityManager.getEntity(id);
        assertTrue(ref.exists());
        assertTrue(ref.isActive());
    }


    @Test
    public void testCanSavePlayerWithoutUnloading(StorageManager esm) {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        assertTrue(character.isActive());
    }

    static class EnableWritingSaveGames extends NonPlayerVisibleSubsystem {
        @Override
        public void initialise(GameEngine engine, Context rootContext) {
            rootContext.getValue(SystemConfig.class).writeSaveGamesEnabled.set(true);
        }
    }
}
