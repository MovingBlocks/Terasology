// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.context.Lifetime;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadOnlyStorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.backdrop.Skysphere;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.ChunkProvider;
import org.terasology.engine.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.engine.world.generator.ScalableWorldGenerator;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.DefaultWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCore;
import org.terasology.engine.world.internal.WorldProviderCoreImpl;
import org.terasology.engine.world.internal.WorldProviderWrapper;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;
import org.terasology.gestalt.di.ServiceRegistry;
import org.terasology.gestalt.module.exceptions.UnresolvedDependencyException;

import javax.inject.Inject;
import java.nio.file.Path;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

public class InitialiseWorld extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    private final GameManifest gameManifest;
    private final Context context;
    private final ServiceRegistry serviceRegistry;

    public InitialiseWorld(GameManifest gameManifest, Context context, ServiceRegistry serviceRegistry) {
        this.gameManifest = gameManifest;
        this.context = context;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public String getMessage() {
        return "Initializing world...";
    }

    @Override
    public boolean step() {
        serviceRegistry.with(WorldGeneratorPluginLibrary.class).lifetime(Lifetime.Singleton).use(DefaultWorldGeneratorPluginLibrary.class);

        WorldInfo worldInfo = verifyNotNull(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD),
                "Game manifest does not contain a MAIN_WORLD");
        verify(worldInfo.getWorldGenerator().isValid(), "Game manifest did not specify world type.");
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.nextString(16));
        }

        serviceRegistry.with(WorldInfo.class).lifetime(Lifetime.Singleton).use(() -> worldInfo);

        logger.info("World seed: \"{}\"", worldInfo.getSeed()); //NOPMD

        // TODO: Separate WorldRenderer from world handling in general
        WorldGeneratorManager worldGeneratorManager = context.get(WorldGeneratorManager.class);
        WorldGenerator worldGenerator;
        try {
            worldGenerator = WorldGeneratorManager.createGenerator(worldInfo.getWorldGenerator(), context);
            serviceRegistry.with(WorldGenerator.class).lifetime(Lifetime.Singleton).use(() -> worldGenerator);
            if (worldGenerator instanceof ScalableWorldGenerator) {
                serviceRegistry.with(ScalableWorldGenerator.class).lifetime(Lifetime.Singleton).use(() -> (ScalableWorldGenerator) worldGenerator);
            }
        } catch (UnresolvedWorldGeneratorException | UnresolvedDependencyException e) {
            logger.atError().log("Unable to load world generator {}. Available world generators: {}",
                    worldInfo.getWorldGenerator(), worldGeneratorManager.getWorldGenerators());
            context.get(GameEngine.class).changeState(new StateMainMenu("Failed to resolve world generator."));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }

        // Init. a new world
        boolean writeSaveGamesEnabled = context.get(SystemConfig.class).writeSaveGamesEnabled.get();
        serviceRegistry.with(StorageManager.class).lifetime(Lifetime.Singleton)
                .use(writeSaveGamesEnabled ? ReadWriteStorageManager.class : ReadOnlyStorageManager.class);
        serviceRegistry.with(ChunkProvider.class).lifetime(Lifetime.Singleton).use(LocalChunkProvider.class);
        serviceRegistry.with(RelevanceSystem.class).lifetime(Lifetime.Singleton).use(RelevanceSystem.class);
        // Provides both WorldProviderCore and BlockEntityRegistry
        serviceRegistry.with(WorldProviderCoreWorkAround.class).lifetime(Lifetime.Singleton);
        serviceRegistry.with(WorldProvider.class).lifetime(Lifetime.Singleton).use(WorldProviderWrapper.class);

        serviceRegistry.with(CelestialModel.class).lifetime(Lifetime.Singleton).use(BasicCelestialModel.class);
        serviceRegistry.with(CelestialSystem.class).lifetime(Lifetime.Singleton).use(DefaultCelestialSystem.class);

        serviceRegistry.with(BackdropProvider.class).lifetime(Lifetime.Singleton).use(Skysphere.class);

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        engineSubsystemFactory.registerWorldRenderer(context, serviceRegistry);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class),
                context.get(RecordAndReplayCurrentStatus.class));
        serviceRegistry.with(LocalPlayer.class).lifetime(Lifetime.Singleton).use(() -> localPlayer);
//        Camera activeCamera = worldRenderer.getActiveCamera();
//        serviceRegistry.with(Camera.class).lifetime(Lifetime.Singleton).use(() -> activeCamera);

        return true;
    }

    private Path getSaveOrRecordingPath() {
        Path saveOrRecordingPath;
        if (context.get(RecordAndReplayCurrentStatus.class).getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            saveOrRecordingPath = PathManager.getInstance().getRecordingPath(gameManifest.getTitle());
        } else {
            saveOrRecordingPath = PathManager.getInstance().getSavePath(gameManifest.getTitle());
        }
        return saveOrRecordingPath;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }

    /**
     * This work-around exists because you cannot easily have two instances of {@link WorldProviderCore} in the context at a given time.
     * One of these instances exists purely to wrap the other, so we instantiate both at the same time here to leave only one instance.
     */
    public static final class WorldProviderCoreWorkAround extends EntityAwareWorldProvider
            implements WorldProviderCore, BlockEntityRegistry {
        @Inject
        public WorldProviderCoreWorkAround(WorldInfo info, ChunkProvider chunkProvider, BlockManager blockManager,
                                           EngineEntityManager entityManager, ComponentSystemManager componentSystemManager) {
            super(new WorldProviderCoreImpl(info, chunkProvider, blockManager, entityManager), entityManager, componentSystemManager);
        }
    }
}
