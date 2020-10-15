// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.registry.ContextAwareClassFactory;
import org.terasology.registry.In;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.backdrop.Skysphere;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkProvider;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldInfo;
import org.terasology.world.internal.WorldProviderCore;
import org.terasology.world.internal.WorldProviderCoreImpl;
import org.terasology.world.internal.WorldProviderWrapper;
import org.terasology.world.sun.BasicCelestialModel;
import org.terasology.world.sun.CelestialModel;
import org.terasology.world.sun.CelestialSystem;
import org.terasology.world.sun.DefaultCelestialSystem;

@ExpectedCost(1)
public class InitialiseRemoteWorld extends SingleStepLoadProcess {

    @In
    private GameManifest gameManifest;
    @In
    private DirectionAndOriginPosRecorderList directionAndOriginPosRecorderList;
    @In
    private RecordAndReplayCurrentStatus recordAndReplayCurrentStatus;
    @In
    private BlockManager blockManager;
    @In
    private ContextAwareClassFactory classFactory;
    @In
    private ExtraBlockDataManager extraDataManager;
    @In
    private NetworkSystem networkSystem;
    @In
    private ComponentSystemManager componentSystemManager;
    @In
    private RenderingSubsystemFactory engineSubsystemFactory;
    @In
    private Context context;

    @Override
    public String getMessage() {
        return "Setting up remote world...";
    }

    @Override
    public boolean step() {

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = classFactory.createInjectableInstance(LocalPlayer.class);
        localPlayer.setRecordAndReplayClasses(directionAndOriginPosRecorderList, recordAndReplayCurrentStatus);
        RemoteChunkProvider chunkProvider = classFactory.createInjectableInstance(RemoteChunkProvider.class,
                ChunkProvider.class);

        context.put(WorldInfo.class, gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD));
        classFactory.createInjectableInstance(WorldProviderCoreImpl.class, WorldProviderCore.class);
        EntityAwareWorldProvider entityWorldProvider =
                classFactory.createInjectableInstance(EntityAwareWorldProvider.class,
                        BlockEntityRegistry.class);
        classFactory.createInjectableInstance(WorldProviderWrapper.class, WorldProvider.class);
        componentSystemManager.register(entityWorldProvider, "engine:BlockEntityRegistry");

        classFactory.createInjectableInstance(BasicCelestialModel.class, CelestialModel.class);
        DefaultCelestialSystem celestialSystem = classFactory.createInjectableInstance(DefaultCelestialSystem.class,
                CelestialSystem.class
        );
        componentSystemManager.register(celestialSystem);

        // Init. a new world
        classFactory.createInjectableInstance(Skysphere.class,
                BackdropProvider.class, BackdropRenderer.class);

        WorldRenderer worldRenderer = classFactory.createInjectable(WorldRenderer.class,
                engineSubsystemFactory::createWorldRenderer);
        float reflectionHeight = networkSystem.getServer().getInfo().getReflectionHeight();
        worldRenderer.getActiveCamera().setReflectionHeight(reflectionHeight);
        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        classFactory.createInjectable(Camera.class, worldRenderer::getActiveCamera);
        networkSystem.setRemoteWorldProvider(chunkProvider);

        return true;
    }
}
