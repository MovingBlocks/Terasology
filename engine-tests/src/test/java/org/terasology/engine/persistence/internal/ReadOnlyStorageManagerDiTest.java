// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.internal;

import org.junit.jupiter.api.Test;
import org.terasology.context.Lifetime;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.entitySystem.metadata.EntitySystemLibrary;
import org.terasology.engine.game.Game;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.testUtil.ModuleManagerFactory;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.module.ModuleEnvironment;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Regression test for #5415: {@code ReadOnlyStorageManager} must be instantiable by the dependency injection
 * container so that a world can be loaded when save games are disabled ({@code --no-save-games}).
 */
public class ReadOnlyStorageManagerDiTest {

    @Test
    void readOnlyStorageManagerIsInjectableViaDependencyInjection() {
        Game game = new Game();
        game.load(new GameManifest("test-world", "", 0));

        ModuleEnvironment moduleEnvironment = mock(ModuleEnvironment.class);
        EngineEntityManager entityManager = mock(EngineEntityManager.class);
        BlockManager blockManager = mock(BlockManager.class);
        ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();

        ServiceRegistry diRegistry = new ServiceRegistry();
        diRegistry.with(Game.class).lifetime(Lifetime.Singleton).use(() -> game);
        diRegistry.with(ModuleEnvironment.class).lifetime(Lifetime.Singleton).use(() -> moduleEnvironment);
        diRegistry.with(EngineEntityManager.class).lifetime(Lifetime.Singleton).use(() -> entityManager);
        diRegistry.with(BlockManager.class).lifetime(Lifetime.Singleton).use(() -> blockManager);
        diRegistry.with(ExtraBlockDataManager.class).lifetime(Lifetime.Singleton).use(() -> extraDataManager);
        diRegistry.with(StorageManager.class).lifetime(Lifetime.Singleton).use(ReadOnlyStorageManager.class);

        org.terasology.engine.context.Context diContext = new org.terasology.engine.context.internal.ContextImpl(diRegistry);
        StorageManager storageManager = diContext.get(StorageManager.class);

        assertNotNull(storageManager, "ReadOnlyStorageManager must be instantiable by the DI container");
        assertTrue(storageManager instanceof ReadOnlyStorageManager,
                "expected ReadOnlyStorageManager but got " + storageManager.getClass().getName());
    }

    @Test
    void savePathIsResolvedFromGameTitle() {
        Game game = new Game();
        game.load(new GameManifest("test-world", "", 0));

        ModuleEnvironment moduleEnvironment = mock(ModuleEnvironment.class);
        EngineEntityManager entityManager = mock(EngineEntityManager.class);
        BlockManager blockManager = mock(BlockManager.class);
        ExtraBlockDataManager extraDataManager = new ExtraBlockDataManager();

        ReadOnlyStorageManager storageManager = new ReadOnlyStorageManager(game, moduleEnvironment, entityManager,
                blockManager, extraDataManager);

        assertNotNull(storageManager);
        Path expected = PathManager.getInstance().getSavePath(game.getName());
        assertEquals(expected, storageManager.getStoragePathProvider().getStoragePathDirectory());
    }
}
