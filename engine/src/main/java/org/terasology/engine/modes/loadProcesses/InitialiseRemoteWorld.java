// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.ComponentSystemManager;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.engine.subsystem.RenderingSubsystemFactory;
import org.terasology.game.GameManifest;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.NetworkSystem;
import org.terasology.recording.DirectionAndOriginPosRecorderList;
import org.terasology.recording.RecordAndReplayCurrentStatus;
import org.terasology.rendering.backdrop.BackdropProvider;
import org.terasology.rendering.backdrop.BackdropRenderer;
import org.terasology.rendering.backdrop.Skysphere;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.world.chunks.remoteChunkProvider.RemoteChunkProvider;
import org.terasology.world.internal.EntityAwareWorldProvider;
import org.terasology.world.internal.WorldProviderCoreImpl;
import org.terasology.world.internal.WorldProviderWrapper;
import org.terasology.world.sun.BasicCelestialModel;
import org.terasology.world.sun.CelestialSystem;
import org.terasology.world.sun.DefaultCelestialSystem;

public class InitialiseRemoteWorld extends SingleStepLoadProcess {
    private final Context context;
    private final GameManifest gameManifest;


    public InitialiseRemoteWorld(Context context, GameManifest gameManifest) {
        this.context = context;
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Setting up remote world...";
    }

    @Override
    public boolean step() {

        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        LocalPlayer localPlayer = new LocalPlayer();
        localPlayer.setRecordAndReplayClasses(context.get(DirectionAndOriginPosRecorderList.class), context.get(RecordAndReplayCurrentStatus.class));
        context.put(LocalPlayer.class, localPlayer);
        BlockManager blockManager = context.get(BlockManager.class);
        ExtraBlockDataManager extraDataManager = context.get(ExtraBlockDataManager.class);

        RemoteChunkProvider chunkProvider = new RemoteChunkProvider(blockManager, localPlayer);

        WorldProviderCoreImpl worldProviderCore = new WorldProviderCoreImpl(gameManifest.getWorldInfo(TerasologyConstants.MAIN_WORLD), chunkProvider,
                blockManager.getBlock(BlockManager.UNLOADED_ID), context);
        EntityAwareWorldProvider entityWorldProvider = new EntityAwareWorldProvider(worldProviderCore, context);
        WorldProvider worldProvider = new WorldProviderWrapper(entityWorldProvider, extraDataManager);
        context.put(WorldProvider.class, worldProvider);
        context.put(BlockEntityRegistry.class, entityWorldProvider);
        context.get(ComponentSystemManager.class).register(entityWorldProvider, "engine:BlockEntityRegistry");

        DefaultCelestialSystem celestialSystem = new DefaultCelestialSystem(new BasicCelestialModel(), context);
        context.put(CelestialSystem.class, celestialSystem);
        context.get(ComponentSystemManager.class).register(celestialSystem);

        // Init. a new world
        Skysphere skysphere = new Skysphere(context);
        BackdropProvider backdropProvider = skysphere;
        BackdropRenderer backdropRenderer = skysphere;
        context.put(BackdropProvider.class, backdropProvider);
        context.put(BackdropRenderer.class, backdropRenderer);

        RenderingSubsystemFactory engineSubsystemFactory = context.get(RenderingSubsystemFactory.class);
        WorldRenderer worldRenderer = engineSubsystemFactory.createWorldRenderer(context);
        float reflectionHeight = context.get(NetworkSystem.class).getServer().getInfo().getReflectionHeight();
        worldRenderer.getActiveCamera().setReflectionHeight(reflectionHeight);
        context.put(WorldRenderer.class, worldRenderer);
        // TODO: These shouldn't be done here, nor so strongly tied to the world renderer
        context.put(Camera.class, worldRenderer.getActiveCamera());
        context.get(NetworkSystem.class).setRemoteWorldProvider(chunkProvider);

        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }

}
