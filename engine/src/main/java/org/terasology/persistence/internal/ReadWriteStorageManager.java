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
import org.terasology.engine.Time;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.entitySystem.entity.internal.EntityDestroySubscriber;
import org.terasology.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.systems.ComponentSystem;
import org.terasology.game.Game;
import org.terasology.game.GameManifest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.network.Client;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.persistence.typeHandling.TypeSerializationLibrary;
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
import org.terasology.world.chunks.ManagedChunk;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 */
public final class ReadWriteStorageManager extends AbstractStorageManager implements EntityDestroySubscriber, EntityChangeSubscriber, DelayedEntityRefFactory {
    private static final Logger logger = LoggerFactory.getLogger(ReadWriteStorageManager.class);

    private final TaskMaster<Task> saveThreadManager;
    private final SaveTransactionHelper saveTransactionHelper;

    /**
     * This lock should be hold during read and write operation in the world directory. Currently it is being hold
     * during reads of chunks or players as they are crruently the only data that needs to be loaded during the game.
     * <br><br>
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


    private EngineEntityManager privateEntityManager;
    private EntitySetDeltaRecorder entitySetDeltaRecorder;
    /**
     * A component library that provides a copy() method that replaces {@link EntityRef}s which {@link EntityRef}s
     * that will use the privateEntityManager.
     */
    private ComponentLibrary entityRefReplacingComponentLibrary;

