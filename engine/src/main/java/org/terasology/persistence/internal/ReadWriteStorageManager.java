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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityDestroySubscriber;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.math.geom.Vector3f;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.FilesUtil;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Immortius
 * @author Florian <florian@fkoeberle.de>
 */
public final class ReadWriteStorageManager extends AbstractStorageManager implements EntityDestroySubscriber {
    private static final Logger logger = LoggerFactory.getLogger(ReadWriteStorageManager.class);

    private final TaskMaster<Task> saveThreadManager;

    private final SaveTransactionHelper saveTransactionHelper;

    /**
     * This lock should be hold during read and write operation in the world directory. Currently it is being hold
     * during reads of chunks or players as they are crruently the only data that needs to be loaded during the game.
     *
     * This lock ensures that reading threads can properly finish reading even when for example the ZIP file with the
     * chunks got replaced with a newer version. Chunks that are getting saved get loaded from memory. It can however
     * still be that a thread tries to load another chunk from the same ZIP file that contains the chunk that needs to
     * be saved. Thus it can potentially happen that 2 threads want to read/write the same ZIP file with chunks.
     */
    private final ReadWriteLock worldDirectoryLock = new ReentrantReadWriteLock(true);
    private final Lock worldDirectoryReadLock = worldDirectoryLock.readLock();
    private final Lock worldDirectoryWriteLock = worldDirectoryLock.writeLock();
    private SaveTransaction saveTransaction;
    private Config config;

    /**
     * Time of the next save in the format that {@link System#currentTimeMillis()} returns.
     */
    private Long nextAutoSave;
    private boolean saveRequested;
    private ConcurrentMap<Vector3i, CompressedChunkBuilder> unloadedAndUnsavedChunkMap = Maps.newConcurrentMap();
    private ConcurrentMap<Vector3i, CompressedChunkBuilder> unloadedAndSavingChunkMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, EntityData.PlayerStore> unloadedAndUnsavedPlayerMap = Maps.newConcurrentMap();
    private ConcurrentMap<String, EntityData.PlayerStore> unloadedAndSavingPlayerMap = Maps.newConcurrentMap();

