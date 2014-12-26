/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.persistence.internal;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.math.Vector3i;
import org.terasology.module.ModuleEnvironment;
import org.terasology.network.Client;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;

/**
 * TODO Type description
 * @author Martin Steiger
 */
public class StorageManagerStub implements StorageManager {

    private static final Logger logger = LoggerFactory.getLogger(StorageManagerStub.class);

    private final StoragePathProvider storagePathProvider;

    private boolean storeChunksInZips = true;

    private ModuleEnvironment environment;
    private EngineEntityManager entityManager;
    private PrefabSerializer prefabSerializer;

    public StorageManagerStub(ModuleEnvironment environment, EngineEntityManager entityManager) {
        this(environment, entityManager, true);
    }

    public StorageManagerStub(ModuleEnvironment environment, EngineEntityManager entityManager, boolean storeChunksInZips) {
        this.entityManager = entityManager;
        this.environment = environment;
        this.storeChunksInZips = storeChunksInZips;
        this.prefabSerializer = new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        this.storagePathProvider = new StoragePathProvider(PathManager.getInstance().getCurrentSavePath());
    }


    @Override
    public void finishSavingAndShutdown() {
        // don't care
    }

    @Override
    public void loadGlobalStore() throws IOException {
        Path globalDataFile = storagePathProvider.getGlobalEntityStorePath();
        if (Files.isRegularFile(globalDataFile)) {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(globalDataFile))) {
                EntityData.GlobalStore store = EntityData.GlobalStore.parseFrom(in);
                GlobalStoreLoader loader = new GlobalStoreLoader(environment, entityManager, prefabSerializer);
                loader.load(store);
            }
        }
    }

    @Override
    public PlayerStore loadPlayerStore(String playerId) {
        EntityData.PlayerStore store = loadPlayerStoreData(playerId);
        if (store != null) {
            return new PlayerStoreInternal(playerId, store, this, entityManager);
        }
        return new PlayerStoreInternal(playerId, this, entityManager);
    }


    @Override
    public void requestSaving() {
        // don't care
    }

    @Override
    public void waitForCompletionOfPreviousSaveAndStartSaving() {
        // don't care
    }

    @Override
    public void deactivateChunk(Chunk chunk) {
        // don't care
    }

    @Override
    public ChunkStore loadChunkStore(Vector3i chunkPos) {
        byte[] chunkData = loadCompressedChunk(chunkPos);
        ChunkStore store = null;
        if (chunkData != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(chunkData);
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                EntityData.ChunkStore storeData = EntityData.ChunkStore.parseFrom(gzipIn);
                store = new ChunkStoreInternal(storeData, this, entityManager);
            } catch (IOException e) {
                logger.error("Failed to read existing saved chunk {}", chunkPos);
            }
        }
        return store;
    }

    protected byte[] loadChunkZip(Vector3i chunkPos) {
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

    @Override
    public void update() {
    }

    @Override
    public boolean isSaving() {
        return false;
    }

    @Override
    public void checkAndRepairSaveIfNecessary() throws IOException {
        // can't do that ..
    }

    @Override
    public void deleteWorld() {
        // can't do that ..
    }

    @Override
    public void deactivatePlayer(Client client) {
        // don't care
    }

    public boolean isStoreChunksInZips() {
        return storeChunksInZips;
    }

    /**
     * For tests only
     */
    void setStoreChunksInZips(boolean storeChunksInZips) {
        this.storeChunksInZips = storeChunksInZips;
    }

    protected byte[] loadCompressedChunk(Vector3i chunkPos) {
        if (isStoreChunksInZips()) {
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

    protected EntityData.PlayerStore loadPlayerStoreData(String playerId) {
        Path storePath = storagePathProvider.getPlayerFilePath(playerId);
        if (Files.isRegularFile(storePath)) {
            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(storePath))) {
                return EntityData.PlayerStore.parseFrom(inputStream);
            } catch (IOException e) {
                logger.error("Failed to load player data for {}", playerId, e);
            }
        }

        return null;
    }

}
