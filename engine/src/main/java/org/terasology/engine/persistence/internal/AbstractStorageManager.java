// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.internal;

import com.google.common.collect.Lists;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.entity.internal.OwnershipHelper;
import org.terasology.joml.geom.AABBfc;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.persistence.ChunkStore;
import org.terasology.engine.persistence.PlayerStore;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.serializers.PrefabSerializer;
import org.terasology.protobuf.EntityData;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * An abstract implementation of {@link StorageManager} that is able
 * to read from a data store.
 *
 */
public abstract class AbstractStorageManager implements StorageManager {

    private static final Logger logger = LoggerFactory.getLogger(AbstractStorageManager.class);

    private final StoragePathProvider storagePathProvider;
    private final BlockManager blockManager;
    private final ExtraBlockDataManager extraDataManager;

    private final ModuleEnvironment environment;
    private final EngineEntityManager entityManager;
    private final PrefabSerializer prefabSerializer;
    private final OwnershipHelper helper;

    private boolean storeChunksInZips = true;

    public AbstractStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                  BlockManager blockManager, ExtraBlockDataManager extraDataManager, boolean storeChunksInZips) {
        this.entityManager = entityManager;
        this.environment = environment;
        this.storeChunksInZips = storeChunksInZips;
        this.prefabSerializer = new PrefabSerializer(entityManager.getComponentLibrary(), entityManager.getTypeSerializerLibrary());
        this.blockManager = blockManager;
        this.extraDataManager = extraDataManager;

        this.storagePathProvider = new StoragePathProvider(savePath);
        this.helper = new OwnershipHelper(entityManager.getComponentLibrary());
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
            return new PlayerStoreInternal(playerId, store, entityManager);
        }
        return new PlayerStoreInternal(playerId, entityManager);
    }

    @Override
    public ChunkStore loadChunkStore(Vector3ic chunkPos) {
        byte[] chunkData = loadCompressedChunk(chunkPos);
        ChunkStore store = null;
        if (chunkData != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(chunkData);
            try (GZIPInputStream gzipIn = new GZIPInputStream(bais)) {
                EntityData.ChunkStore storeData = EntityData.ChunkStore.parseFrom(gzipIn);
                store = new ChunkStoreInternal(storeData, entityManager, blockManager, extraDataManager);
            } catch (IOException e) {
                logger.error("Failed to read existing saved chunk {}", chunkPos);
            }
        }
        return store;
    }

    protected byte[] loadChunkZip(Vector3ic chunkPos) {
        byte[] chunkData = null;
        Vector3i chunkZipPos = storagePathProvider.getChunkZipPosition(chunkPos);
        Path chunkPath = storagePathProvider.getChunkZipPath(chunkZipPos);
        if (Files.isRegularFile(chunkPath)) {
            try (FileSystem chunkZip = FileSystems.newFileSystem(chunkPath, (ClassLoader) null)) {
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

    public boolean isStoreChunksInZips() {
        return storeChunksInZips;
    }

    /**
     * For tests only
     */
    void setStoreChunksInZips(boolean storeChunksInZips) {
        this.storeChunksInZips = storeChunksInZips;
    }

    protected byte[] loadCompressedChunk(Vector3ic chunkPos) {
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

    protected Collection<EntityRef> getEntitiesOfChunk(Chunk chunk) {
        List<EntityRef> entitiesToStore = Lists.newArrayList();

        AABBfc aabb = chunk.getAABB();
        for (EntityRef entity : getEntityManager().getEntitiesWith(LocationComponent.class)) {
            if (!entity.getOwner().exists() && !entity.isAlwaysRelevant() && !entity.hasComponent(ClientComponent.class)) {
                LocationComponent loc = entity.getComponent(LocationComponent.class);
                if (loc == null) {
                    continue;
                }
                Vector3f pos = loc.getWorldPosition(new Vector3f());
                if (pos.isFinite() && aabb.containsPoint(loc.getWorldPosition(new Vector3f()))) {
                    entitiesToStore.add(entity);
                }
            }
        }
        return entitiesToStore;
    }

    protected void deactivateOrDestroyEntityRecursive(EntityRef entity) {
        if (entity.isActive()) {
            for (EntityRef ownedEntity : helper.listOwnedEntities(entity)) {
                if (!ownedEntity.isAlwaysRelevant()) {
                    if (!ownedEntity.isPersistent()) {
                        // TODO check if destroy is recursive
                        ownedEntity.destroy();
                    } else {
                        deactivateOrDestroyEntityRecursive(ownedEntity);
                    }
                }
            }
            getEntityManager().deactivateForStorage(entity);
        }
    }

    protected StoragePathProvider getStoragePathProvider() {
        return storagePathProvider;
    }

    protected ModuleEnvironment getEnvironment() {
        return environment;
    }

    protected EngineEntityManager getEntityManager() {
        return entityManager;
    }

    protected PrefabSerializer getPrefabSerializer() {
        return prefabSerializer;
    }
}
