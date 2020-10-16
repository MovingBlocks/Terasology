// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadOnlyStorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.recording.RecordAndReplaySerializer;
import org.terasology.recording.RecordAndReplayStatus;
import org.terasology.recording.RecordAndReplayUtils;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.backdrop.Skysphere;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.DefaultWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.internal.WorldProviderCoreImpl;
import org.terasology.world.internal.WorldProviderWrapper;
import org.terasology.world.sun.BasicCelestialModel;
import org.terasology.world.sun.CelestialModel;
import org.terasology.world.sun.CelestialSystem;
import org.terasology.world.sun.DefaultCelestialSystem;

import java.io.IOException;
import java.nio.file.Path;

@ExpectedCost(5)
public class InitialiseWorld extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    @In
    private ContextAwareClassFactory classFactory;
    @In
    private BlockManager blockManager;
    @In
    private GameManifest gameManifest;
    @In
    private ExtraBlockDataManager extraDataManager;
    @In
    private ModuleManager moduleManager;
    @In
    private WorldGeneratorManager worldGeneratorManager;
    @In
    private GameEngine gameEngine;
    @In
    private RecordAndReplaySerializer recordAndReplaySerializer;
    @In
    private RecordAndReplayUtils recordAndReplayUtils;
    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    @In
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    @In
    private RenderingSubsystemFactory engineSubsystemFactory;
    @In
    private EngineEntityManager entityManager;
    @In
    private Config config;
    @In
    private ComponentSystemManager componentSystemManager;
    @In
    private Context context;

    @Override
    public String getMessage() {
        return "Initializing world...";
    }

    @Override
    public boolean step() {

        ModuleEnvironment environment = moduleManager.getEnvironment();
        classFactory.createToContext(DefaultWorldGeneratorPluginLibrary.class,
                WorldGeneratorPluginLibrary.class
        );

        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.nextString(16));
        }
        context.put(WorldInfo.class, worldInfo);

        logger.info("World seed: \"{}\"", worldInfo.getSeed());

        // TODO: Separate WorldRenderer from world handling in general
        WorldGenerator worldGenerator;
        try {
            worldGenerator = WorldGeneratorManager.createGenerator(worldInfo.getWorldGenerator(), context);
            // setting the world seed will create the world builder
            worldGenerator.setWorldSeed(worldInfo.getSeed());
            context.put(WorldGenerator.class, worldGenerator);
        } catch (UnresolvedWorldGeneratorException e) {
            logger.error("Unable to load world generator {}. Available world generators: {}",
                    worldInfo.getWorldGenerator(), worldGeneratorManager.getWorldGenerators());
            gameEngine.changeState(new StateMainMenu("Failed to resolve world generator."));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }

        // Init. a new world
        boolean writeSaveGamesEnabled = config.getSystem().isWriteSaveGamesEnabled();
        //Gets save data from a normal save or from a recording if it is a replay
        Path saveOrRecordingPath = getSaveOrRecordingPath();
        StorageManager storageManager;
        try {
            storageManager = writeSaveGamesEnabled
                    ? new ReadWriteStorageManager(saveOrRecordingPath, environment, entityManager, blockManager,
                    extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus)
                    : new ReadOnlyStorageManager(saveOrRecordingPath, environment, entityManager, blockManager,
                    extraDataManager);
        } catch (IOException e) {
            logger.error("Unable to create storage manager!", e);
            gameEngine.changeState(new StateMainMenu("Unable to create storage manager!"));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }
        context.put(StorageManager.class, storageManager);

        LocalChunkProvider chunkProvider = classFactory.createToContext(LocalChunkProvider.class,
                ChunkProvider.class, LocalChunkProvider.class);
        RelevanceSystem relevanceSystem = classFactory.createToContext(RelevanceSystem.class);
        componentSystemManager.register(relevanceSystem, "engine:relevanceSystem");
        chunkProvider.setRelevanceSystem(relevanceSystem);

        classFactory.createToContext(WorldProviderCoreImpl.class, WorldProviderCore.class);
        EntityAwareWorldProvider entityWorldProvider =
                classFactory.createToContext(EntityAwareWorldProvider.class,
                        BlockEntityRegistry.class);
        // EntityAwareWorldProvider implements WorldProviderCore too but not expose as it. must used in
        // WorldProviderWrapper.
        classFactory.createToContext(WorldProvider.class, () -> new WorldProviderWrapper(entityWorldProvider,
                extraDataManager));

        chunkProvider.setBlockEntityRegistry(entityWorldProvider);
        componentSystemManager.register(entityWorldProvider, "engine:BlockEntityRegistry");

        classFactory.createToContext(BasicCelestialModel.class, CelestialModel.class);
        DefaultCelestialSystem celestialSystem = classFactory.createToContext(DefaultCelestialSystem.class,
                CelestialSystem.class);
        componentSystemManager.register(celestialSystem);

        classFactory.createToContext(Skysphere.class,
                BackdropProvider.class, BackdropRenderer.class);

        WorldRenderer worldRenderer = classFactory.createToContext(WorldRenderer.class,
                engineSubsystemFactory::createWorldRenderer);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = classFactory.createToContext(LocalPlayer.class);
        localPlayer.setRecordAndReplayClasses(directionAndOriginPosRecorderList, recordAndReplayCurrentStatus);
        classFactory.createToContext(Camera.class, worldRenderer::getActiveCamera);

        return true;
    }

    private Path getSaveOrRecordingPath() {
        Path saveOrRecordingPath;
        if (recordAndReplayCurrentStatus.getStatus() == RecordAndReplayStatus.PREPARING_REPLAY) {
            saveOrRecordingPath = PathManager.getInstance().getRecordingPath(gameManifest.getTitle());
        } else {
            saveOrRecordingPath = PathManager.getInstance().getSavePath(gameManifest.getTitle());
        }
        return saveOrRecordingPath;
    }
}
