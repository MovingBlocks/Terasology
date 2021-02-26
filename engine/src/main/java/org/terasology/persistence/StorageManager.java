// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence;

import org.joml.Vector3ic;
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
    ChunkStore loadChunkStore(Vector3ic chunkPos);

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
