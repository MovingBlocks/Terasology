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
import org.terasology.game.GameManifest;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;

import java.util.Map;

/**
 * Utility class for creating {@link SaveTransaction} instances.
 * @author Florian <florian@fkoeberle.de>
 */
class SaveTransactionBuilder {
    private Map<String, EntityData.PlayerStore> playerStores = Maps.newHashMap();
    private Map<Vector3i, CompressedChunkBuilder> compressedChunkBuilders = Maps.newHashMap();
    private EntityData.GlobalStore globalStore;
    private final boolean storeChunksInZips;
    private final StoragePathProvider storagePathProvider;
    private GameManifest gameManifest;

    SaveTransactionBuilder(boolean storeChunksInZips, StoragePathProvider storagePathProvider) {
        this.storeChunksInZips = storeChunksInZips;
        this.storagePathProvider = storagePathProvider;
    }

    public void addPlayerStore(String id, EntityData.PlayerStore playerStore) {
        playerStores.put(id, playerStore);
    }

    public void setGlobalStore(EntityData.GlobalStore globalStore) {
        this.globalStore = globalStore;
    }

    public void addCompressedChunkBuilder(final Vector3i chunkPosition, final CompressedChunkBuilder b) {
        compressedChunkBuilders.put(chunkPosition, b);
    }

    public SaveTransaction build() {
        return new SaveTransaction(playerStores, globalStore, compressedChunkBuilders, gameManifest, storeChunksInZips,
                storagePathProvider);
    }

    public void setGameManifest(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }
}
