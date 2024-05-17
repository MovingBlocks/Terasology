// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.config.Config;
import org.terasology.engine.config.SystemConfig;
import org.terasology.engine.context.Context;
import org.terasology.engine.core.ComponentSystemManager;
import org.terasology.engine.core.GameEngine;
import org.terasology.engine.core.PathManager;
import org.terasology.engine.core.TerasologyConstants;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.core.modes.StateMainMenu;
import org.terasology.engine.core.module.ModuleManager;
import org.terasology.engine.core.subsystem.RenderingSubsystemFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.engine.game.GameManifest;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.persistence.StorageManager;
import org.terasology.engine.persistence.internal.ReadOnlyStorageManager;
import org.terasology.engine.persistence.internal.ReadWriteStorageManager;
import org.terasology.engine.recording.DirectionAndOriginPosRecorderList;
import org.terasology.engine.recording.RecordAndReplayCurrentStatus;
import org.terasology.engine.recording.RecordAndReplaySerializer;
import org.terasology.engine.recording.RecordAndReplayStatus;
import org.terasology.engine.recording.RecordAndReplayUtils;
import org.terasology.engine.rendering.backdrop.BackdropProvider;
import org.terasology.engine.rendering.backdrop.Skysphere;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.engine.rendering.world.WorldRenderer;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.engine.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.engine.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.engine.world.generator.WorldGenerator;
import org.terasology.engine.world.generator.internal.WorldGeneratorManager;
import org.terasology.engine.world.generator.plugin.DefaultWorldGeneratorPluginLibrary;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.engine.world.internal.EntityAwareWorldProvider;
import org.terasology.engine.world.internal.WorldInfo;
import org.terasology.engine.world.internal.WorldProviderCoreImpl;
import org.terasology.engine.world.internal.WorldProviderWrapper;
import org.terasology.engine.world.sun.BasicCelestialModel;
import org.terasology.engine.world.sun.CelestialSystem;
import org.terasology.engine.world.sun.DefaultCelestialSystem;
import org.terasology.gestalt.module.ModuleEnvironment;
import org.terasology.gestalt.module.exceptions.UnresolvedDependencyException;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;

public class InitialiseWorld extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    private final GameManifest gameManifest;
    private final Context context;

    public InitialiseWorld(GameManifest gameManifest, Context context) {
        this.gameManifest = gameManifest;
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Initializing world...";
    }

    @Override
    public boolean step() {
        BlockManager blockManager = context.get(BlockManager.class);
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);

        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        context.put(WorldGeneratorPluginLibrary.class, new DefaultWorldGeneratorPluginLibrary(environment, context));

        WorldInfo worldInfo = verifyNotNull(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD),
                "Game manifest does not contain a MAIN_WORLD");
        verify(worldInfo.getWorldGenerator().isValid(), "Game manifest did not specify world type.");
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.nextString(16));
        }

        logger.info("World seed: \"{}\"", worldInfo.getSeed()); //NOPMD

        // TODO: Separate WorldRenderer from world handling in general
        WorldGeneratorManager worldGeneratorManager = context.get(WorldGeneratorManager.class);
        WorldGenerator worldGenerator;
        try {
            worldGenerator = WorldGeneratorManager.createGenerator(worldInfo.getWorldGenerator(), context);
            // setting the world seed will create the world builder
            worldGenerator.setWorldSeed(worldInfo.getSeed());
            context.put(WorldGenerator.class, worldGenerator);
        } catch (UnresolvedWorldGeneratorException | UnresolvedDependencyException e) {
            logger.atError().log("Unable to load world generator {}. Available world generators: {}",
                    worldInfo.getWorldGenerator(), worldGeneratorManager.getWorldGenerators());
            context.get(GameEngine.class).changeState(new StateMainMenu("Failed to resolve world generator."));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }

        // Init. a new world
        EngineEntityManager entityManager = (EngineEntityManager) context.get(EntityManager.class);
        boolean writeSaveGamesEnabled = context.get(SystemConfig.class).writeSaveGamesEnabled.get();
        //Gets save data from a normal save or from a recording if it is a replay
        Path saveOrRecordingPath = getSaveOrRecordingPath();
        StorageManager storageManager;
        RecordAndReplaySerializer recordAndReplaySerializer = context.get(RecordAndReplaySerializer.class);
        RecordAndReplayUtils recordAndReplayUtils = context.get(RecordAndReplayUtils.class);
        RecordAndReplayCurrentStatus recordAndReplayCurrentStatus = context.get(RecordAndReplayCurrentStatus.class);
        try {
            storageManager = writeSaveGamesEnabled
                    ? new ReadWriteStorageManager(saveOrRecordingPath, environment, entityManager, blockManager,
                    extraDataManager, recordAndReplaySerializer, recordAndReplayUtils, recordAndReplayCurrentStatus)
                    : new ReadOnlyStorageManager(saveOrRecordingPath, environment, entityManager, blockManager,
                    extraDataManager);
        } catch (IOException e) {
            logger.error("Unable to create storage manager!", e);
            context.get(GameEngine.class).changeState(new StateMainMenu("Unable to create storage manager!"));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }
        context.put(StorageManager.class, storageManager);
        LocalChunkProvider chunkProvider = new LocalChunkProvider(storageManager,
                entityManager,
                worldGenerator,
                blockManager,
                extraDataManager,
                context.get(Config.class),
                Maps.newConcurrentMap());
        RelevanceSystem relevanceSystem = new RelevanceSystem(chunkProvider);
        context.put(RelevanceSystem.class, relevanceSystem);
        context.get(ComponentSystemManager.class).register(relevanceSystem, "engine:relevanceSystem");
        chunkProvider.setRelevanceSystem(relevanceSystem);
        Block unloadedBlock = blockManager.getBlock(BlockManager.UNLOADED_ID);
        WorldProviderCoreImpl worldProviderCore = new WorldProviderCoreImpl(worldInfo, chunkProvider, unloadedBlock,
                context);
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(worldProviderCore, context);
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider, extraDataManager);
        context.put(WorldProvider.class, worldProvider);
        chunkProvider.setBlockEntityRegistry(entityWorldProvider);
        context.put(BlockEntityRegistry.class, entityWorldProvider);
        context.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");

        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
        context.get(ComponentSystemManager.class).register(celestialSystem);

        Skysphere skysphere = new Skysphere(context);
        BackdropProvider backdropProvider = skysphere;
        context.put(BackdropProvider.class, backdropProvider);

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        WorldRenderer worldRenderer = engineSubsystemFactory.createWorldRenderer(context);
        context.put(WorldRenderer.class, worldRenderer);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class),
                context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        context.put(Camera.class, worldRenderer.getActiveCamera());

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
}
