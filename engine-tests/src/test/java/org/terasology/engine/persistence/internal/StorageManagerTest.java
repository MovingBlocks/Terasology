// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.terasology.engine.TerasologyTestingEnvironment;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.bootstrap.EntitySystemSetupUtil;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.persistence.PlayerStore;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.recording.CharacterStateEventPositionMap;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.recording.RecordedEventStore;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.SymmetricFamily;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.loader.BlockFamilyDefinitionData;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.joml.geom.AABBfc;
import org.terasology.reflection.TypeRegistry;
import org.terasology.unittest.stubs.EntityRefComponent;
import org.terasology.unittest.stubs.StringComponent;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("TteTest")
@ExtendWith(MockitoExtension.class)
// TODO: Re-evaluate Mockito strictness settings after replacing TerasologyTestingEnvironment (#5010)
//   Mockito is currently in LENIENT mode because some of the mocked objects or methods are only used
//   by a subset of the tests.
@MockitoSettings(strictness = Strictness.LENIENT)
public class StorageManagerTest extends TerasologyTestingEnvironment {
    public static final String PLAYER_ID = "someId";
    public static final Vector3ic CHUNK_POS = new Vector3i(1, 2, 3);

    private ModuleEnvironment moduleEnvironment;
    private ReadWriteStorageManager esm;
    private EngineEntityManager entityManager;
    private BlockManager blockManager;
    private ExtraBlockDataManager extraDataManager;
    private Block testBlock;
    private Block testBlock2;
    private EntityRef character;
    private Path savePath;
    private RecordAndReplaySerializer recordAndReplaySerializer;
    private RecordAndReplayUtils recordAndReplayUtils;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    @BeforeEach
    public void setup(TestInfo testInfo) throws Exception {
        super.setup();

        String saveName = "testSave-" + testInfo.getTestMethod().map(Method::getName)
                .orElseGet(() -> UUID.randomUUID().toString());
        savePath = PathManager.getInstance().getSavePath(saveName);

        assertWithMessage("Leftover files in %s", savePath)
                .that(savePath.resolve("global.dat").toFile().exists())
                .isFalse();

        entityManager = context.get(EngineEntityManager.class);
        moduleEnvironment = mock(ModuleEnvironment.class);
        blockManager = context.get(BlockManager.class);
        extraDataManager = context.get(ExtraBlockDataManager.class);

        ModuleManager moduleManager = mock(ModuleManager.class);

        when(moduleManager.getEnvironment()).thenReturn(moduleEnvironment);

        RecordedEventStore recordedEventStore = new RecordedEventStore();
        recordAndReplayUtils = new RecordAndReplayUtils();
        CharacterStateEventPositionMap characterStateEventPositionMap = new CharacterStateEventPositionMap();
        DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList = new DirectionAndOriginPosRecorderList();
        recordAndReplaySerializer = new RecordAndReplaySerializer(entityManager, recordedEventStore,
                recordAndReplayUtils, characterStateEventPositionMap, directionAndOriginPosRecorderList,
                moduleManager, mock(TypeRegistry.class));
        recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);


        esm = new ReadWriteStorageManager(savePath, moduleEnvironment, entityManager, blockManager, extraDataManager,
                false, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        context.put(StorageManager.class, esm);

        this.character = entityManager.create();
        Client client = createClientMock(PLAYER_ID, character);
        NetworkSystem networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        when(networkSystem.getPlayers()).thenReturn(List.of(client));
        context.put(NetworkSystem.class, networkSystem);

        AssetManager assetManager = context.get(AssetManager.class);
        BlockFamilyDefinitionData data = new BlockFamilyDefinitionData();
        data.setBlockFamily(SymmetricFamily.class);
        assetManager.loadAsset(new ResourceUrn("test:testblock"), data, BlockFamilyDefinition.class);
        assetManager.loadAsset(new ResourceUrn("test:testblock2"), data, BlockFamilyDefinition.class);
        testBlock = context.get(BlockManager.class).getBlock("test:testblock");
        testBlock2 = context.get(BlockManager.class).getBlock("test:testblock2");

        context.put(ChunkProvider.class, mock(ChunkProvider.class));
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        context.put(WorldProvider.class, worldProvider);
    }

