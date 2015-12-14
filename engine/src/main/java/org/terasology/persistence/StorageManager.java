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
package org.terasology.persistence;

import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.world.chunks.Chunk;

import java.io.IOException;

/**
 * The entity store manager handles the storing and retrieval of stores of entities (and other data). In particular
 * it keeps track of their existence and the external references of each store, which can be invalidated.
 *
 */
public interface StorageManager {

    /**
     * Loads the global store, restoring the entity manager's state and all global entities
     */
    void loadGlobalStore() throws IOException;

    /**
     * Loads a saved player store
     *
     * @param playerId
     * @return The retrieved player store, or null if no player is saved with that id
     */
    PlayerStore loadPlayerStore(String playerId);

    void requestSaving();

    void waitForCompletionOfPreviousSaveAndStartSaving();

    /**
     * Loads a saved chunk store
     *
     * @param chunkPos
     */
    ChunkStore loadChunkStore(Vector3i chunkPos);

    void finishSavingAndShutdown();

    /**
     * Deactivates the player and stores it at the next possible time.
     *
     * @param client
     */
    void deactivatePlayer(Client client);

    void update();

    /**
     * Deactivates the entities in the chunk and store the chunk a the next possible time.
     */
    void deactivateChunk(Chunk chunk);

    boolean isSaving();

    void checkAndRepairSaveIfNecessary() throws IOException;

    void deleteWorld();
}
