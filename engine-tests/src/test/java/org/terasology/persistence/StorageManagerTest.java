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
package org.terasology.persistence;

import com.google.common.collect.Lists;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.terasology.TerasologyTestingEnvironment;
import org.terasology.asset.AssetManager;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.EngineTime;
import org.terasology.engine.Time;
import org.terasology.engine.bootstrap.EntitySystemBuilder;
import org.terasology.engine.modes.loadProcesses.LoadPrefabs;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.stubs.EntityRefComponent;
import org.terasology.entitySystem.stubs.StringComponent;
import org.terasology.game.Game;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.network.Client;
import org.terasology.network.NetworkMode;
import org.terasology.network.NetworkSystem;
import org.terasology.network.internal.NetworkSystemImpl;
import org.terasology.persistence.internal.StorageManagerInternal;
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

import javax.vecmath.Vector3f;

import java.nio.file.FileSystem;
import java.nio.file.Files;
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
 */
public class StorageManagerTest {

    public static final String PLAYER_ID = "someId";
    public static final Vector3i CHUNK_POS = new Vector3i(1, 2, 3);

    private ModuleManager moduleManager;
    private NetworkSystem networkSystem;
    private StorageManagerInternal esm;
    private EngineEntityManager entityManager;
    private Block testBlock;
    private Block testBlock2;

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws Exception {
        JavaArchive homeArchive = ShrinkWrap.create(JavaArchive.class);
        FileSystem vfs = ShrinkWrapFileSystems.newFileSystem(homeArchive);
        PathManager.getInstance().useOverrideHomePath(temporaryFolder.getRoot().toPath());
        PathManager.getInstance().setCurrentSaveTitle("testSave");

        assert !Files.isRegularFile(vfs.getPath("global.dat"));

        moduleManager = ModuleManagerFactory.create();
        networkSystem = mock(NetworkSystem.class);
        when(networkSystem.getMode()).thenReturn(NetworkMode.NONE);
        // TODO return list to verify it gets saved properly
        when(networkSystem.getPlayers()).thenReturn(Collections.<Client> emptyList());
        CoreRegistry.put(ModuleManager.class, moduleManager);
        CoreRegistry.put(Config.class, new Config());
        CoreRegistry.put(AssetManager.class, new AssetManager(moduleManager.getEnvironment()));
        CoreRegistry.put(NetworkSystem.class, networkSystem);

        entityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());

        BlockManagerImpl blockManager = CoreRegistry.put(BlockManager.class, new BlockManagerImpl(mock(WorldAtlas.class), new DefaultBlockFamilyFactoryRegistry()));
        testBlock = new Block();
        testBlock.setId((short) 1);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:testblock"), testBlock), true);
        testBlock2 = new Block();
        testBlock2.setId((short) 2);
        blockManager.addBlockFamily(new SymmetricFamily(new BlockUri("test:testblock2"), testBlock2), true);

        esm = new StorageManagerInternal(moduleManager.getEnvironment(), entityManager, false);
        CoreRegistry.put(StorageManager.class, esm);

        ComponentSystemManager componentSystemManager = new ComponentSystemManager();
        CoreRegistry.put(ComponentSystemManager.class, componentSystemManager);
        
        CoreRegistry.put(ChunkProvider.class, mock(ChunkProvider.class));

        Game game = mock(Game.class);
        when(game.getTime()).thenReturn(mock(EngineTime.class));
        CoreRegistry.put(Game.class, game);
        BiomeManager biomeManager = mock(BiomeManager.class);
        when(biomeManager.getBiomes()).thenReturn(Collections.<Biome> emptyList());
        CoreRegistry.put(BiomeManager.class, biomeManager);
        WorldProvider worldProvider = mock(WorldProvider.class);
        when(worldProvider.getWorldInfo()).thenReturn(new WorldInfo());
        CoreRegistry.put(WorldProvider.class, worldProvider);
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
    public void storeAndRestorePlayerStore() {
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.save();
        PlayerStore restoredStore = esm.loadPlayerStore(PLAYER_ID);
        assertNotNull(restoredStore);
        assertFalse(restoredStore.hasCharacter());
        assertEquals(new Vector3f(), restoredStore.getRelevanceLocation());
    }

    @Test
    public void playerRelevanceLocationSurvivesStorage() {
        Vector3f loc = new Vector3f(1, 2, 3);
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setRelevanceLocation(loc);
        assertEquals(loc, store.getRelevanceLocation());
        store.save();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        assertEquals(loc, restored.getRelevanceLocation());
    }