    private Client createClientMock(String clientId, EntityRef charac) {
        EntityRef clientEntity = createClientEntity(charac);
        Client client = mock(Client.class);
        when(client.getEntity()).thenReturn(clientEntity);
        when(client.getId()).thenReturn(clientId);
        return client;
    }

    private EntityRef createClientEntity(EntityRef charac) {
        ClientComponent clientComponent = new ClientComponent();
        clientComponent.local = true;
        clientComponent.character = charac;
        return entityManager.create(clientComponent);
    }

    @Test
    @Order(1)
    public void testGetUnstoredPlayerReturnsNewStor() {
        PlayerStore store = esm.loadPlayerStore(PLAYER_ID);
        assertNotNull(store);
        assertEquals(new Vector3f(), store.getRelevanceLocation());
        assertFalse(store.hasCharacter());
        assertEquals(PLAYER_ID, store.getId());
    }

    @Test
    public void testStoreAndRestoreOfPlayerWithoutCharacter() {
        // remove character from player:
        character.destroy();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restoredStore = esm.loadPlayerStore(PLAYER_ID);
        assertNotNull(restoredStore);
        assertFalse(restoredStore.hasCharacter());
        assertEquals(new Vector3f(), restoredStore.getRelevanceLocation());
    }

    @Test
    public void testPlayerRelevanceLocationSurvivesStorage() {
        Vector3f loc = new Vector3f(1, 2, 3);
        character.addComponent(new LocationComponent(loc));

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        assertEquals(loc, restored.getRelevanceLocation());
    }

    @Test
    public void testCharacterSurvivesStorage() {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertTrue(restored.hasCharacter());
        assertEquals(character, restored.getCharacter());
    }

    @Test
    public void testGlobalEntitiesStoredAndRestored() throws Exception {
        EntityRef entity = entityManager.create(new StringComponent("Test"));
        long entityId = entity.getId();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);

        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, false, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        List<EntityRef> entities = Lists.newArrayList(newEntityManager.getEntitiesWith(StringComponent.class));
        assertEquals(1, entities.size());
        assertEquals(entityId, entities.get(0).getId());
    }


    @Test
    public void testReferenceRemainsValidOverStorageRestoral() throws Exception {
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

        PlayerStore restored = newSM.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertTrue(restored.getCharacter().getComponent(EntityRefComponent.class).entityRef.exists());
    }

    @Test
    public void testGetUnstoredChunkReturnsNothing() {
        esm.loadChunkStore(CHUNK_POS);
    }

    @Test
    public void testStoreAndRestoreChunkStore() {
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
    public void testChunkSurvivesStorageSaveAndRestore() throws Exception {
        Chunk chunk = new ChunkImpl(CHUNK_POS, blockManager, extraDataManager);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.setBlock(0, 4, 2, testBlock2);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(List.of(chunk));
        when(chunkProvider.getChunk(ArgumentMatchers.any(Vector3ic.class))).thenReturn(chunk);
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        boolean storeChunkInZips = true;

        esm.setStoreChunksInZips(storeChunkInZips);
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EntitySystemSetupUtil.addReflectionBasedLibraries(context);
        EntitySystemSetupUtil.addEntityManagementRelatedClasses(context);
        EngineEntityManager newEntityManager = context.get(EngineEntityManager.class);
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleEnvironment, newEntityManager, blockManager,
                extraDataManager, storeChunkInZips, recordAndReplaySerializer, recordAndReplayUtils,
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
    public void testEntitySurvivesStorageInChunkStore() throws Exception {
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
                extraDataManager, false, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        restored.restoreEntities();
        EntityRef ref = newEntityManager.getEntity(id);
        assertTrue(ref.exists());
        assertTrue(ref.isActive());
    }


    @Test
    public void testCanSavePlayerWithoutUnloading() {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        assertTrue(character.isActive());
    }
}
