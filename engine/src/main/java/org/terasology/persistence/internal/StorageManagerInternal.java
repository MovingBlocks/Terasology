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
import com.google.common.collect.Maps;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityDestroySubscriber;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Vector3i;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.concurrency.ShutdownTask;
import org.terasology.utilities.concurrency.Task;
import org.terasology.utilities.concurrency.TaskMaster;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.Biome;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.internal.ChunkImpl;

import javax.vecmath.Vector3f;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.GZIPInputStream;

/**
 * @author Immortius
 * @author Florian <florian@fkoeberle.de>
 */
public final class StorageManagerInternal implements StorageManager, EntityDestroySubscriber {
    private static final Logger logger = LoggerFactory.getLogger(StorageManagerInternal.class);

    private final TaskMaster<Task> saveThreadManager;

    private ModuleEnvironment environment;
    private EngineEntityManager entityManager;
    private PrefabSerializer prefabSerializer;

    private TIntObjectMap<List<StoreMetadata>> externalRefHolderLookup = new TIntObjectHashMap<>();
    private Map<StoreId, StoreMetadata> storeMetadata = Maps.newHashMap();

    private boolean storeChunksInZips = true;
    private final StoragePathProvider storagePathProvider;
    private SaveTransaction saveTransaction;
    private Config config;

    /**
     * Time of the next save in the format that {@link System#currentTimeMillis()} returns.
     */
    private Long nextAutoSave;
    private boolean saveRequested;
    private ConcurrentMap<Vector3i, CompressedChunkBuilder> unloadedAndUnsavedChunkMap = Maps.newConcurrentMap();
    private ConcurrentMap<Vector3i, CompressedChunkBuilder> unloadedAndSavingChunkMap = Maps.newConcurrentMap();

    public StorageManagerInternal(ModuleEnvironment environment, EngineEntityManager entityManager) {
        this(environment, entityManager, true);
    }

    public StorageManagerInternal(ModuleEnvironment environment, EngineEntityManager entityManager, boolean storeChunksInZips) {
        this.entityManager = entityManager;
        this.environment = environment;
        this.storeChunksInZips = storeChunksInZips;
        this.prefabSerializer = new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        entityManager.subscribe(this);
        this.storagePathProvider = new StoragePathProvider(PathManager.getInstance().getCurrentSavePath());
        this.saveThreadManager = TaskMaster.createFIFOTaskMaster("Saving", 1);
        this.config = CoreRegistry.get(Config.class);

    }

    @Override
    public void finishSavingAndShutdown() {
        saveThreadManager.shutdown(new ShutdownTask(), true);
        checkSaveTransactionAndClearUpIfItIsDone();
    }

    private void checkSaveTransactionAndClearUpIfItIsDone() {
        if (saveTransaction != null) {
            SaveTransactionResult result = saveTransaction.getResult();
            if (result != null) {
                Throwable t = saveTransaction.getResult().getCatchedThrowable();
                if (t != null) {
                    throw new RuntimeException("Saving failed", t);
                }
                saveTransaction = null;
            }
            unloadedAndSavingChunkMap.clear();
        }
    }


    @Override
    public void onPlayerDisconnect(String id) {
        // TODO handle player disconnect. Maybe a new player manager would make sense?
    }

    private void createGlobalStoreForSave(SaveTransactionBuilder saveTransaction) {
        GlobalStoreSaver globalStoreSaver = new GlobalStoreSaver(entityManager, prefabSerializer);
        for (StoreMetadata table : storeMetadata.values()) {
            globalStoreSaver.addStoreMetadata(table);
        }
        for (EntityRef entity : entityManager.getAllEntities()) {
            globalStoreSaver.store(entity);

        }
        EntityData.GlobalStore globalStore = globalStoreSaver.save();
        saveTransaction.addGlobalStore(globalStore);
    }