    @Test
    public void addCharacterSurvivesStorage() {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        assertFalse(store.hasCharacter());
        store.setCharacter(character);
        assertTrue(store.hasCharacter());
        assertEquals(character, store.getCharacter());
        store.save();

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertTrue(restored.hasCharacter());
        assertEquals(character, restored.getCharacter());
    }

    @Test
    public void relevanceLocationSetToCharacterLocation() {
        Vector3f loc = new Vector3f(1, 2, 3);
        EntityRef character = entityManager.create(new LocationComponent(loc));
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        assertEquals(loc, store.getRelevanceLocation());
    }

    @Test
    public void characterEntityDeactivatedWhileStored() {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();
        assertFalse(character.isActive());
    }

    @Test
    public void referenceCorrectlyInvalidatedWhileStored() {
        EntityRef someEntity = entityManager.create();
        EntityRef character = entityManager.create(new EntityRefComponent(someEntity));
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();

        someEntity.destroy();
        entityManager.create(); // This causes the destroyed entity's id to be reused

        PlayerStore restored = esm.loadPlayerStore(PLAYER_ID);
        restored.restoreEntities();
        assertFalse(character.getComponent(EntityRefComponent.class).entityRef.exists());
    }

    @Test
    public void canSaveAndRestoreStorage() throws Exception {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();

        esm.flush();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new StorageManagerInternal(moduleManager.getEnvironment(), newEntityManager);
        newSM.loadGlobalStore();
        assertNotNull(newSM.loadPlayerStore(PLAYER_ID));
    }

    

    @Test
    public void globalEntitiesStoredAndRestored() throws Exception {
        EntityRef entity = entityManager.create(new StringComponent("Test"));
        int entityId = entity.getId();

        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();
        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new StorageManagerInternal(moduleManager.getEnvironment(), newEntityManager, false);
        newSM.loadGlobalStore();

        List<EntityRef> entities = Lists.newArrayList(newEntityManager.getEntitiesWith(StringComponent.class));
        assertEquals(1, entities.size());
        assertEquals(entityId, entities.get(0).getId());
    }


    @Test
    public void referenceRemainsValidOverStorageRestoral() throws Exception {
        EntityRef someEntity = entityManager.create();
        EntityRef character = entityManager.create(new EntityRefComponent(someEntity));
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save();
        esm.createGlobalStoreForSave();

        esm.flush();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new StorageManagerInternal(moduleManager.getEnvironment(), newEntityManager, false);
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
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(Arrays.asList(chunk));
        when(chunkProvider.getChunk(Mockito.any(Vector3i.class))).thenReturn(chunk);
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        boolean storeChunkInZips = true;

        esm.setStoreChunksInZips(storeChunkInZips);
        esm.waitForCompletionOfPreviousSaveAndStartSaving();
        esm.finishSavingAndShutdown();

        EngineEntityManager newEntityManager = new EntitySystemBuilder().build(moduleManager.getEnvironment(), networkSystem, new ReflectionReflectFactory());
        StorageManager newSM = new StorageManagerInternal(moduleManager.getEnvironment(), newEntityManager,
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
        ChunkProvider chunkProvider = mock(ChunkProvider.class);
        when(chunkProvider.getAllChunks()).thenReturn(Arrays.asList(chunk));
        CoreRegistry.put(ChunkProvider.class, chunkProvider);
        EntityRef entity = entityManager.create();
        int id = entity.getId();
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
        StorageManager newSM = new StorageManagerInternal(moduleManager.getEnvironment(), newEntityManager, false);
        newSM.loadGlobalStore();

        ChunkStore restored = newSM.loadChunkStore(CHUNK_POS);
        restored.restoreEntities();
        EntityRef ref = newEntityManager.getEntity(id);
        assertTrue(ref.exists());
        assertTrue(ref.isActive());
    }

    @Test
    public void canSavePlayerWithoutUnloading() throws Exception {
        EntityRef character = entityManager.create();
        PlayerStore store = esm.createPlayerStoreForSave(PLAYER_ID);
        store.setCharacter(character);
        store.save(false);

        esm.flush();

        assertTrue(character.isActive());
    }

    @Test
    public void ignoresDestroyOfUnreferencedEntity() {
        esm.onEntityDestroyed(3);
    }
}
