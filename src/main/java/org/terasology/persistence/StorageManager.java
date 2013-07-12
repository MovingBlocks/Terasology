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
package org.terasology.persistence;

import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.math.Vector3i;
import org.terasology.protobuf.EntityData;
import org.terasology.world.chunks.Chunk;

import java.io.IOException;

/**
 * The entity store manager handles the storing and retrieval of stores of entities (and other data). In particular
 * it keeps track of their existence and the external references of each store, which can be invalidated.
 * @author Immortius
 */
public interface StorageManager {

    /**
     * @return A new global store ready for saving into
     */
    GlobalStore createGlobalStoreForSave();

    /**
     * Loads the global store, restoring the entity manager's state and all global entities
     */
    void loadGlobalStore() throws IOException;

    /**
     * Creates an empty player store for saving
     * @param playerId
     * @return The new player store
     */
    PlayerStore createPlayerStoreForSave(String playerId);

    /**
     * Loads a saved player store
     * @param playerId
     * @return The retrieved player store, or null if no player is saved with that id
     */
    PlayerStore loadPlayerStore(String playerId);

    /**
     * Creates an empty chunk store for saving
     * @param chunk The chunk to be saved
     * @return The new chunk store
     */
    ChunkStore createChunkStoreForSave(Chunk chunk);

    /**
     * Loads a saved chunk store
     * @param chunkPos
     *
     */
    ChunkStore loadChunkStore(Vector3i chunkPos);

    /**
     * @param chunkPos
     * @return Whether the storage manager has the desired chunk store
     */
    boolean containsChunkStoreFor(Vector3i chunkPos);

    void flush() throws IOException;

    void shutdown();

}