    public ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager) throws IOException {
        this(savePath, environment, entityManager, true);
    }

    public ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager, boolean storeChunksInZips) throws IOException {
        super(savePath, environment, entityManager, storeChunksInZips);

        entityManager.subscribeDestroyListener(this);
        Files.createDirectories(getStoragePathProvider().getStoragePathDirectory());
        this.saveTransactionHelper = new SaveTransactionHelper(getStoragePathProvider());
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

    /**
     *
     * @param unsavedEntities currently loaded persistent entities without owner that have not been saved yet.
     */
    private void addGlobalStoreToSaveTransaction(SaveTransactionBuilder transactionBuilder, Set<EntityRef> unsavedEntities) {
        GlobalStoreSaver globalStoreSaver = new GlobalStoreSaver(getEntityManager(), getPrefabSerializer());
        for (EntityRef entity : unsavedEntities) {
            globalStoreSaver.store(entity);
        }
        EntityData.GlobalStore globalStore = globalStoreSaver.save();
        transactionBuilder.setGlobalStore(globalStore);
    }

    @Override
    public void deactivatePlayer(Client client) {
        EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
        EntityData.PlayerStore playerStore = createPlayerStore(client, character, true);
        unloadedAndUnsavedPlayerMap.put(client.getId(), playerStore);
    }

    @Override
    protected EntityData.PlayerStore loadPlayerStoreData(String playerId) {
        EntityData.PlayerStore  disposedUnsavedPlayer = unloadedAndUnsavedPlayerMap.get(playerId);
        if (disposedUnsavedPlayer != null) {
            return disposedUnsavedPlayer;
        }
        EntityData.PlayerStore  disposedSavingPlayer = unloadedAndSavingPlayerMap.get(playerId);
        if (disposedSavingPlayer != null) {
            return disposedSavingPlayer;
        }
        worldDirectoryReadLock.lock();
        try {
            return super.loadPlayerStoreData(playerId);
        } finally {
            worldDirectoryReadLock.unlock();
        }
    }

    /**
     *
     * @param unsavedEntities currently loaded persistent entities without owner that have not been saved yet.
     *                        This method removes entities it saves.
     */
    private void addChunksToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder, ChunkProvider chunkProvider,
                                            Set<EntityRef> unsavedEntities) {
        unloadedAndSavingChunkMap.clear();
        /**
         * New entries might be added concurrently. By using putAll + clear to transfer entries we might loose new
         * ones added in between putAll and clear. Bz iterating we can make sure that all entires removed
         * from unloadedAndUnsavedChunkMap get added to unloadedAndSavingChunkMap.
         */
        Iterator<Map.Entry<Vector3i, CompressedChunkBuilder>> unsavedEntryIterator = unloadedAndUnsavedChunkMap.entrySet().iterator();
        while (unsavedEntryIterator.hasNext()) {
            Map.Entry<Vector3i, CompressedChunkBuilder> entry = unsavedEntryIterator.next();
            unloadedAndSavingChunkMap.put(entry.getKey(), entry.getValue());
            unsavedEntryIterator.remove();
        }

        Map<Vector3i, Collection<EntityRef>> chunkPosToEntitiesMap = createChunkPosToUnsavedEntitiesMap();

        for (Chunk chunk : chunkProvider.getAllChunks()) {
            if (chunk.isReady()) {
                // If there is a newer undisposed version of the chunk,we don't need to save the disposed version:
                unloadedAndSavingChunkMap.remove(chunk.getPosition());
                Collection<EntityRef> entitiesToStore = chunkPosToEntitiesMap.get(chunk.getPosition());
                if (entitiesToStore == null) {
                    entitiesToStore = Collections.emptySet();
                }
                unsavedEntities.removeAll(entitiesToStore);
                CompressedChunkBuilder compressedChunkBuilder = createCompressedChunkBuilder(chunk,
                        entitiesToStore, false);
                saveTransactionBuilder.addCompressedChunkBuilder(chunk.getPosition(), compressedChunkBuilder);
            }
        }

        for (Map.Entry<Vector3i, CompressedChunkBuilder> entry: unloadedAndSavingChunkMap.entrySet()) {
            saveTransactionBuilder.addCompressedChunkBuilder(entry.getKey(), entry.getValue());
        }
    }

    private Map<Vector3i, Collection<EntityRef>> createChunkPosToUnsavedEntitiesMap() {
        Map<Vector3i, Collection<EntityRef>> chunkPosToEntitiesMap = Maps.newHashMap();
        for (EntityRef entity : getEntityManager().getEntitiesWith(LocationComponent.class)) {
            /*
             * Note: Entities with owners get saved with the owner. Entities that are always relevant don't get stored
             * in chunk as the chunk is not always loaded
             */
            if (entity.isPersistent() && !entity.getOwner().exists() && !entity.hasComponent(ClientComponent.class)
                    && !entity.isAlwaysRelevant()) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                if (locationComponent != null) {
                    Vector3f loc = locationComponent.getWorldPosition();
                    Vector3i chunkPos = TeraMath.calcChunkPos((int) loc.x, (int) loc.y, (int) loc.z);
                    Collection<EntityRef> collection = chunkPosToEntitiesMap.get(chunkPos);
                    if (collection == null) {
                        collection = Lists.newArrayList();
                        chunkPosToEntitiesMap.put(chunkPos, collection);
                    }
                    collection.add(entity);
                }
            }
        }
        return chunkPosToEntitiesMap;
    }

    /**
     *This method should only be called by the main thread.
     *
     * @param entitiesToSave all persistent entities within the given chunk
     * @param deactivate if true the entities of the chunk will be deaktivated and the chunk data will be used directly.
     *                 If deactivate is false then the entities won't be touched and a chunk will be but in
     *                 snapshot mode so that concurrent modifcations (and possibly future unload) is possible.
     */
    private CompressedChunkBuilder createCompressedChunkBuilder(Chunk chunk,
                                                                Collection<EntityRef> entitiesToSave,
                                                                boolean deactivate) {
        EntityStorer storer = new EntityStorer(getEntityManager());
        for (EntityRef entityRef : entitiesToSave) {
            if (entityRef.isPersistent()) {
                storer.store(entityRef, deactivate);
            } else {
                if (deactivate) {
                    entityRef.destroy();
                }
            }
        }
        EntityData.EntityStore entityStore = storer.finaliseStore();

        ChunkImpl chunkImpl = (ChunkImpl) chunk;
        boolean viaSnapshot = !deactivate;
        if (viaSnapshot) {
            chunkImpl.createSnapshot();
        }
        return new CompressedChunkBuilder(entityStore, chunkImpl, viaSnapshot);
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
        SaveTransactionBuilder saveTransactionBuilder = new SaveTransactionBuilder(
                isStoreChunksInZips(), getStoragePathProvider(), worldDirectoryWriteLock);

        /**
         * Currently loaded persistent entities without owner that have not been saved yet.
         */
        Set<EntityRef> unsavedEntities = new HashSet<>();
        for (EntityRef entity: getEntityManager().getAllEntities()) {
            if (entity.isPersistent() && !entity.getOwner().exists()) {
                unsavedEntities.add(entity);
            }
        }
        ChunkProvider chunkProvider = CoreRegistry.get(ChunkProvider.class);
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

        addChunksToSaveTransaction(saveTransactionBuilder, chunkProvider, unsavedEntities);
        addPlayersToSaveTransaction(saveTransactionBuilder, networkSystem, unsavedEntities);
        addGlobalStoreToSaveTransaction(saveTransactionBuilder, unsavedEntities);
        addGameManifestToSaveTransaction(saveTransactionBuilder);

        return saveTransactionBuilder.build();
    }


    /**
     *
     * @param unsavedEntities currently loaded persistent entities without owner that have not been saved yet.
     *                        This method removes entities it saves.
     */
    private void addPlayersToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder, NetworkSystem networkSystem,
                                             Set<EntityRef> unsavedEntities) {
        unloadedAndSavingPlayerMap.clear();
        /**
         * New entries might be added concurrently. By using putAll + clear to transfer entries we might loose new
         * ones added in between putAll and clear. By iterating we can make sure that all entities removed
         * from unloadedAndUnsavedPlayerMap get added to unloadedAndSavingPlayerMap.
         */
        Iterator<Map.Entry<String, EntityData.PlayerStore>> unsavedEntryIterator = unloadedAndUnsavedPlayerMap.entrySet().iterator();
        while (unsavedEntryIterator.hasNext()) {
            Map.Entry<String, EntityData.PlayerStore> entry = unsavedEntryIterator.next();
            unloadedAndSavingPlayerMap.put(entry.getKey(), entry.getValue());
            unsavedEntryIterator.remove();
        }

        for (Client client : networkSystem.getPlayers()) {
            // If there is a newer undisposed version of the player,we don't need to save the disposed version:
            unloadedAndSavingPlayerMap.remove(client.getId());
            EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
            unsavedEntities.remove(character);
            saveTransactionBuilder.addPlayerStore(client.getId(), createPlayerStore(client, character, false));
        }

        for (Map.Entry<String, EntityData.PlayerStore> entry: unloadedAndSavingPlayerMap.entrySet()) {
            saveTransactionBuilder.addPlayerStore(entry.getKey(), entry.getValue());
        }
    }

    private EntityData.PlayerStore createPlayerStore(Client client, EntityRef character, boolean deactivate) {
        String playerId = client.getId();
        PlayerStore playerStore = new PlayerStoreInternal(playerId, this, getEntityManager());
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
        EntityStorer storer = new EntityStorer(getEntityManager());
        storer.store(character, PlayerStoreInternal.CHARACTER, deactivate);
        playerEntityStore.setStore(storer.finaliseStore());

        return playerEntityStore.build();
    }


    private Collection<EntityRef> getEntitiesOfChunk(Chunk chunk) {
        List<EntityRef> entitiesToStore = Lists.newArrayList();

        AABB aabb = chunk.getAABB();
        for (EntityRef entity : getEntityManager().getEntitiesWith(LocationComponent.class)) {
            if (!entity.getOwner().exists() && !entity.isAlwaysRelevant() && !entity.hasComponent(ClientComponent.class)) {
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc != null) {
                    if (aabb.contains(loc.getWorldPosition())) {
                        entitiesToStore.add(entity);
                    }
                }
            }
        }
        return entitiesToStore;
    }

    @Override
    public void deactivateChunk(Chunk chunk) {
        Collection<EntityRef> entitiesOfChunk = getEntitiesOfChunk(chunk);
        unloadedAndUnsavedChunkMap.put(chunk.getPosition(), createCompressedChunkBuilder(chunk, entitiesOfChunk, true));
    }

    @Override
    protected byte[] loadCompressedChunk(Vector3i chunkPos) {
        CompressedChunkBuilder disposedUnsavedChunk = unloadedAndUnsavedChunkMap.get(chunkPos);
        if (disposedUnsavedChunk != null) {
            return disposedUnsavedChunk.buildEncodedChunk();
        }
        CompressedChunkBuilder disposedSavingChunk = unloadedAndSavingChunkMap.get(chunkPos);
        if (disposedSavingChunk != null) {
            return disposedSavingChunk.buildEncodedChunk();
        }

        worldDirectoryReadLock.lock();
        try {
            return super.loadCompressedChunk(chunkPos);
        } finally {
            worldDirectoryReadLock.unlock();
        }
    }

    @Override
    public void onEntityDestroyed(long entityId) {

    }

    private void addGameManifestToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder) {
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
        ChunkProvider chunkProvider = CoreRegistry.get(ChunkProvider.class);
        int unloadedChunkCount = unloadedAndUnsavedChunkMap.size();
        int loadedChunkCount = chunkProvider.getAllChunks().size();
        double totalChunkCount = unloadedChunkCount + loadedChunkCount;
        double percentageUnloaded = 100.0 * unloadedChunkCount / totalChunkCount;
        if (percentageUnloaded >= config.getSystem().getMaxUnloadedChunksPercentageTillSave()) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        if (nextAutoSave == null) {
            scheduleNextAutoSave();
            return false;
        }
        return currentTime >= nextAutoSave;
    }

    private void scheduleNextAutoSave() {
        long msBetweenAutoSave = config.getSystem().getMaxSecondsBetweenSaves() * 1000;
        nextAutoSave = System.currentTimeMillis() + msBetweenAutoSave;
    }

    @Override
    public boolean isSaving() {
        return saveTransaction != null && saveTransaction.getResult() == null;
    }

    @Override
    public void checkAndRepairSaveIfNecessary() throws IOException {
        saveTransactionHelper.cleanupSaveTransactionDirectory();
        if (Files.exists(getStoragePathProvider().getUnmergedChangesPath())) {
            saveTransactionHelper.mergeChanges();
        }
    }


    @Override
    public void deleteWorld() {
        waitForCompletionOfPreviousSave();
        unloadedAndUnsavedChunkMap.clear();
        unloadedAndSavingChunkMap.clear();
        unloadedAndUnsavedPlayerMap.clear();
        unloadedAndSavingPlayerMap.clear();

        try {
            FilesUtil.recursiveDelete(getStoragePathProvider().getWorldPath());
        } catch (IOException e) {
            logger.error("Failed to purge chunks", e);
        }
    }

}
