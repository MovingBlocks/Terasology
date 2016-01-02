/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.nio.zipfs.ZipFileSystemProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.ClientComponent;
import org.terasology.protobuf.EntityData;
import org.terasology.utilities.concurrency.AbstractTask;
import org.terasology.world.chunks.internal.ChunkImpl;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * Task that writes a previously created memory snapshot of the game to the disk.
 * <br><br>
 * The result of this task can be obtained via {@link #getResult()}.
 *
 */
public class SaveTransaction extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(SaveTransaction.class);

    private static final ImmutableMap<String, String> CREATE_ZIP_OPTIONS = ImmutableMap.of("create", "true", "encoding", "UTF-8");
    private final GameManifest gameManifest;
    private final Lock worldDirectoryWriteLock;
    private final EngineEntityManager privateEntityManager;
    private final EntitySetDeltaRecorder deltaToSave;
    private volatile SaveTransactionResult result;

    // Unprocessed data to save:
    private final Map<String, EntityData.PlayerStore> unloadedPlayers;
    private final Map<String, PlayerStoreBuilder> loadedPlayers;
    private final Map<Vector3i, CompressedChunkBuilder> unloadedChunks;
    private final Map<Vector3i, ChunkImpl> loadedChunks;
    private final GlobalStoreBuilder globalStoreBuilder;

    // processed data:
    private EntityData.GlobalStore globalStore;
    private Map<String, EntityData.PlayerStore> allPlayers;
    private Map<Vector3i, CompressedChunkBuilder> allChunks;


    // Save parameters:
    private final boolean storeChunksInZips;

    // utility classes for saving:
    private final StoragePathProvider storagePathProvider;
    private final SaveTransactionHelper saveTransactionHelper;


    public SaveTransaction(EngineEntityManager privateEntityManager, EntitySetDeltaRecorder deltaToSave,
                           Map<String, EntityData.PlayerStore> unloadedPlayers,
                           Map<String, PlayerStoreBuilder> loadedPlayers, GlobalStoreBuilder globalStoreBuilder,
                           Map<Vector3i, CompressedChunkBuilder> unloadedChunks, Map<Vector3i, ChunkImpl> loadedChunks,
                           GameManifest gameManifest, boolean storeChunksInZips,
                           StoragePathProvider storagePathProvider, Lock worldDirectoryWriteLock) {
        this.privateEntityManager = privateEntityManager;
        this.deltaToSave = deltaToSave;
        this.unloadedPlayers = unloadedPlayers;
        this.loadedPlayers = loadedPlayers;
        this.unloadedChunks = unloadedChunks;
        this.loadedChunks = loadedChunks;
        this.globalStoreBuilder = globalStoreBuilder;
        this.gameManifest = gameManifest;
        this.storeChunksInZips = storeChunksInZips;
        this.storagePathProvider = storagePathProvider;
        this.saveTransactionHelper = new SaveTransactionHelper(storagePathProvider);
        this.worldDirectoryWriteLock = worldDirectoryWriteLock;
    }


    @Override
    public String getName() {
        return "Saving";
    }

    @Override
    public void run() {
        try {
            if (Files.exists(storagePathProvider.getUnmergedChangesPath())) {
                // should not happen, as initialization should clean it up
                throw new IOException("Save rand while there were unmerged changes");
            }
            saveTransactionHelper.cleanupSaveTransactionDirectory();
            applyDeltaToPrivateEntityManager();
            prepareChunksPlayersAndGlobalStore();
            createSaveTransactionDirectory();
            writePlayerStores();
            writeGlobalStore();
            writeChunkStores();
            saveGameManifest();
            perpareChangesForMerge();
            mergeChanges();
            result = SaveTransactionResult.createSuccessResult();
            logger.info("Save game finished");
        } catch (IOException | RuntimeException t) {
            logger.error("Save game creation failed", t);
            result = SaveTransactionResult.createFailureResult(t);
        }
    }

    private void prepareChunksPlayersAndGlobalStore() {
        /**
         * Currently loaded persistent entities without owner that have not been saved yet.
         */
        Set<EntityRef> unsavedEntities = new HashSet<>();
        for (EntityRef entity : privateEntityManager.getAllEntities()) {
            if (entity.isPersistent()) {
                unsavedEntities.add(entity);
            }
        }
        preparePlayerStores(unsavedEntities);
        prepareCompressedChunkBuilders(unsavedEntities);
        this.globalStore = globalStoreBuilder.build(privateEntityManager, unsavedEntities);
    }


    /**
     * @param unsavedEntities currently loaded persistent entities without owner that have not been saved yet.
     *                        This method removes entities it saves.
     */
    private void prepareCompressedChunkBuilders(Set<EntityRef> unsavedEntities) {
        Map<Vector3i, Collection<EntityRef>> chunkPosToEntitiesMap = createChunkPosToUnsavedOwnerLessEntitiesMap();

        allChunks = Maps.newHashMap();
        allChunks.putAll(unloadedChunks);
        for (Map.Entry<Vector3i, ChunkImpl> chunkEntry : loadedChunks.entrySet()) {
            Collection<EntityRef> entitiesToStore = chunkPosToEntitiesMap.get(chunkEntry.getKey());
            if (entitiesToStore == null) {
                entitiesToStore = Collections.emptySet();
            }
            ChunkImpl chunk = chunkEntry.getValue();
            unsavedEntities.removeAll(entitiesToStore);
            CompressedChunkBuilder compressedChunkBuilder = new CompressedChunkBuilder(privateEntityManager, chunk,
                    entitiesToStore, false);
            unsavedEntities.removeAll(compressedChunkBuilder.getStoredEntities());
            allChunks.put(chunkEntry.getKey(), compressedChunkBuilder);
        }
    }

    /**
     * @param unsavedEntities currently loaded persistent entities without owner that have not been saved yet.
     *                        This method removes entities it saves.
     */
    private void preparePlayerStores(Set<EntityRef> unsavedEntities) {
        allPlayers = Maps.newHashMap();
        allPlayers.putAll(unloadedPlayers);
        for (Map.Entry<String, PlayerStoreBuilder> playerEntry : loadedPlayers.entrySet()) {
            PlayerStoreBuilder playerStoreBuilder = playerEntry.getValue();
            EntityData.PlayerStore playerStore = playerStoreBuilder.build(privateEntityManager);
            unsavedEntities.removeAll(playerStoreBuilder.getStoredEntities());
            Long characterEntityId = playerStoreBuilder.getCharacterEntityId();
            if (characterEntityId != null) {
                EntityRef character = privateEntityManager.getEntity(characterEntityId);
                unsavedEntities.remove(character);
            }
            allPlayers.put(playerEntry.getKey(), playerStore);
        }
    }

    private Map<Vector3i, Collection<EntityRef>> createChunkPosToUnsavedOwnerLessEntitiesMap() {
        Map<Vector3i, Collection<EntityRef>> chunkPosToEntitiesMap = Maps.newHashMap();
        for (EntityRef entity : privateEntityManager.getEntitiesWith(LocationComponent.class)) {
            /*
             * Note: Entities with owners get saved with the owner. Entities that are always relevant don't get stored
             * in chunk as the chunk is not always loaded
             */
            if (entity.isPersistent() && !entity.getOwner().exists() && !entity.hasComponent(ClientComponent.class)
                    && !entity.isAlwaysRelevant()) {
                LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
                if (locationComponent != null) {
                    Vector3f loc = locationComponent.getWorldPosition();
                    Vector3i chunkPos = ChunkMath.calcChunkPos((int) loc.x, (int) loc.y, (int) loc.z);
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


    private void applyDeltaToPrivateEntityManager() {
        deltaToSave.getEntityDeltas().forEachEntry((entityId, delta) -> {
            if (entityId >= privateEntityManager.getNextId()) {
                privateEntityManager.setNextId(entityId + 1);
            }
            return true;
        });
        deltaToSave.getDestroyedEntities().forEach(entityId -> {
            if (entityId >= privateEntityManager.getNextId()) {
                privateEntityManager.setNextId(entityId + 1);
            }
            return true;
        });
        deltaToSave.getEntityDeltas().forEachEntry((entityId, delta) -> {
            if (privateEntityManager.isActiveEntity(entityId)) {
                EntityRef entity = privateEntityManager.getEntity(entityId);
                for (Component changedComponent : delta.getChangedComponents().values()) {
                    entity.removeComponent(changedComponent.getClass());
                    entity.addComponent(changedComponent);
                }
                delta.getRemovedComponents().forEach(entity::removeComponent);
            } else {
                privateEntityManager.createEntityWithId(entityId, delta.getChangedComponents().values());
            }

            return true;
        });
        final List<EntityRef> entitiesToDestroy = Lists.newArrayList();
        deltaToSave.getDestroyedEntities().forEach(entityId -> {
            EntityRef entityToDestroy;
            if (privateEntityManager.isActiveEntity(entityId)) {
                entityToDestroy = privateEntityManager.getEntity(entityId);
            } else {
                /**
                 * Create the entity as theere could be a component that references a {@link DelayedEntityRef}
                 * with the specified id. It is important that the {@link DelayedEntityRef} will reference
                 * a destroyed {@link EntityRef} instance. That is why a entity will be created, potentially
                 * bound to one or more {@link DelayedEntityRef}s and then destroyed.
                 *
                 */
                entityToDestroy = privateEntityManager.createEntityWithId(entityId,
                        Collections.<Component>emptyList());
            }
            entitiesToDestroy.add(entityToDestroy);
            return true;
        });

        /*
         * Bind the delayed entities refs, before destroying the entities:
         *
         * That way delayed entity refs will reference the enttiy refs that got marked as destroyed and now new
         * unloaded ones.
         */
        deltaToSave.bindAllDelayedEntityRefsTo(privateEntityManager);

        entitiesToDestroy.forEach(EntityRef::destroy);

        deltaToSave.getDeactivatedEntities().forEach(entityId -> {
            EntityRef entityRef = privateEntityManager.getEntity(entityId);
            privateEntityManager.deactivateForStorage(entityRef);
            return true;
        });
    }

    private void createSaveTransactionDirectory() throws IOException {
        Path directory = storagePathProvider.getUnfinishedSaveTransactionPath();
        Files.createDirectories(directory);
    }

    private void perpareChangesForMerge() throws IOException {
        try {
            renameMergeFolder();
        } catch (AccessDeniedException e) {
            /*
             * On some windows systems the rename fails sometimes with a AccessDeniedException, The exact cause is
             * unknown, but it is propablz a virus scanner. Renaming the folder 1 second later works.
             */
            logger.warn("Rename of merge folder failed, retrying in one second");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
            renameMergeFolder();
        }
    }

    private void renameMergeFolder() throws IOException {
        Path directoryForUnfinishedFiles = storagePathProvider.getUnfinishedSaveTransactionPath();
        Path directoryForFinishedFiles = storagePathProvider.getUnmergedChangesPath();

        try {
            Files.move(directoryForUnfinishedFiles, directoryForFinishedFiles, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            logger.warn("Atomic rename of merge folder was not possible, doing it non atomically...");
            Files.move(directoryForUnfinishedFiles, directoryForFinishedFiles);
        }
    }


    private void writePlayerStores() throws IOException {
        Files.createDirectories(storagePathProvider.getPlayersTempPath());
        for (Map.Entry<String, EntityData.PlayerStore> playerStoreEntry : allPlayers.entrySet()) {
            Path playerFile = storagePathProvider.getPlayerFileTempPath(playerStoreEntry.getKey());
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(playerFile))) {
                playerStoreEntry.getValue().writeTo(out);
            }
        }
    }

    private void writeGlobalStore() throws IOException {
        Path path = storagePathProvider.getGlobalEntityStoreTempPath();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(path))) {
            globalStore.writeTo(out);
        }
    }

    private void writeChunkStores() throws IOException {
        FileSystemProvider zipProvider = new ZipFileSystemProvider();

        Path chunksPath = storagePathProvider.getWorldTempPath();
        Files.createDirectories(chunksPath);
        if (storeChunksInZips) {
            Map<Vector3i, FileSystem> newChunkZips = Maps.newHashMap();
            for (Map.Entry<Vector3i, CompressedChunkBuilder> entry : allChunks.entrySet()) {
                Vector3i chunkPos = entry.getKey();
                Vector3i chunkZipPos = storagePathProvider.getChunkZipPosition(chunkPos);
                FileSystem zip = newChunkZips.get(chunkZipPos);
                if (zip == null) {
                    Path targetPath = storagePathProvider.getChunkZipTempPath(chunkZipPos);
                    Files.deleteIfExists(targetPath);
                    zip = zipProvider.newFileSystem(targetPath, CREATE_ZIP_OPTIONS);
                    newChunkZips.put(chunkZipPos, zip);
                }
                Path chunkPath = zip.getPath(storagePathProvider.getChunkFilename(chunkPos));
                CompressedChunkBuilder compressedChunkBuilder = entry.getValue();
                byte[] compressedChunk = compressedChunkBuilder.buildEncodedChunk();
                try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                    bos.write(compressedChunk);
                }
            }
            // Copy existing, unmodified content into the zips and close them
            for (Map.Entry<Vector3i, FileSystem> chunkZipEntry : newChunkZips.entrySet()) {
                Vector3i chunkZipPos = chunkZipEntry.getKey();
                Path oldChunkZipPath = storagePathProvider.getChunkZipPath(chunkZipPos);
                final FileSystem zip = chunkZipEntry.getValue();
                if (Files.isRegularFile(oldChunkZipPath)) {
                    try (FileSystem oldZip = FileSystems.newFileSystem(oldChunkZipPath, null)) {
                        for (Path root : oldZip.getRootDirectories()) {
                            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                                        throws IOException {
                                    if (!Files.isRegularFile(zip.getPath(file.toString()))) {
                                        Files.copy(file, zip.getPath(file.toString()));
                                    }
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                        }
                    }
                }
                zip.close();
            }
        } else {
            for (Map.Entry<Vector3i, CompressedChunkBuilder> entry : allChunks.entrySet()) {
                Vector3i chunkPos = entry.getKey();
                CompressedChunkBuilder compressedChunkBuilder = entry.getValue();
                byte[] compressedChunk = compressedChunkBuilder.buildEncodedChunk();
                Path chunkPath = storagePathProvider.getChunkTempPath(chunkPos);
                try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                    out.write(compressedChunk);
                }
            }
        }
    }

    /**
     * @return the result if there is one yet or null. This method returns the value of a volatile variable and
     * can thus be used even from another thread.
     */
    public SaveTransactionResult getResult() {
        return result;
    }

    private void saveGameManifest() {
        try {
            Path path = storagePathProvider.getGameManifestTempPath();
            GameManifest.save(path, gameManifest);
        } catch (IOException e) {
            logger.error("Failed to save world manifest", e);
        }
    }

    private void mergeChanges() throws IOException {
        worldDirectoryWriteLock.lock();
        try {
            saveTransactionHelper.mergeChanges();
        } finally {
            worldDirectoryWriteLock.unlock();
        }
    }

}
