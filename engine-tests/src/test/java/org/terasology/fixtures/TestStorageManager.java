// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.fixtures;

import org.terasology.math.geom.Vector3i;
import org.terasology.network.Client;
import org.terasology.persistence.ChunkStore;
import org.terasology.persistence.PlayerStore;
import org.terasology.persistence.StorageManager;
import org.terasology.world.chunks.Chunk;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestStorageManager implements StorageManager {

    private final Map<Vector3i, ChunkStore> chunkStores = new HashMap<>();

    public TestStorageManager() {
    }

    public TestStorageManager(List<Chunk> chunkList) {
        this();
        chunkList.forEach(this::add);
    }

    public TestStorageManager(Map<Vector3i, ChunkStore> chunkStores) {
        this.chunkStores.putAll(chunkStores);
    }

    public void add(Chunk chunk) {
        chunkStores.put(chunk.getPosition(), new TestChunkStore(chunk));
    }

    @Override
    public void loadGlobalStore() throws IOException {

    }

    @Override
    public PlayerStore loadPlayerStore(String playerId) {
        return null;
    }

    @Override
    public void requestSaving() {

    }

    @Override
    public void waitForCompletionOfPreviousSaveAndStartSaving() {

    }

    @Override
    public ChunkStore loadChunkStore(Vector3i chunkPos) {
        return chunkStores.get(chunkPos);
    }

    @Override
    public void finishSavingAndShutdown() {

    }

    @Override
    public void deactivatePlayer(Client client) {

    }

    @Override
    public void update() {

    }

    @Override
    public void deactivateChunk(Chunk chunk) {

    }

    @Override
    public boolean isSaving() {
        return false;
    }

    @Override
    public void checkAndRepairSaveIfNecessary() throws IOException {

    }

    @Override
    public void deleteWorld() {

    }
}
