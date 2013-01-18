package org.terasology.game.modes.loadProcesses;

import org.terasology.logic.players.LocalPlayerSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.physics.BulletPhysics;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldInfo;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;

/**
 * @author Immortius
 */
public class InitialiseRemoteWorld implements LoadProcess {
    private WorldInfo worldInfo;

    public InitialiseRemoteWorld(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public String getMessage() {
        return "Setting up remote world...";
    }

    @Override
    public boolean step() {

        RemoteChunkProvider chunkProvider = new RemoteChunkProvider();

        // Init. a new world
        WorldRenderer worldRenderer = new WorldRenderer(worldInfo, chunkProvider, CoreRegistry.get(LocalPlayerSystem.class));
        CoreRegistry.put(WorldRenderer.class, worldRenderer);
        CoreRegistry.put(WorldProvider.class, worldRenderer.getWorldProvider());

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        CoreRegistry.put(LocalPlayer.class, new LocalPlayer());
        CoreRegistry.put(Camera.class, worldRenderer.getActiveCamera());
        CoreRegistry.put(BulletPhysics.class, worldRenderer.getBulletRenderer());

        // TODO: This may be the wrong place, or we should change time handling so that it deals better with time not passing during loading
        CoreRegistry.get(WorldProvider.class).setTime(worldInfo.getTime());

        CoreRegistry.get(NetworkSystem.class).setRemoteWorldProvider(chunkProvider);

        return true;
    }

    @Override
    public int begin() {
        return 1;
    }
}
