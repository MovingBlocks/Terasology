// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector3f;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.config.UniverseConfig;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameScheduler;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.Time;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.EntityChangeSubscriber;
import org.terasology.engine.entitySystem.entity.internal.EntityDestroySubscriber;
import org.terasology.engine.entitySystem.entity.internal.PojoEntityManager;
import org.terasology.engine.entitySystem.metadata.ComponentLibrary;
import org.terasology.engine.entitySystem.systems.ComponentSystem;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.rendering.opengl.ScreenGrabber;
import org.terasology.engine.utilities.FilesUtil;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.internal.ChunkImpl;
import org.terasology.engine.world.generator.WorldConfigurator;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.gestalt.module.Module;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.persistence.typeHandling.TypeHandlerLibrary;
import org.terasology.protobuf.EntityData;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class ReadWriteStorageManager extends AbstractStorageManager
        implements EntityDestroySubscriber, EntityChangeSubscriber, DelayedEntityRefFactory {
    private static final Logger logger = LoggerFactory.getLogger(ReadWriteStorageManager.class);

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
    private Mono<Object> saveTransaction;
    private final Config config;
    private final SystemConfig systemConfig;

    /**
     * Time of the next save in the format that {@link System#currentTimeMillis()} returns.
     */
    private Long nextAutoSave;
    private boolean saveRequested;
    private final ConcurrentMap<Vector3ic, CompressedChunkBuilder> unloadedAndUnsavedChunkMap = Maps.newConcurrentMap();
    private final ConcurrentMap<Vector3ic, CompressedChunkBuilder> unloadedAndSavingChunkMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, EntityData.PlayerStore> unloadedAndUnsavedPlayerMap = Maps.newConcurrentMap();
    private final ConcurrentMap<String, EntityData.PlayerStore> unloadedAndSavingPlayerMap = Maps.newConcurrentMap();


    private final EngineEntityManager privateEntityManager;
    private EntitySetDeltaRecorder entitySetDeltaRecorder;
    private final RecordAndReplaySerializer recordAndReplaySerializer;
    private final RecordAndReplayUtils recordAndReplayUtils;
    private final RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    /**
     * A component library that provides a copy() method that replaces {@link EntityRef}s which {@link EntityRef}s
     * that will use the privateEntityManager.
     */
    private final ComponentLibrary entityRefReplacingComponentLibrary;

    public ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager, BlockManager blockManager,
                                   ExtraBlockDataManager extraDataManager, RecordAndReplaySerializer recordAndReplaySerializer,
                                   RecordAndReplayUtils recordAndReplayUtils, RecordAndReplayCurrentStatus recordAndReplayCurrentStatus)
            throws IOException {
        this(savePath, environment, entityManager, blockManager, extraDataManager,
            true, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);
    }

    ReadWriteStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                   BlockManager blockManager, ExtraBlockDataManager extraDataManager, boolean storeChunksInZips,
                                   RecordAndReplaySerializer recordAndReplaySerializer, RecordAndReplayUtils recordAndReplayUtils,
                            RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) throws IOException {
        super(savePath, environment, entityManager, blockManager, extraDataManager, storeChunksInZips);

        entityManager.subscribeForDestruction(this);
        entityManager.subscribeForChanges(this);
        // TODO Ensure that the component library and the type serializer library are thread save (e.g. immutable)
        this.privateEntityManager = createPrivateEntityManager(entityManager.getComponentLibrary());
        Files.createDirectories(getStoragePathProvider().getStoragePathDirectory());
        this.saveTransactionHelper = new SaveTransactionHelper(getStoragePathProvider());
        this.config = CoreRegistry.get(Config.class);
        this.systemConfig = CoreRegistry.get((SystemConfig.class));
        this.entityRefReplacingComponentLibrary = privateEntityManager.getComponentLibrary()
                .createCopyUsingCopyStrategy(EntityRef.class, new DelayedEntityRefCopyStrategy(this));
        this.entitySetDeltaRecorder = new EntitySetDeltaRecorder(this.entityRefReplacingComponentLibrary);
        this.recordAndReplaySerializer = recordAndReplaySerializer;
        this.recordAndReplayUtils = recordAndReplayUtils;
        this.recordAndReplayCurrentStatus = recordAndReplayCurrentStatus;

    }

    private static EngineEntityManager createPrivateEntityManager(ComponentLibrary componentLibrary) {
        PojoEntityManager pojoEntityManager = new PojoEntityManager();
        pojoEntityManager.setComponentLibrary(componentLibrary);
        pojoEntityManager.setTypeSerializerLibrary(CoreRegistry.get(TypeHandlerLibrary.class));
        return pojoEntityManager;
    }

    @Override
    public void finishSavingAndShutdown() {
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.RECORDING) {
            recordAndReplayUtils.setShutdownRequested(true);
        }
        if (saveTransaction != null) {
            saveTransaction.block();
        }
    }

    private void addGlobalStoreBuilderToSaveTransaction(SaveTransactionBuilder transactionBuilder) {
        GlobalStoreBuilder globalStoreBuilder = new GlobalStoreBuilder(getEntityManager(), getPrefabSerializer());
        transactionBuilder.setGlobalStoreBuilder(globalStoreBuilder);
    }

    @Override
    public void deactivatePlayer(Client client) {
        EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
        PlayerStoreBuilder playerStoreBuilder = createPlayerStore(character);
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
        /*
         * New entries might be added concurrently. By using putAll + clear to transfer entries we might loose new
         * ones added in between putAll and clear. By iterating we can make sure that all entries removed
         * from unloadedAndUnsavedChunkMap get added to unloadedAndSavingChunkMap.
         */
        Iterator<Map.Entry<Vector3ic, CompressedChunkBuilder>> unsavedEntryIterator = unloadedAndUnsavedChunkMap.entrySet().iterator();
        while (unsavedEntryIterator.hasNext()) {
            Map.Entry<Vector3ic, CompressedChunkBuilder> entry = unsavedEntryIterator.next();
            unloadedAndSavingChunkMap.put(entry.getKey(), entry.getValue());
            unsavedEntryIterator.remove();
        }

        chunkProvider.getAllChunks().stream().filter(Chunk::isReady).forEach(chunk -> {
            // If there is a newer undisposed version of the chunk,we don't need to save the disposed version:
            unloadedAndSavingChunkMap.remove(chunk.getPosition());
            ChunkImpl chunkImpl = (ChunkImpl) chunk;  // this storage manager can only work with ChunkImpls
            saveTransactionBuilder.addLoadedChunk(chunk.getPosition(), chunkImpl);
        });

        for (Map.Entry<Vector3ic, CompressedChunkBuilder> entry : unloadedAndSavingChunkMap.entrySet()) {
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
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.REPLAY_FINISHED) {
            recordAndReplayUtils.setShutdownRequested(true); //Important to trigger complete serialization in a recording
        }
        if (saveTransaction != null) {
            saveTransaction.block();
        }
    }

    private SaveTransaction createSaveTransaction() {
        SaveTransactionBuilder saveTransactionBuilder = new SaveTransactionBuilder(privateEntityManager,
                entitySetDeltaRecorder, isStoreChunksInZips(), getStoragePathProvider(), worldDirectoryWriteLock,
                recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);

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
        /*
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
            saveTransactionBuilder.addLoadedPlayer(client.getId(), createPlayerStore(character));
        }

        for (Map.Entry<String, EntityData.PlayerStore> entry : unloadedAndSavingPlayerMap.entrySet()) {
            saveTransactionBuilder.addUnloadedPlayer(entry.getKey(), entry.getValue());
        }
    }

    private PlayerStoreBuilder createPlayerStore(EntityRef character) {
        LocationComponent location = character.getComponent(LocationComponent.class);
        Vector3f relevanceLocation;
        if (location != null) {
            relevanceLocation = location.getWorldPosition(new Vector3f());
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
    protected byte[] loadCompressedChunk(Vector3ic chunkPos) {
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
        UniverseConfig universeConfig = config.getUniverseConfig();
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
        List<WorldInfo> worlds = universeConfig.getWorlds();
        for (WorldInfo worldInfo: worlds) {
            gameManifest.addWorld(worldInfo);
        }

        WorldGenerator worldGenerator = CoreRegistry.get(WorldGenerator.class);
        if (worldGenerator != null) {
            WorldConfigurator worldConfigurator = worldGenerator.getConfigurator();
            var params = worldConfigurator.getProperties();
            gameManifest.setModuleConfigs(worldGenerator.getUri(), params);
        }

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

        if (saveRequested) {
            startSaving();
        } else if (isSavingNecessary()) {
            startAutoSaving();
        }
    }

    private boolean isRunModeAllowSaving() {
        NetworkSystem networkSystem = CoreRegistry.get(NetworkSystem.class);
        return networkSystem.getMode().isAuthority();
    }

    private void startSaving() {
        logger.info("Saving - Creating game snapshot");
        try (var ignored = PerformanceMonitor.startActivity("Saving")) {
            ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
            for (ComponentSystem sys : componentSystemManager.getAllSystems()) {
                sys.preSave();
            }

            saveRequested = false;
            saveTransaction = Mono.fromRunnable(createSaveTransaction())
                    .subscribeOn(GameScheduler.parallel())
                    .doFinally(unused -> this.saveComplete())
                    .share();
            saveTransaction.subscribe();

            if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.NOT_ACTIVATED) {
                saveGamePreviewImage();
            }

            for (ComponentSystem sys : componentSystemManager.getAllSystems()) {
                sys.postSave();
            }
        }
        entitySetDeltaRecorder = new EntitySetDeltaRecorder(this.entityRefReplacingComponentLibrary);
        logger.info("Saving - Snapshot created: Writing phase starts");
    }

    private void startAutoSaving() {
        logger.info("Auto Saving - Creating game snapshot");
        try (var ignored = PerformanceMonitor.startActivity("Auto Saving")) {
            ComponentSystemManager componentSystemManager = CoreRegistry.get(ComponentSystemManager.class);
            for (ComponentSystem sys : componentSystemManager.getAllSystems()) {
                sys.preAutoSave();
            }

            saveTransaction = Mono.fromRunnable(createSaveTransaction())
                    .subscribeOn(GameScheduler.parallel())
                    .doFinally(unused -> this.saveComplete())
                    .share();
            saveTransaction.subscribe();

            for (ComponentSystem sys : componentSystemManager.getAllSystems()) {
                sys.postAutoSave();
            }

            scheduleNextAutoSave();
        }
        entitySetDeltaRecorder = new EntitySetDeltaRecorder(this.entityRefReplacingComponentLibrary);
        logger.info("Auto Saving - Snapshot created: Writing phase starts");
    }

    private void saveComplete() {
        saveTransaction = null;
        unloadedAndSavingChunkMap.clear();
        unloadedAndSavingPlayerMap.clear();
    }

    private boolean isSavingNecessary() {
        ChunkProvider chunkProvider = CoreRegistry.get(ChunkProvider.class);
        int unloadedChunkCount = unloadedAndUnsavedChunkMap.size();
        int loadedChunkCount = chunkProvider.getAllChunks().size();
        double totalChunkCount = unloadedChunkCount + loadedChunkCount;
        double percentageUnloaded = 100.0 * unloadedChunkCount / totalChunkCount;
        if (percentageUnloaded >= systemConfig.maxUnloadedChunksPercentageTillSave.get()) {
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
        long msBetweenAutoSave = (long) systemConfig.maxSecondsBetweenSaves.get() * 1000;
        nextAutoSave = System.currentTimeMillis() + msBetweenAutoSave;
    }

    private void saveGamePreviewImage() {
        final ScreenGrabber screenGrabber = CoreRegistry.get(ScreenGrabber.class);
        final Game game = CoreRegistry.get(Game.class);
        if (screenGrabber != null && game != null) {
            screenGrabber.takeGamePreview(PathManager.getInstance().getSavePath(game.getName()));
        }
    }

    @Override
    public boolean isSaving() {
        return saveTransaction != null;
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
