// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.protobuf.EntityData;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.world.chunks.internal.ChunkImpl;

import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Utility class for creating {@link SaveTransaction} instances.
 *
 */
class SaveTransactionBuilder {
    private final Lock worldDirectoryWriteLock;
    private final EngineEntityManager privateEntityManager;
    private final EntitySetDeltaRecorder deltaToSave;
    private Map<String, EntityData.PlayerStore> unloadedPlayers = Maps.newHashMap();
    private Map<String, PlayerStoreBuilder> loadedPlayers = Maps.newHashMap();
    private Map<Vector3i, CompressedChunkBuilder> unloadedChunks = Maps.newHashMap();
    private Map<Vector3i, ChunkImpl> loadedChunks = Maps.newHashMap();
    private GlobalStoreBuilder globalStoreBuilder;
    private final boolean storeChunksInZips;
    private final StoragePathProvider storagePathProvider;
    private GameManifest gameManifest;
    private RecordAndReplaySerializer recordAndReplaySerializer;
    private RecordAndReplayUtils recordAndReplayUtils;
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;

    SaveTransactionBuilder(EngineEntityManager privateEntityManager, EntitySetDeltaRecorder deltaToSave,
                           boolean storeChunksInZips, StoragePathProvider storagePathProvider,
                           Lock worldDirectoryWriteLock, RecordAndReplaySerializer recordAndReplaySerializer,
                           RecordAndReplayUtils recordAndReplayUtils,
                           RecordAndReplayCurrentStatus recordAndReplayCurrentStatus) {
        this.privateEntityManager = privateEntityManager;
        this.deltaToSave = deltaToSave;
        this.storeChunksInZips = storeChunksInZips;
        this.storagePathProvider = storagePathProvider;
        this.worldDirectoryWriteLock = worldDirectoryWriteLock;
        this.recordAndReplaySerializer = recordAndReplaySerializer;
        this.recordAndReplayUtils = recordAndReplayUtils;
        this.recordAndReplayCurrentStatus = recordAndReplayCurrentStatus;
    }

    public void addUnloadedPlayer(String id, EntityData.PlayerStore unloadedPlayer) {
        unloadedPlayers.put(id, unloadedPlayer);
    }

    void addLoadedPlayer(String id, PlayerStoreBuilder loadedPlayer) {
        loadedPlayers.put(id, loadedPlayer);
    }

    void setGlobalStoreBuilder(GlobalStoreBuilder globalStoreBuilder) {
        this.globalStoreBuilder = globalStoreBuilder;
    }

    void addUnloadedChunk(final Vector3ic chunkPosition, final CompressedChunkBuilder b) {
        unloadedChunks.put(new Vector3i(chunkPosition), b);
    }


    void addLoadedChunk(final Vector3ic chunkPosition, final ChunkImpl chunk) {
        loadedChunks.put(new Vector3i(chunkPosition), chunk);
    }

    public SaveTransaction build() {
        return new SaveTransaction(privateEntityManager, deltaToSave, unloadedPlayers, loadedPlayers, globalStoreBuilder,
                unloadedChunks, loadedChunks, gameManifest, storeChunksInZips, storagePathProvider,
                worldDirectoryWriteLock, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus);

    }

    public void setGameManifest(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }
}
