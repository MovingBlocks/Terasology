/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityDestroySubscriber;
import org.terasology.entitySystem.EntityRef;
import org.terasology.math.Vector3i;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.PlayerStore;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public final class StorageManagerInternal implements StorageManager, EntityDestroySubscriber {
    private static final String PLAYERS_PATH = "players";
    private static final String WORLDS_PATH = "worlds";
    private static final String PLAYER_STORE_EXTENSION = ".player";
    private static final String GLOBAL_ENTITY_STORE = "global.dat";

    private static final Logger logger = LoggerFactory.getLogger(StorageManagerInternal.class);

    private Path playersPath;

    private EngineEntityManager entityManager;
    private Map<String, EntityData.PlayerStore> playerStores = Maps.newHashMap();
    private TIntObjectMap<List<StoreMetadata>> externalRefHolderLookup = new TIntObjectHashMap<>();
    private Map<StoreId, StoreMetadata> storeMetadata = Maps.newHashMap();

    private Map<Vector3i, ChunkStoreInternal> chunkStores = Maps.newHashMap();

    public StorageManagerInternal(EngineEntityManager entityManager) {
        this.entityManager = entityManager;
        entityManager.subscribe(this);
        playersPath = PathManager.getInstance().getCurrentSavePath().resolve(PLAYERS_PATH);
    }

    @Override
    public void loadGlobalEntities() throws IOException {
        Path globalDataFile = PathManager.getInstance().getCurrentSavePath().resolve(GLOBAL_ENTITY_STORE);
        if (Files.isRegularFile(globalDataFile)) {
            try (InputStream in = new BufferedInputStream(Files.newInputStream(globalDataFile))) {
                EntityData.GlobalStore store = EntityData.GlobalStore.parseFrom(in);
                GlobalStoreLoader loader = new GlobalStoreLoader(entityManager);
                loader.load(store);
                for (StoreMetadata refTable : loader.getStoreMetadata()) {
                    storeMetadata.put(refTable.getId(), refTable);
                    indexStoreMetadata(refTable);
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        flushPlayerStores();
        flushChunkStores();
        flushGlobalStore();
    }

    private void flushGlobalStore() throws IOException {
        GlobalStoreSaver globalStore = new GlobalStoreSaver(entityManager);
        for (EntityRef entity : entityManager.getAllEntities()) {
            globalStore.store(entity);
        }
        for (StoreMetadata table : storeMetadata.values()) {
            globalStore.addStoreMetadata(table);
        }
        EntityData.GlobalStore globalStoreData = globalStore.save();
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(PathManager.getInstance().getCurrentSavePath().resolve(GLOBAL_ENTITY_STORE)))) {
            globalStoreData.writeTo(out);
        }
    }

    @Override
    public PlayerStore createPlayerStoreForSave(String playerId) {
        return new PlayerStoreInternal(playerId, this, entityManager);
    }

    private void flushPlayerStores() throws IOException {
        Files.createDirectories(playersPath);
        for (Map.Entry<String, EntityData.PlayerStore> playerStoreEntry : playerStores.entrySet()) {
            Path playerFile = playersPath.resolve(playerStoreEntry.getKey() + PLAYER_STORE_EXTENSION);
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(playerFile));) {
                playerStoreEntry.getValue().writeTo(out);
            }
        }
        playerStores.clear();
    }

    @Override
    public PlayerStore loadPlayerStore(String playerId) {
        EntityData.PlayerStore store = playerStores.get(playerId);
        if (store == null) {
            Path storePath = playersPath.resolve(playerId + PLAYER_STORE_EXTENSION);
            if (Files.isRegularFile(storePath)) {
                try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(storePath))) {
                    store = EntityData.PlayerStore.parseFrom(inputStream);
                } catch (IOException e) {
                    logger.error("Failed to load player data for {}", playerId, e);
                }
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
        return null;
    }

    public void store(String id, EntityData.PlayerStore playerStore, TIntSet externalReference) {
        if (externalReference.size() > 0) {
            StoreMetadata metadata = new StoreMetadata(new PlayerStoreId(id), externalReference);
            indexStoreMetadata(metadata);
        }
        playerStores.put(id, playerStore);
    }

    @Override
    public ChunkStore createChunkStoreForSave(Chunk chunk) {
        return new ChunkStoreInternal(chunk, this, entityManager);
    }

    @Override
    public ChunkStore loadChunkStore(Vector3i chunkPos) {
        ChunkStore store = chunkStores.get(chunkPos);
        if (store == null) {
            Path chunkPath = PathManager.getInstance().getCurrentSavePath().resolve(WORLDS_PATH).resolve(TerasologyConstants.MAIN_WORLD).resolve(getChunkFilename(chunkPos));
            if (Files.isRegularFile(chunkPath)) {
                try (InputStream in = new BufferedInputStream(Files.newInputStream(chunkPath))) {
                    TIntSet validRefs = null;
                    StoreMetadata table = storeMetadata.get(new ChunkStoreId(chunkPos));
                    if (table != null) {
                        validRefs = table.getExternalReferences();
                    }
                    EntityData.ChunkStore chunkData = EntityData.ChunkStore.parseFrom(in);
                    store = new ChunkStoreInternal(chunkData, validRefs, this, entityManager);


                } catch (IOException e) {
                    logger.error("Failed to load chunk {}", chunkPos, e);
                }
            }
        }
        return store;
    }

    private void flushChunkStores() throws IOException {
        Path chunksPath = PathManager.getInstance().getCurrentSavePath().resolve(WORLDS_PATH).resolve(TerasologyConstants.MAIN_WORLD);
        Files.createDirectories(chunksPath);
        for (Map.Entry<Vector3i, ChunkStoreInternal> chunkStoreEntry : chunkStores.entrySet()) {
            Path chunkPath = chunksPath.resolve(getChunkFilename(chunkStoreEntry.getKey()));
            try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(chunkPath))) {
                chunkStoreEntry.getValue().getStore().writeTo(out);
            }
        }
    }

    private String getChunkFilename(Vector3i pos) {
        return String.format("%d.%d.%d.chunk", pos.x, pos.y, pos.z);
    }

    public void store(ChunkStoreInternal chunkStore, TIntSet externalRefs) {
        if (externalRefs.size() > 0) {
            StoreMetadata metadata = new StoreMetadata(new ChunkStoreId(chunkStore.getChunkPosition()), externalRefs);
            indexStoreMetadata(metadata);
        }
        this.chunkStores.put(chunkStore.getChunkPosition(), chunkStore);
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
        for (StoreMetadata table : tables) {
            table.getExternalReferences().remove(entityId);
            if (table.getExternalReferences().isEmpty()) {
                storeMetadata.remove(table.getId());
            }
        }
    }
}
