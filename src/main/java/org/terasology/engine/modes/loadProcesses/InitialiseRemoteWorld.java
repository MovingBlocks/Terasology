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

import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.PhysicsEngine;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.EntityAwareWorldProvider;
import org.terasology.world.WorldProvider;
import org.terasology.world.WorldProviderCoreImpl;
import org.terasology.world.WorldProviderWrapper;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

/**
 * @author Immortius
 */
public class InitialiseRemoteWorld implements LoadProcess {
    private GameManifest gameManifest;

    public InitialiseRemoteWorld(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Setting up remote world...";
    }

    @Override
    public boolean step() {

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer());

        RemoteChunkProvider chunkProvider = new RemoteChunkProvider();

        WorldProviderCoreImpl worldProviderCore = new WorldProviderCoreImpl(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD), chunkProvider);
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(worldProviderCore);
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider);
        CoreRegistry.put(WorldProvider.class, worldProvider);
        CoreRegistry.put(BlockEntityRegistry.class, entityWorldProvider);
        CoreRegistry.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");

        // Init. a new world
        WorldRenderer worldRenderer = new WorldRenderer(worldProvider, chunkProvider, CoreRegistry.get(LocalPlayerSystem.class));
        CoreRegistry.put(WorldRenderer.class, worldRenderer);
        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(PhysicsEngine.class, worldRenderer.getBulletRenderer());

        CoreRegistry.get(NetworkSystem.class).setRemoteWorldProvider(chunkProvider);

        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
