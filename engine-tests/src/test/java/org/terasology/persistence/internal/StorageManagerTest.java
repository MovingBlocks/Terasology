/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.persistence.internal;

import com.google.common.collect.Lists;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.terasology.asset.AssetManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.stubs.EntityRefComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.Game;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.reflection.reflect.ReflectionReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.testUtil.ModuleManagerFactory;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.family.DefaultBlockFamilyFactoryRegistry;
import org.terasology.world.block.family.SymmetricFamily;
import org.terasology.world.block.internal.BlockManagerImpl;
import org.terasology.world.block.loader.WorldAtlas;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.internal.ChunkImpl;
import org.terasology.world.internal.WorldInfo;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Immortius
 * @author Florian <florian@fkoeberle.de>
 */
public class StorageManagerTest {

    public static final String PLAYER_ID = "someId";
    public static final Vector3i CHUNK_POS = new Vector3i(1, 2, 3);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private ModuleManager moduleManager;
    private NetworkSystem networkSystem;
    private ReadWriteStorageManager esm;
    private EngineEntityManager entityManager;
    private Block testBlock;
    private Block testBlock2;
    private EntityRef character;
    private Path savePath;

    @Before
    public void setup() throws Exception {
        JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(temporaryFolder.getRoot().toPath());
        savePath = PathManager.getInstance().getSavePath("testSave");

        assert !Files.isRegularFile(vfs.getPath("global.dat"));

        moduleManager = ModuleManagerFactory.create();
        networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        CoreRegistry.put(ModuleManager.class, moduleManager);
        CoreRegistry.put(Config.class, new Config());
        CoreRegistry.put(AssetManager.class, new AssetManager(moduleManager.getEnvironment()));
        CoreRegistry.put(NetworkSystem.class, networkSystem);

        entityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem,
                new ReflectionReflectFactory());

        esm = new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), entityManager, false);
        CoreRegistry.put(StorageManager.class, esm);

        this.character = entityManager.create();
        Client client = createClientMock(PLAYER_ID, character);

        when(networkSystem.getPlayers()).thenReturn(Arrays.asList(client));

        BlockManagerImpl blockManager = CoreRegistry.put(BlockManager.class, new BlockManagerImpl(mock(WorldAtlas.class), new DefaultBlockFamilyFactoryRegistry()));
        testBlock = new Block();
        testBlock.setId((short) 1);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:testblock"), testBlock), true);
        testBlock2 = new Block();
        testBlock2.setId((short) 2);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:testblock2"), testBlock2), true);


        ComponentSystemManager componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);

        CoreRegistry.put(ChunkProvider.class, mock(ChunkProvider.class));

        Game game = mock(Game.class);
        when(game.getTime()).thenReturn(mock(EngineTime.class));
        CoreRegistry.put(Game.class, game);
        BiomeManager biomeManager = mock(BiomeManager.class);
        when(biomeManager.getBiomes()).thenReturn(Collections.<Biome>emptyList());
        CoreRegistry.put(BiomeManager.class, biomeManager);
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        CoreRegistry.put(WorldProvider.class, worldProvider);


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
        EntityRef clientEntity = entityManager.create(clientComponent);
        return clientEntity;
    }

    @Test
    public void getUnstoredPlayerReturnsNewStor() {
        PlayerStore store = esm.loadPlayerStore(PLAYER_ID);
        assertNotNull(store);
        assertEquals(new Vector3f(), store.getRelevanceLocation());
        assertFalse(store.hasCharacter());
        assertEquals(PLAYER_ID, store.getId());
    }

    @Test
    public void storeAndRestoreOfPlayerWithoutCharacter() {
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
    public void playerRelevanceLocationSurvivesStorage() {
        Vector3f loc = new Vector3f(1, 2, 3);
        character.addComponent(new LocationComponent(loc));

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        assertEquals(loc, restored.getRelevanceLocation());
    }

    @Test
    public void characterSurvivesStorage() {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertTrue(restored.hasCharacter());
        assertEquals(character, restored.getCharacter());
    }

    @Test
    public void globalEntitiesStoredAndRestored() throws Exception {
        EntityRef entity = entityManager.create(new StringComponent("Test"));
        long entityId = entity.getId();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();
        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), newEntityManager, false);
        newSM.loadGlobalStore();

        List<EntityRef> entities = Lists.newArrayList(newEntityManager.getEntitiesWith(StringComponent.class));
        assertEquals(1, entities.size());
        assertEquals(entityId, entities.get(0).getId());
    }


    @Test
    public void referenceRemainsValidOverStorageRestoral() throws Exception {
        EntityRef someEntity = entityManager.create();
        character.addComponent(new EntityRefComponent(someEntity));

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), newEntityManager, false);
        newSM.loadGlobalStore();

        PlayerStore restored = newSM.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertTrue(restored.getCharacter().getComponent(EntityRefComponent.class).entityRef.exists());
    }

    @Test
    public void getUnstoredChunkReturnsNothing() {
        esm.loadChunkStore(CHUNK_POS);
    }

    @Test
    public void storeAndRestoreChunkStore() {
        Chunk chunk = new ChunkImpl(CHUNK_POS);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(Arrays.asList(chunk));
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
    public void chunkSurvivesStorageSaveAndRestore() throws Exception {
        Chunk chunk = new ChunkImpl(CHUNK_POS);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.setBlock(0, 4, 2, testBlock2);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(Arrays.asList(chunk));
        when(chunkProvider.getChunk(Matchers.any(Vector3i.class))).thenReturn(chunk);
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        boolean storeChunkInZips = true;

        esm.setStoreChunksInZips(storeChunkInZips);
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), newEntityManager,
                storeChunkInZips);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        assertNotNull(restored);
        assertEquals(CHUNK_POS, restored.getChunkPosition());
        assertNotNull(restored.getChunk());
        assertEquals(testBlock, restored.getChunk().getBlock(0, 0, 0));
        assertEquals(testBlock2, restored.getChunk().getBlock(0, 4, 2));
    }

    @Test
    public void entitySurvivesStorageInChunkStore() throws Exception {
        Chunk chunk = new ChunkImpl(CHUNK_POS);
        chunk.setBlock(0, 0, 0, testBlock);
        chunk.markReady();
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(Arrays.asList(chunk));
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        EntityRef entity = entityManager.create();
        long id = entity.getId();
        LocationComponent locationComponent = new LocationComponent();
        Vector3f positionInChunk = new Vector3f(chunk.getAABB().getMin());
        positionInChunk.x += 1;
        positionInChunk.y += 1;
        positionInChunk.z += 1;
        locationComponent.setWorldPosition(positionInChunk);
        entity.addComponent(locationComponent);
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new ReadWriteStorageManager(savePath, moduleManager.getEnvironment(), newEntityManager, false);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        restored.restoreEntities();
        EntityRef ref = newEntityManager.getEntity(id);
        assertTrue(ref.exists());
        assertTrue(ref.isActive());
    }


    @Test
    public void canSavePlayerWithoutUnloading() throws Exception {
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        assertTrue(character.isActive());
    }
}
