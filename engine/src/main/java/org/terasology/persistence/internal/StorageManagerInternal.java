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
import org.terasology.math.AABB;
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
import java.util.zip.GZIPInputStream;

/**
 * @author Immortius
 */
public final class StorageManagerInternal implements StorageManager, EntityDestroySubscriber {

    private static final int BACKGROUND_THREADS = 4;

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
        checkSaveTransactionAndClearVariableIfDone();
    }

    private void checkSaveTransactionAndClearVariableIfDone() {
        if (saveTransaction != null) {
            SaveTransactionResult result = saveTransaction.getResult();
            if (result != null) {
                Throwable t = saveTransaction.getResult().getCatchedThrowable();
                if (t != null) {
                    throw new RuntimeException("Saving failed", t);
                }
                saveTransaction = null;
            }
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
        for (Chunk chunk : chunkProvider.getAllChunks()) {
            createChunkStoreForSave(chunk, saveTransactionBuilder);
        }
    }

    private void createChunkStoreForSave(Chunk chunk, SaveTransactionBuilder saveTransactionBuilder) {
        List<EntityRef> entitiesToStore = getEntitiesOfChunk(chunk);

        EntityStorer storer = new EntityStorer(entityManager);
        for (EntityRef entityRef : entitiesToStore) {
            storer.store(entityRef, false);
        }
        EntityData.EntityStore entityStore = storer.finaliseStore();
        TIntSet externalRefs = storer.getExternalReferences();

        Vector3i chunkPosition = chunk.getPosition();

        ChunkImpl chunkImpl = (ChunkImpl) chunk;
        chunkImpl.createSnapshot();
        CompressedChunkBuilder compressedChunkBuilder = new CompressedChunkBuilder(entityStore, chunkImpl);
        saveTransactionBuilder.addCompressedChunkBuilder(chunkPosition, compressedChunkBuilder);

        if (externalRefs.size() > 0) {
            StoreMetadata metadata = new StoreMetadata(new ChunkStoreId(chunkPosition), externalRefs);
            indexStoreMetadata(metadata);
        }
    }

    private List<EntityRef> getEntitiesOfChunk(Chunk chunk) {
        List<EntityRef> entitiesToStore = Lists.newArrayList();

        AABB aabb = chunk.getAABB();
        for (EntityRef entity : entityManager.getEntitiesWith(LocationComponent.class)) {
            if (!entity.getOwner().exists() && !entity.isAlwaysRelevant() && !entity.hasComponent(ClientComponent.class)) {
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc != null) {
                    if (aabb.contains(loc.getWorldPosition())) {
                        if (entity.isPersistent()) {
                            entitiesToStore.add(entity);
                        } else {
                            entity.destroy();
                        }
                    }
                }
            }
        }
        return entitiesToStore;
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
        checkSaveTransactionAndClearVariableIfDone();
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
    public ChunkStore loadChunkStore(Vector3i chunkPos) {
        byte[] chunkData = null;
        if (storeChunksInZips) {
            chunkData = loadChunkZip(chunkPos);
        } else {
            Path chunkPath = storagePathProvider.getChunkPath(chunkPos);
            if (Files.isRegularFile(chunkPath)) {
                try {
                    chunkData = Files.readAllBytes(chunkPath);
                } catch (IOException e) {
                    logger.error("Failed to load chunk {}", chunkPos, e);
                }
            }
        }
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

        checkSaveTransactionAndClearVariableIfDone();
        if (saveRequested || isAutoSavingWantedAndPossible()) {
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


    private boolean isAutoSavingWantedAndPossible() {
        if (!config.getSystem().isAutoSaveEnabled()) {
            return false;
        }

        if (saveRequested) {
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


    private void scheduleNextAutoSave() {
        long msBetweenAutoSave = config.getSystem().getSecondsBetweenAutoSave() * 1000;
        nextAutoSave = System.currentTimeMillis() + msBetweenAutoSave;
    }

    public boolean isSaving() {
        return saveTransaction != null && saveTransaction.getResult() == null;
    }
}
