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

import com.google.common.collect.Maps;
import org.joml.Vector3i;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.math.JomlUtil;
import org.terasology.protobuf.EntityData;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplaySerializer;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.world.chunks.internal.ChunkImpl;

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

    void addUnloadedChunk(final org.terasology.math.geom.Vector3i chunkPosition, final CompressedChunkBuilder b) {
        unloadedChunks.put(JomlUtil.from(chunkPosition), b);
    }


    void addLoadedChunk(final org.terasology.math.geom.Vector3i chunkPosition, final ChunkImpl chunk) {
        loadedChunks.put(JomlUtil.from(chunkPosition), chunk);
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
