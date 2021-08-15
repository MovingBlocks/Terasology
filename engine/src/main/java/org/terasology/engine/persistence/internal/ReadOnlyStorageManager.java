// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.internal;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * A {@link org.terasology.engine.persistence.StorageManager} that performs reading only.
 */
public final class ReadOnlyStorageManager extends AbstractStorageManager {

    public ReadOnlyStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                  BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        this(savePath, environment, entityManager, blockManager, extraDataManager, true);
    }

    public ReadOnlyStorageManager(Path savePath, ModuleEnvironment environment, EngineEntityManager entityManager,
                                  BlockManager blockManager, ExtraBlockDataManager extraDataManager, boolean storeChunksInZips) {
        super(savePath, environment, entityManager, blockManager, extraDataManager, storeChunksInZips);
    }

    @Override
    public void finishSavingAndShutdown() {
        // don't care
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
        Collection<EntityRef> entitiesOfChunk = getEntitiesOfChunk(chunk);

        entitiesOfChunk.forEach(this::deactivateOrDestroyEntityRecursive);
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
        EntityRef character = client.getEntity().getComponent(ClientComponent.class).character;
        deactivateOrDestroyEntityRecursive(character);
    }
}