    public ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                   BlockManager blockManager, BiomeManager biomeManager) throws IOException {
        this(savePath, environment, entityManager, blockManager, biomeManager, true);
    }

    public ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                   BlockManager blockManager, BiomeManager biomeManager, boolean storeChunksInZips) throws IOException {
        super(savePath, environment, entityManager, blockManager, biomeManager, storeChunksInZips);

        entityManager.subscribeForDestruction(this);
        entityManager.subscribeForChanges(this);
        // TODO Ensure that the component library and the type serializer library are thread save (e.g. immutable)
        this.privateEntityManager = createPrivateEntityManager(entityManager.getComponentLibrary());
        Files.createDirectories(getStoragePathProvider().getStoragePathDirectory());
        this.saveTransactionHelper = new SaveTransactionHelper(getStoragePathProvider());
        this.saveThreadManager = TaskMaster.createFIFOTaskMaster("Saving", 1);
        this.config = CoreRegistry.get(Config.class);
        this.entityRefReplacingComponentLibrary = privateEntityManager.getComponentLibrary()
                .createCopyUsingCopyStrategy(EntityRef.class, new DelayedEntityRefCopyStrategy(this));
        this.entitySetDeltaRecorder = new EntitySetDeltaRecorder(this.entityRefReplacingComponentLibrary);

    }

    private static EngineEntityManager createPrivateEntityManager(ComponentLibrary componentLibrary) {
        PojoEntityManager pojoEntityManager = new PojoEntityManager();
        pojoEntityManager.setComponentLibrary(componentLibrary);
        pojoEntityManager.setTypeSerializerLibrary(CoreRegistry.get(TypeSerializationLibrary.class));
        return pojoEntityManager;
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


    private void addGlobalStoreBuilderToSaveTransaction(SaveTransactionBuilder transactionBuilder) {
        GlobalStoreBuilder globalStoreBuilder = new GlobalStoreBuilder(getEntityManager(), getPrefabSerializer());
        transactionBuilder.setGlobalStoreBuilder(globalStoreBuilder);
    }

    @Override
    public void deactivatePlayer(Client client) {
        EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
        PlayerStoreBuilder playerStoreBuilder = createPlayerStore(client, character);
        EntityData.PlayerStore playerStore = playerStoreBuilder.build(getEntityManager());
        deactivateOrDestroyEntityRecursive(character);
        unloadedAndUnsavedPlayerMap.put(client.getId(), playerStore);
    }

    @Override
    protected EntityData.PlayerStore loadPlayerStoreData(String playerId) {
        EntityData.PlayerStore disposedUnsavedPlayer = unloadedAndUnsavedPlayerMap.get(playerId);
        if (disposedUnsavedPlayer != null) {
            return disposedUnsavedPlayer;
        }
        EntityData.PlayerStore disposedSavingPlayer = unloadedAndSavingPlayerMap.get(playerId);
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

    private void addChunksToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder,
                                            ChunkProvider chunkProvider) {
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

        chunkProvider.getAllChunks().stream().filter(ManagedChunk::isReady).forEach(chunk -> {
            // If there is a newer undisposed version of the chunk,we don't need to save the disposed version:
            unloadedAndSavingChunkMap.remove(chunk.getPosition());
            ChunkImpl chunkImpl = (ChunkImpl) chunk;  // this storage manager can only work with ChunkImpls
            saveTransactionBuilder.addLoadedChunk(chunk.getPosition(), chunkImpl);
        });

        for (Map.Entry<Vector3i, CompressedChunkBuilder> entry : unloadedAndSavingChunkMap.entrySet()) {
            saveTransactionBuilder.addUnloadedChunk(entry.getKey(), entry.getValue());
        }
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
        SaveTransactionBuilder saveTransactionBuilder = new SaveTransactionBuilder(privateEntityManager,
                entitySetDeltaRecorder, isStoreChunksInZips(), getStoragePathProvider(), worldDirectoryWriteLock);

        ChunkProvider chunkProvider = CoreRegistry.get(ChunkProvider.class);
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);

        addChunksToSaveTransaction(saveTransactionBuilder, chunkProvider);
        addPlayersToSaveTransaction(saveTransactionBuilder, networkSystem);
        addGlobalStoreBuilderToSaveTransaction(saveTransactionBuilder);
        addGameManifestToSaveTransaction(saveTransactionBuilder);

        return saveTransactionBuilder.build();
    }


    private void addPlayersToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder,
                                             NetworkSystem networkSystem) {
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
            saveTransactionBuilder.addLoadedPlayer(client.getId(), createPlayerStore(client, character));
        }

        for (Map.Entry<String, EntityData.PlayerStore> entry : unloadedAndSavingPlayerMap.entrySet()) {
            saveTransactionBuilder.addUnloadedPlayer(entry.getKey(), entry.getValue());
        }
    }

    private PlayerStoreBuilder createPlayerStore(Client client, EntityRef character) {
        LocationComponent location = character.getComponent(LocationComponent.class);
        Vector3f relevanceLocation;
        if (location != null) {
            relevanceLocation = location.getWorldPosition();
        } else {
            relevanceLocation = new Vector3f();
        }
        Long characterId;
        if (character.exists()) {
            characterId = character.getId();
        } else {
            characterId = null;
        }
        return new PlayerStoreBuilder(characterId, relevanceLocation);
    }

    @Override
    public void deactivateChunk(Chunk chunk) {
        Collection<EntityRef> entitiesOfChunk = getEntitiesOfChunk(chunk);
        ChunkImpl chunkImpl = (ChunkImpl) chunk; // storage manager only works with ChunkImpl
        unloadedAndUnsavedChunkMap.put(chunk.getPosition(), new CompressedChunkBuilder(getEntityManager(), chunkImpl,
                entitiesOfChunk, true));

        entitiesOfChunk.forEach(this::deactivateOrDestroyEntityRecursive);
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
    public void onEntityDestroyed(EntityRef entity) {
        entitySetDeltaRecorder.onEntityDestroyed(entity);
    }

    private void addGameManifestToSaveTransaction(SaveTransactionBuilder saveTransactionBuilder) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BiomeManager biomeManager = CoreRegistry.get(BiomeManager.class);
        WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
        Time time = CoreRegistry.get(Time.class);
        Game game = CoreRegistry.get(Game.class);

        GameManifest gameManifest = new GameManifest(game.getName(), game.getSeed(), time.getGameTimeInMs());
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
        entitySetDeltaRecorder = new EntitySetDeltaRecorder(this.entityRefReplacingComponentLibrary);
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

    @Override
    public void onEntityComponentAdded(EntityRef entity, Class<? extends Component> component) {
        entitySetDeltaRecorder.onEntityComponentAdded(entity, component);
    }

    @Override
    public void onEntityComponentChange(EntityRef entity, Class<? extends Component> component) {
        entitySetDeltaRecorder.onEntityComponentChange(entity, component);
    }

    @Override
    public void onEntityComponentRemoved(EntityRef entity, Class<? extends Component> component) {
        entitySetDeltaRecorder.onEntityComponentRemoved(entity, component);
    }

    @Override
    public void onReactivation(EntityRef entity, Collection<Component> components) {
        entitySetDeltaRecorder.onReactivation(entity, components);

    }

    @Override
    public void onBeforeDeactivation(EntityRef entity, Collection<Component> components) {
        entitySetDeltaRecorder.onBeforeDeactivation(entity, components);

    }

    @Override
    public DelayedEntityRef createDelayedEntityRef(long id) {
        DelayedEntityRef delayedEntityRef = new DelayedEntityRef(id);
        entitySetDeltaRecorder.registerDelayedEntityRef(delayedEntityRef);
        return delayedEntityRef;
    }
}