    @Override
    public void loadGlobalStore() throws IOException {

        Path globalDataFile = storagePathProvider.getGlobalEntityStorePath();
        if (Files.isRegularFile(globalDataFile)) {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(globalDataFile))) {
                EntityData.GlobalStore store = EntityData.GlobalStore.parseFrom(in);
                GlobalStoreLoader loader = new GlobalStoreLoader(environment, entityManager, prefabSerializer);
                loader.load(store);
                for (StoreMetadata refTable : loader.getStoreMetadata()) {
                    storeMetadata.put(refTable.getId(), refTable);
                    indexStoreMetadata(refTable);
                }
            }
        }
    }

    @Override
    public PlayerStore loadPlayerStore(String playerId) {
        EntityData.PlayerStore store = null;
        Path storePath = storagePathProvider.getPlayerFilePath(playerId);
        if (Files.isRegularFile(storePath)) {
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(storePath))) {
                store = EntityData.PlayerStore.parseFrom(inputStream);
            } catch (IOException e) {
                logger.error("Failed to load player data for {}", playerId, e);
            }
        }
        if (store != null) {
            TIntSet validRefs = null;
            StoreMetadata table = storeMetadata.get(new PlayerStoreId(playerId));
            if (table != null) {
                validRefs = table.getExternalReferences();
            }
            return new PlayerStoreInternal(playerId, store, validRefs, this, entityManager);
        }
        return new PlayerStoreInternal(playerId, this, entityManager);
    }

    private void createChunkStoresForSave(SaveTransactionBuilder saveTransactionBuilder, ChunkProvider chunkProvider) {
        unloadedAndSavingChunkMap.clear();
        /**
         * New entries might be added concurrently. By using putAll + clear to transfer entries we might loose new
         * ones added in between putAll and clear. Bz iterating we can make sure that all entires removed
         * from unloadedAndUnsavedChunkMap get added to unloadedAndSavingChunkMap.
         */
        Iterator<Map.Entry<Vector3i, CompressedChunkBuilder>> unsavedEntryIterator = unloadedAndUnsavedChunkMap.entrySet().iterator();
        while(unsavedEntryIterator.hasNext()) {
            Map.Entry<Vector3i, CompressedChunkBuilder> entry = unsavedEntryIterator.next();
            unloadedAndSavingChunkMap.put(entry.getKey(), entry.getValue());
            unsavedEntryIterator.remove();
        }


        for (Chunk chunk : chunkProvider.getAllChunks()) {
            // If there is a newer undisposed version of the chunk,we don't need to save the disposed version:
            unloadedAndSavingChunkMap.remove(chunk.getPosition());
            saveTransactionBuilder.addCompressedChunkBuilder(chunk.getPosition(), createCompressedChunkBuilder(chunk, true));
        }

        for (Map.Entry<Vector3i, CompressedChunkBuilder> entry: unloadedAndSavingChunkMap.entrySet()) {
            saveTransactionBuilder.addCompressedChunkBuilder(entry.getKey(), entry.getValue());
        }
    }

    /**
     *This method should only be called by the main thread.
     *
     * @param viaSnapshot specifies if a snapshot of the chunk data will be created. Only the main thread may set this
     *                    to true for saving. If it is false the chunk data will be saved directly. The value false
     *                    requires the chunk to be no longer in use like after unloading.
     */
    private CompressedChunkBuilder createCompressedChunkBuilder(Chunk chunk, boolean viaSnapshot) {
        EntityStorer storer = new EntityStorer(entityManager);
        for (EntityRef entityRef : entityManager.getEntitiesOfChunk(chunk)) {
            if (entityRef.isPersistent()) {
                storer.store(entityRef, false);
            }
        }
        EntityData.EntityStore entityStore = storer.finaliseStore();
        TIntSet externalRefs = storer.getExternalReferences();

        Vector3i chunkPosition = chunk.getPosition();

        ChunkImpl chunkImpl = (ChunkImpl) chunk;
        if (viaSnapshot) {
            chunkImpl.createSnapshot();
        }
        CompressedChunkBuilder compressedChunkBuilder = new CompressedChunkBuilder(entityStore, chunkImpl, viaSnapshot);

        if (externalRefs.size() > 0) {
            StoreMetadata metadata = new StoreMetadata(new ChunkStoreId(chunkPosition), externalRefs);
            indexStoreMetadata(metadata);
        }

        return compressedChunkBuilder;


    }



    @Override
    public void requestSaving() {
        this.saveRequested = true;
    }

    @Override
    public void waitForCompletionOfPreviousSaveAndStartSaving() {
        waitForCompletionOfPreviousSave();
        startSaving();
    }

    private void waitForCompletionOfPreviousSave() {
        if (saveTransaction != null && saveTransaction.getResult() == null) {
            saveThreadManager.shutdown(new ShutdownTask(), true);
            saveThreadManager.restart();
        }
        checkSaveTransactionAndClearUpIfItIsDone();
    }

    private SaveTransaction createSaveTransaction() {
        SaveTransactionBuilder saveTransactionBuilder = new SaveTransactionBuilder(storeChunksInZips,
                storagePathProvider);

        ChunkProvider chunkProvider = CoreRegistry.get(ChunkProvider.class);
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

        chunkProvider.shutdown();
        createChunkStoresForSave(saveTransactionBuilder, chunkProvider);
        createPlayerStoresForSave(saveTransactionBuilder, networkSystem);
        createGlobalStoreForSave(saveTransactionBuilder);
        createGameManifest(saveTransactionBuilder);
        chunkProvider.restart();
        // TODO check if more threads need to be stopped:  e.g. network and common engine threads


        return saveTransactionBuilder.build();
    }



    private void createPlayerStoresForSave(SaveTransactionBuilder saveTransactionBuilder, NetworkSystem networkSystem) {
        for (Client client : networkSystem.getPlayers()) {
            addPlayer(saveTransactionBuilder, client);
        }
    }

    private void addPlayer(SaveTransactionBuilder saveTransactionBuilder, Client client) {
        String playerId = client.getId();
        PlayerStore playerStore = new PlayerStoreInternal(playerId, this, entityManager);
        EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
        if (character.exists()) {
            playerStore.setCharacter(character);
        }

        boolean hasCharacter = character.exists();
        LocationComponent location = character.getComponent(LocationComponent.class);
        Vector3f relevanceLocation;
        if (location != null) {
            relevanceLocation = location.getWorldPosition();
        } else {
            relevanceLocation = new Vector3f();
        }

        EntityData.PlayerStore.Builder playerEntityStore = EntityData.PlayerStore.newBuilder();
        playerEntityStore.setCharacterPosX(relevanceLocation.x);
        playerEntityStore.setCharacterPosY(relevanceLocation.y);
        playerEntityStore.setCharacterPosZ(relevanceLocation.z);
        playerEntityStore.setHasCharacter(hasCharacter);
        EntityStorer storer = new EntityStorer(entityManager);
        storer.store(character, PlayerStoreInternal.CHARACTER, false);
        playerEntityStore.setStore(storer.finaliseStore());


        TIntSet externalReference = storer.getExternalReferences();
        if (externalReference.size() > 0) {
            StoreMetadata metadata = new StoreMetadata(new PlayerStoreId(playerId), externalReference);
             indexStoreMetadata(metadata);
        }
        saveTransactionBuilder.addPlayerStore(playerId, playerEntityStore.build());
    }

    @Override
    public void onChunkUnload(Chunk chunk) {
        unloadedAndUnsavedChunkMap.put(chunk.getPosition(), createCompressedChunkBuilder(chunk, false));
    }


    private byte[] loadCompressedChunk(Vector3i chunkPos) {
        CompressedChunkBuilder disposedUnsavedChunk = unloadedAndUnsavedChunkMap.get(chunkPos);
        if (disposedUnsavedChunk != null) {
            return disposedUnsavedChunk.buildEncodedChunk();
        }
        CompressedChunkBuilder disposedSavingChunk = unloadedAndSavingChunkMap.get(chunkPos);
        if (disposedSavingChunk != null) {
            return disposedSavingChunk.buildEncodedChunk();
        }

        if (storeChunksInZips) {
            return loadChunkZip(chunkPos);
        } else {
            Path chunkPath = storagePathProvider.getChunkPath(chunkPos);
            if (Files.isRegularFile(chunkPath)) {
                try {
                    return Files.readAllBytes(chunkPath);
                } catch (IOException e) {
                    logger.error("Failed to load chunk {}", chunkPos, e);
                }
            }
        }
        return null;
    }

    @Override
    public ChunkStore loadChunkStore(Vector3i chunkPos) {
        byte[] chunkData = loadCompressedChunk(chunkPos);
        ChunkStore store = null;
        if (chunkData != null) {
            TIntSet validRefs = null;
            StoreMetadata table = storeMetadata.get(new ChunkStoreId(chunkPos));
            if (table != null) {
                validRefs = table.getExternalReferences();
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(chunkData);
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                EntityData.ChunkStore storeData = EntityData.ChunkStore.parseFrom(gzipIn);
                store = new ChunkStoreInternal(storeData, validRefs, this, entityManager);
            } catch (IOException e) {
                logger.error("Failed to read existing saved chunk {}", chunkPos);
            }
        }
        return store;
    }

    private byte[] loadChunkZip(Vector3i chunkPos) {
        byte[] chunkData = null;
        Vector3i chunkZipPos = storagePathProvider.getChunkZipPosition(chunkPos);
        Path chunkPath = storagePathProvider.getChunkZipPath(chunkZipPos);
        if (Files.isRegularFile(chunkPath)) {
            try (FileSystem chunkZip = FileSystems.newFileSystem(chunkPath, null)) {
                Path targetChunk = chunkZip.getPath(storagePathProvider.getChunkFilename(chunkPos));
                if (Files.isRegularFile(targetChunk)) {
                    chunkData = Files.readAllBytes(targetChunk);
                }
            } catch (IOException e) {
                logger.error("Failed to load chunk zip {}", chunkPath, e);
            }
        }
        return chunkData;
    }


    private void indexStoreMetadata(StoreMetadata metadata) {
        storeMetadata.put(metadata.getId(), metadata);
        TIntIterator iterator = metadata.getExternalReferences().iterator();
        while (iterator.hasNext()) {
            int refId = iterator.next();
            List<StoreMetadata> tables = externalRefHolderLookup.get(refId);
            if (tables == null) {
                tables = Lists.newArrayList();
                externalRefHolderLookup.put(refId, tables);
            }
            tables.add(metadata);
        }
    }

    @Override
    public void onEntityDestroyed(int entityId) {
        List<StoreMetadata> tables = externalRefHolderLookup.remove(entityId);
        if (tables != null) {
            for (StoreMetadata table : tables) {
                table.getExternalReferences().remove(entityId);
                if (table.getExternalReferences().isEmpty()) {
                    storeMetadata.remove(table.getId());
                }
            }
        }
    }

    private void createGameManifest(SaveTransactionBuilder saveTransactionBuilder) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BiomeManager biomeManager = CoreRegistry.get(BiomeManager.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        Game game = CoreRegistry.get(Game.class);

        GameManifest gameManifest = new GameManifest(game.getName(), game.getSeed(), game.getTime().getGameTimeInMs());
        for (Module module : CoreRegistry.get(ModuleManager.class).getEnvironment()) {
            gameManifest.addModule(module.getId(), module.getVersion());
        }

        List<String> registeredBlockFamilies = Lists.newArrayList();
        for (BlockFamily family : blockManager.listRegisteredBlockFamilies()) {
            registeredBlockFamilies.add(family.getURI().toString());
        }
        gameManifest.setRegisteredBlockFamilies(registeredBlockFamilies);
        gameManifest.setBlockIdMap(blockManager.getBlockIdMap());
        List<Biome> biomes = biomeManager.getBiomes();
        Map<String, Short> biomeIdMap = new HashMap<>(biomes.size());
        for (Biome biome : biomes) {
            short shortId = biomeManager.getBiomeShortId(biome);
            String id = biomeManager.getBiomeId(biome);
            biomeIdMap.put(id, shortId);
        }
        gameManifest.setBiomeIdMap(biomeIdMap);
        gameManifest.addWorld(worldProvider.getWorldInfo());
        saveTransactionBuilder.setGameManifest(gameManifest);
    }

    @Override
    public void update() {
        if (!isRunModeAllowSaving()) {
            return;
        }
        if (isSaving()) {
            return;
        }

        checkSaveTransactionAndClearUpIfItIsDone();
        if (saveRequested || isSavingNecessary()) {
            startSaving();
        }
    }

    private boolean isRunModeAllowSaving() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        return networkSystem.getMode().isAuthority();
    }

    private void startSaving() {
        logger.info("Saving - Creating game snapshot");
        PerformanceMonitor.startActivity("Auto Saving");
        ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
        for (ComponentSystem sys : componentSystemManager.iterateAll()) {
            sys.preSave();
        }

        saveRequested = false;
        saveTransaction = createSaveTransaction();
        saveThreadManager.offer(saveTransaction);

        for (ComponentSystem sys : componentSystemManager.iterateAll()) {
            sys.postSave();
        }
        scheduleNextAutoSave();
        PerformanceMonitor.endActivity();
        logger.info("Saving - Snapshot created: Writing phase starts");
    }


    private boolean isSavingNecessary() {
        if (unloadedAndUnsavedChunkMap.size() >= config.getSystem().getMaxUnloadedChunksTillSave()) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        if (nextAutoSave == null) {
            scheduleNextAutoSave();
            return false;
        }
        if (currentTime >= nextAutoSave) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * For tests only
     * 
     */
    public void setStoreChunksInZips(boolean storeChunksInZips) {
        this.storeChunksInZips = storeChunksInZips;
    }

    private void scheduleNextAutoSave() {
        long msBetweenAutoSave = config.getSystem().getMaxSecondsBetweenSaves() * 1000;
        nextAutoSave = System.currentTimeMillis() + msBetweenAutoSave;
    }

    public boolean isSaving() {
        return saveTransaction != null && saveTransaction.getResult() == null;
    }

}
