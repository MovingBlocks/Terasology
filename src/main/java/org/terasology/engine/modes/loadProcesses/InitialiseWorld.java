/*
 * Copyright 2013 Moving Blocks
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
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.entitySystem.EngineEntityManager;
import org.terasology.entitySystem.EntityManager;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.persistence.StorageManager;
import org.terasology.persistence.internal.StorageManagerInternal;
import org.terasology.physics.BulletPhysics;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.utilities.procedural.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.EntityAwareWorldProvider;
import org.terasology.world.WorldBiomeProviderImpl;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldProviderCoreImpl;
import org.terasology.world.WorldProviderWrapper;
import org.terasology.world.chunks.localChunkProvider.LocalChunkProvider;
import org.terasology.world.chunks.localChunkProvider.RelevanceSystem;
import org.terasology.world.generator.core.ChunkGeneratorManager;
import org.terasology.world.generator.core.ChunkGeneratorManagerImpl;

import java.util.Arrays;

/**
 * @author Immortius
 */
public class InitialiseWorld implements LoadProcess {

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
        StorageManager storageManager = CoreRegistry.put(StorageManager.class, new StorageManagerInternal((EngineEntityManager) CoreRegistry.get(EntityManager.class)));
        WorldInfo worldInfo = gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD);
        if (worldInfo.getSeed() == null || worldInfo.getSeed().isEmpty()) {
            FastRandom random = new FastRandom();
            worldInfo.setSeed(random.randomCharacterString(16));
        }

        logger.info("World seed: \"{}\"", worldInfo.getSeed());

        // TODO: Separate WorldRenderer from world handling in general
        // Init ChunkGeneratorManager
        ChunkGeneratorManager chunkGeneratorManager = ChunkGeneratorManagerImpl.buildChunkGenerator(Arrays.asList(worldInfo.getChunkGenerators()));
        chunkGeneratorManager.setWorldSeed(worldInfo.getSeed());
        chunkGeneratorManager.setWorldBiomeProvider(new WorldBiomeProviderImpl(worldInfo.getSeed()));

        // Init. a new world
        LocalChunkProvider chunkProvider = new LocalChunkProvider(storageManager, chunkGeneratorManager);
        CoreRegistry.get(ComponentSystemManager.class).register(new RelevanceSystem(chunkProvider), "engine:relevanceSystem");
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(new WorldProviderCoreImpl(worldInfo, chunkProvider));
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        chunkProvider.setBlockEntityRegistry(entityWorldProvider);
        CoreRegistry.put(BlockEntityRegistry.class, entityWorldProvider);
        CoreRegistry.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");
        WorldRenderer worldRenderer = new WorldRenderer(worldProvider, chunkProvider, CoreRegistry.get(LocalPlayerSystem.class));
        CoreRegistry.put(WorldRenderer.class, worldRenderer);

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysics.class, worldRenderer.getBulletRenderer());

        // TODO: This may be the wrong place, or we should change time handling so that it deals better with time not passing
        worldProvider.getTime().setMilliseconds(worldInfo.getTime());

        return true;
    }

    @Override
    public int begin() {
        return 1;
    }


}
