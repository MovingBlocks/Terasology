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
import org.terasology.engine.ComponentSystemManager;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.reflect.ReflectFactory;
import org.terasology.registry.CoreRegistry;
import org.terasology.engine.GameEngine;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.internal.EngineEntityManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.StorageManagerInternal;
import org.terasology.physics.Physics;
import org.terasology.physics.engine.PhysicsEngine;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.world.generator.UnresolvedWorldGeneratorException;
import org.terasology.world.generator.WorldGenerator;
import org.terasology.world.generator.internal.WorldGeneratorManager;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCoreImpl;
import org.terasology.world.internal.WorldProviderWrapper;

/**
 * @author Immortius
 */
public class InitialiseWorld extends SingleStepLoadProcess {

    private static final Logger logger = LoggerFactory.getLogger(InitialiseWorld.class);

    private GameManifest gameManifest;

    public InitialiseWorld(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Initializing world...";
    }

    @Override
    public boolean step() {

        CoreRegistry.put(WorldGeneratorPluginLibrary.class, new WorldGeneratorPluginLibrary(CoreRegistry.get(ModuleManager.class),
                CoreRegistry.get(ReflectFactory.class), CoreRegistry.get(CopyStrategyLibrary.class)));

        StorageManager storageManager = CoreRegistry.put(StorageManager.class,
                new StorageManagerInternal(CoreRegistry.get(ModuleManager.class), (EngineEntityManager) CoreRegistry.get(EntityManager.class)));
        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.nextString(16));
        }

        logger.info("World seed: \"{}\"", worldInfo.getSeed());

        // TODO: Separate WorldRenderer from world handling in general
        WorldGenerator worldGenerator;
        try {
            worldGenerator = CoreRegistry.get(WorldGeneratorManager.class).createGenerator(worldInfo.getWorldGenerator());
            CoreRegistry.put(WorldGenerator.class, worldGenerator);
        } catch (UnresolvedWorldGeneratorException e) {
            logger.error("Unable to load world generator", e);
            CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu("Failed to resolve world generator."));
            return false;
        }

        // Init. a new world
        LocalChunkProvider chunkProvider = new LocalChunkProvider(storageManager, worldGenerator);
        CoreRegistry.get(ComponentSystemManager.class).register(new RelevanceSystem(chunkProvider), "engine:relevanceSystem");
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(new WorldProviderCoreImpl(worldInfo, chunkProvider));
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        chunkProvider.setBlockEntityRegistry(entityWorldProvider);
        CoreRegistry.put(BlockEntityRegistry.class, entityWorldProvider);
        CoreRegistry.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");
        RenderingSubsystemFactory engineSubsystemFactory = CoreRegistry.get(RenderingSubsystemFactory.class);
        WorldRenderer worldRenderer = engineSubsystemFactory.createWorldRenderer(worldProvider, chunkProvider, CoreRegistry.get(LocalPlayerSystem.class));
        CoreRegistry.put(WorldRenderer.class, worldRenderer);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(PhysicsEngine.class, worldRenderer.getBulletRenderer());
        CoreRegistry.put(Physics.class, worldRenderer.getBulletRenderer());

        // TODO: This may be the wrong place, or we should change time handling so that it deals better with time not passing
        worldProvider.getTime().setMilliseconds(worldInfo.getTime());

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 5;
    }
}
