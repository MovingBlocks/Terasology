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

package org.terasology.engine.modes.loadProcesses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.paths.PathManager;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.module.ModuleEnvironment;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.ReadOnlyStorageManager;
import org.terasology.persistence.internal.ReadWriteStorageManager;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.backdrop.Skysphere;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.DefaultWorldGeneratorPluginLibrary;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCoreImpl;
import org.terasology.world.internal.WorldProviderWrapper;
import org.terasology.world.sun.BasicCelestialModel;
import org.terasology.world.sun.CelestialSystem;
import org.terasology.world.sun.DefaultCelestialSystem;

import java.io.IOException;
import java.nio.file.Path;

/**
 */
public class InitialiseWorld extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    private GameManifest gameManifest;
    private Context context;

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
        BiomeManager biomeManager = context.get(BiomeManager.class);

        ModuleEnvironment environment = context.get(ModuleManager.class).getEnvironment();
        context.put(WorldGeneratorPluginLibrary.class, new DefaultWorldGeneratorPluginLibrary(environment, context));

        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.nextString(16));
        }

        logger.info("World seed: \"{}\"", worldInfo.getSeed());

        // TODO: Separate WorldRenderer from world handling in general
        WorldGeneratorManager worldGeneratorManager = context.get(WorldGeneratorManager.class);
        WorldGenerator worldGenerator;
        try {
            worldGenerator = WorldGeneratorManager.createGenerator(worldInfo.getWorldGenerator(), context);
            // setting the world seed will create the world builder
            worldGenerator.setWorldSeed(worldInfo.getSeed());
            context.put(WorldGenerator.class, worldGenerator);
        } catch (UnresolvedWorldGeneratorException e) {
            logger.error("Unable to load world generator {}. Available world generators: {}",
                    worldInfo.getWorldGenerator(), worldGeneratorManager.getWorldGenerators());
            context.get(GameEngine.class).changeState(new StateMainMenu("Failed to resolve world generator."));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }

        // Init. a new world
        EngineEntityManager entityManager = (EngineEntityManager) context.get(EntityManager.class);
        boolean writeSaveGamesEnabled = context.get(Config.class).getSystem().isWriteSaveGamesEnabled();
        Path savePath = PathManager.getInstance().getSavePath(gameManifest.getTitle());
        StorageManager storageManager;
        try {
            storageManager = writeSaveGamesEnabled
                    ? new ReadWriteStorageManager(savePath, environment, entityManager, blockManager, biomeManager)
                    : new ReadOnlyStorageManager(savePath, environment, entityManager, blockManager, biomeManager);
        } catch (IOException e) {
            logger.error("Unable to create storage manager!", e);
            context.get(GameEngine.class).changeState(new StateMainMenu("Unable to create storage manager!"));
            return true; // We need to return true, otherwise the loading state will just call us again immediately
        }
        context.put(StorageManager.class, storageManager);
        LocalChunkProvider chunkProvider = new LocalChunkProvider(storageManager, entityManager, worldGenerator,
                blockManager, biomeManager);
        context.get(ComponentSystemManager.class).register(new RelevanceSystem(chunkProvider), "engine:relevanceSystem");
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(
                new WorldProviderCoreImpl(worldInfo, chunkProvider, blockManager.getBlock(BlockManager.UNLOADED_ID),
                        context)
                , context);
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider);
        context.put(WorldProvider.class, worldProvider);
        chunkProvider.setBlockEntityRegistry(entityWorldProvider);
        context.put(BlockEntityRegistry.class, entityWorldProvider);
        context.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");

        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
        context.get(ComponentSystemManager.class).register(celestialSystem);

        Skysphere skysphere = new Skysphere();
        BackdropProvider backdropProvider = skysphere;
        BackdropRenderer backdropRenderer = skysphere;
        context.put(BackdropProvider.class, backdropProvider);
        context.put(BackdropRenderer.class, backdropRenderer);

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        WorldRenderer worldRenderer = engineSubsystemFactory.createWorldRenderer(context);
        context.put(WorldRenderer.class, worldRenderer);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        context.put(LocalPlayer.class, new LocalPlayer());
        context.put(Camera.class, worldRenderer.getActiveCamera());

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }
}
